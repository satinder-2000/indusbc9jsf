package org.indusbc.mbean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.BsonValue;
import org.indusbc.collections.ExpenseCategory;
import org.indusbc.collections.ProofOfIdDocument;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.indusbc.collections.Access;
import org.indusbc.collections.ExpenseAccount;
import org.indusbc.collections.ExpenseParty;
import org.indusbc.dtos.ExpensePartyDto;
import org.indusbc.util.AccessType;
import org.indusbc.util.FinancialYear;
import org.indusbc.util.HashGenerator;

/**
 *
 * @author singh
 */
@Named(value = "expensePartyRegisterMBean")
@FlowScoped(value = "ExpensePartyRegister")
public class ExpensePartyRegisterMBean implements Serializable{
    
    private static final Logger LOGGER = Logger.getLogger(ExpensePartyRegisterMBean.class.getName());
    private List<ProofOfIdDocument> proofOfIdDocList;
    private List<ExpenseCategory> expenseCategoryList;
    private ExpensePartyDto expensePartyDto;
    
    
    @PostConstruct
    public void init(){
        expensePartyDto = new ExpensePartyDto();
        ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ProofOfIdDocument> poIdColl = mongoDatabase.getCollection("ProofOfIdDocument", ProofOfIdDocument.class);
        Iterable<ProofOfIdDocument> poIdItrable=poIdColl.find();
        Iterator<ProofOfIdDocument> poIdItr=poIdItrable.iterator();
        proofOfIdDocList=new ArrayList<>();
        while(poIdItr.hasNext()){
            proofOfIdDocList.add(poIdItr.next());
        }
        LOGGER.info(String.format("Count of ProofOfIdDocument(s) found is %d", proofOfIdDocList.size()));
        MongoCollection<ExpenseCategory> expenseCategoryColl = mongoDatabase.getCollection("ExpenseCategory", ExpenseCategory.class);
        Iterable<ExpenseCategory> expenseCategoryItrable=expenseCategoryColl.find(Filters.eq("year", FinancialYear.financialYear()));
        Iterator<ExpenseCategory> expenseCategoryItr=expenseCategoryItrable.iterator();
        expenseCategoryList=new ArrayList<>();
        while(expenseCategoryItr.hasNext()){
            expenseCategoryList.add(expenseCategoryItr.next());
        }
        LOGGER.info(String.format("Count of ExpenseCategory(s) found is %d", expenseCategoryList.size()));
    }
    
    public String validateExpenseParty(){
        FacesContext facesContext=FacesContext.getCurrentInstance();
        ServletContext servletContext= (ServletContext) facesContext.getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        FindIterable<Access> accessItrble=accessColl.find(Filters.eq("email", expensePartyDto.getEmail()));
        if(accessItrble.first()!=null){
            facesContext.addMessage("email",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email registered already.","Email registered already."));
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
        try{
            simpleDateFormat.parse(expensePartyDto.getMemorableDate());
        }catch(ParseException pe){
            facesContext.addMessage("memDate",new FacesMessage(FacesMessage.SEVERITY_ERROR, "format dd/MM/yyyy required.","format dd/MM/yyyy required."));
        }
        //Proof Of Id 
        MongoCollection<ProofOfIdDocument> poIdColl = mongoDatabase.getCollection("ProofOfIdDocument", ProofOfIdDocument.class);
        ProofOfIdDocument proofOfIdDocumentDb = poIdColl.find(Filters.eq("documentType", expensePartyDto.getProofOfIdDocument())).first();
        Pattern p = Pattern.compile(proofOfIdDocumentDb.getRegex());
        Matcher m = p.matcher(expensePartyDto.getDocumentId());
        if(!m.find()){
            facesContext.addMessage("documentId",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Document Id.","Invalid Document Id."));
        }
        
        if (!facesContext.getMessageList().isEmpty()){
            return null;
        }else{
            return "ExpensePartyRegisterConfirm?faces-redirect=true";
        }
      

    }
    
    public String getReturnValue(){
        submitExpenseParty();
        return "/flowreturns/ExpensePartyRegister-return?faces-redirect=true";
    }
    
     private void submitExpenseParty() {
        //Prepare the ExpenseParty Document first from the Dto
         ExpenseParty expenseParty=new ExpenseParty();
         expenseParty.setName(expensePartyDto.getName());
         expenseParty.setEmail(expensePartyDto.getEmail());
         expenseParty.setOrganisation(expensePartyDto.getOrganisation());
         expenseParty.setProofOfIdDocument(expensePartyDto.getProofOfIdDocument());
         expenseParty.setDocumentId(expensePartyDto.getDocumentId());
         StringBuilder partyHashSb=new StringBuilder(expensePartyDto.getName()).append(expensePartyDto.getEmail()).append(expensePartyDto.getDocumentId());
         String partyHash=HashGenerator.generateHash(partyHashSb.toString());
         expenseParty.setPartyHash(partyHash);
         //Submit the EXpenseParty and get its ID
         FacesContext facesContext=FacesContext.getCurrentInstance();
        ServletContext servletContext= (ServletContext) facesContext.getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<ExpenseParty> expensePartyColl = mongoDatabase.getCollection("ExpenseParty", ExpenseParty.class);
        InsertOneResult expensePartyIdResult = expensePartyColl.insertOne(expenseParty);
        LOGGER.info(String.format("ExpenseParty created with Id of %s", expensePartyIdResult.getInsertedId()));
        //Let's do the Expense Accounts now
        //Create Expense Account(s) now
        List<ExpenseAccount> partyExpenseAccounts = new ArrayList<>();
        MongoCollection<ExpenseAccount> expenseAccountColl = mongoDatabase.getCollection("ExpenseAccount", ExpenseAccount.class);
        for(String expAcct : expensePartyDto.getExpenseAccounts()){
            ExpenseAccount ea = new ExpenseAccount();
            ea.setName(expAcct);
            ea.setExpensePartyId(expensePartyIdResult.getInsertedId().asObjectId().getValue());
            ea.setExpenseAccountHash(HashGenerator.generateHash(expensePartyDto.getEmail()+expAcct));
            ea.setCreatedOn(new Date());
            partyExpenseAccounts.add(ea);
        }
        InsertManyResult expenseAccountsIdResult = expenseAccountColl.insertMany(partyExpenseAccounts);
        Map<Integer, BsonValue> expenseAccountsIdMap=expenseAccountsIdResult.getInsertedIds();
        Set<Integer> keySet = expenseAccountsIdMap.keySet();
        for (Integer key: keySet){
            BsonValue bsonValue = expenseAccountsIdMap.get(key);
            LOGGER.info(String.format("ExpenseAccount created with Id of %s", bsonValue));
            //bsonValue.asObjectId().getValue();
        }
        //Next, we need to persist Access
        Access access =new Access();
        access.setEmail(expensePartyDto.getEmail());
        access.setPassword("");
        access.setAccessType(AccessType.ExpenseParty.toString());
        access.setPartyId(expensePartyIdResult.getInsertedId().asObjectId().getValue());
        access.setLastAccessedOn(new Date());
        access.setFailedAttempts(0);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        InsertOneResult accessIdResult=accessColl.insertOne(access);
        LOGGER.info(String.format("Access created with Id of %s", accessIdResult.getInsertedId()));
        //And finally send email to the Party
        
         
    }

    public List<ProofOfIdDocument> getProofOfIdDocList() {
        return proofOfIdDocList;
    }

    public void setProofOfIdDocList(List<ProofOfIdDocument> proofOfIdDocList) {
        this.proofOfIdDocList = proofOfIdDocList;
    }

    public List<ExpenseCategory> getExpenseCategoryList() {
        return expenseCategoryList;
    }

    public void setExpenseCategoryList(List<ExpenseCategory> expenseCategoryList) {
        this.expenseCategoryList = expenseCategoryList;
    }

    public ExpensePartyDto getExpensePartyDto() {
        return expensePartyDto;
    }

    public void setExpensePartyDto(ExpensePartyDto expensePartyDto) {
        this.expensePartyDto = expensePartyDto;
    }

   
    
    
    
}
