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
import jakarta.inject.Inject;
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
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.indusbc.collections.Access;
import org.indusbc.collections.ExpenseAccount;
import org.indusbc.collections.ExpenseCategory;
import org.indusbc.collections.ExpenseParty;
import org.indusbc.collections.ProofOfIdDocument;
import org.indusbc.collections.RevenueAccount;
import org.indusbc.collections.RevenueCategory;
import org.indusbc.collections.RevenueParty;
import org.indusbc.dtos.RevenuePartyDto;
import org.indusbc.util.AccessType;
import org.indusbc.util.FinancialYear;
import org.indusbc.util.HashGenerator;

/**
 *
 * @author singh
 */
@Named(value = "revenuePartyRegisterMBean")
@FlowScoped(value = "RevenuePartyRegister")
public class RevenuePartyRegisterMBean implements Serializable {
    
    private static final Logger LOGGER =Logger.getLogger(RevenuePartyRegisterMBean.class.getName());
    private RevenuePartyDto revenuePartyDto;
    private List<ProofOfIdDocument> proofOfIdDocList;
    private List<RevenueCategory> revenueCategoryList;
    
    @PostConstruct
    public void init(){
        revenuePartyDto=new RevenuePartyDto();
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
        MongoCollection<RevenueCategory> revenueCategoryColl = mongoDatabase.getCollection("RevenueCategory", RevenueCategory.class);
        Iterable<RevenueCategory> revenueCategoryItrable=revenueCategoryColl.find(Filters.eq("year", FinancialYear.financialYear()));
        Iterator<RevenueCategory> revenueCategoryItr=revenueCategoryItrable.iterator();
        revenueCategoryList=new ArrayList<>();
        while(revenueCategoryItr.hasNext()){
            revenueCategoryList.add(revenueCategoryItr.next());
        }
        LOGGER.info(String.format("Count of RevenueCategory(s) found is %d", revenueCategoryList.size()));
        
    }
    
    public String validateRevenueParty(){
        FacesContext facesContext=FacesContext.getCurrentInstance();
        ServletContext servletContext= (ServletContext) facesContext.getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        FindIterable<Access> accessItrble=accessColl.find(Filters.eq("email", revenuePartyDto.getEmail()));
        if(accessItrble.first()!=null){
            facesContext.addMessage("email",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email registered already.","Email registered already."));
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
        try{
            simpleDateFormat.parse(revenuePartyDto.getMemorableDate());
        }catch(ParseException pe){
            facesContext.addMessage("memDate",new FacesMessage(FacesMessage.SEVERITY_ERROR, "format dd/MM/yyyy required.","format dd/MM/yyyy required."));
        }
        //Proof Of Id 
        MongoCollection<ProofOfIdDocument> poIdColl = mongoDatabase.getCollection("ProofOfIdDocument", ProofOfIdDocument.class);
        ProofOfIdDocument proofOfIdDocumentDb = poIdColl.find(Filters.eq("documentType", revenuePartyDto.getProofOfIdDocument())).first();
        Pattern p = Pattern.compile(proofOfIdDocumentDb.getRegex());
        Matcher m = p.matcher(revenuePartyDto.getDocumentId());
        if(!m.find()){
            facesContext.addMessage("documentId",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Document Id.","Invalid Document Id."));
        }
        
        if (!facesContext.getMessageList().isEmpty()){
            return null;
        }else{
            return "RevenuePartyRegisterConfirm?faces-redirect=true";
        }
    }
    
    public String amendRevenueParty(){
        return "RevenuePartyRegisterAmend?faces-redirect=true";
    }
    
    
    
    private void submitRevenueParty(){
        //Prepare the ExpenseParty Document first from the Dto
        RevenueParty revenueParty=new RevenueParty();
        revenueParty.setName(revenuePartyDto.getName());
        revenueParty.setEmail(revenuePartyDto.getEmail());
        revenueParty.setOrganisation(revenuePartyDto.getOrganisation());
        revenueParty.setProofOfIdDocument(revenuePartyDto.getProofOfIdDocument());
        revenueParty.setDocumentId(revenuePartyDto.getDocumentId());
         StringBuilder partyHashSb=new StringBuilder(revenuePartyDto.getName()).append(revenuePartyDto.getEmail()).append(revenuePartyDto.getDocumentId());
         String partyHash=HashGenerator.generateHash(partyHashSb.toString());
         revenueParty.setPartyHash(partyHash);
         //Submit the EXpenseParty and get its ID
         FacesContext facesContext=FacesContext.getCurrentInstance();
        ServletContext servletContext= (ServletContext) facesContext.getExternalContext().getContext();
        MongoClient mongoClient = (MongoClient)servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        MongoDatabase mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<RevenueParty> revenuePartyColl = mongoDatabase.getCollection("RevenueParty", RevenueParty.class);
        InsertOneResult revenuePartyIdResult = revenuePartyColl.insertOne(revenueParty);
        LOGGER.info(String.format("ExpenseParty created with Id of %s", revenuePartyIdResult.getInsertedId()));
        //Let's do the Revenue Accounts now
        //Create Revenue Account(s) now
        List<RevenueAccount> partyRevenueAccounts = new ArrayList<>();
        MongoCollection<RevenueAccount> revenueAccountColl = mongoDatabase.getCollection("RevenueAccount", RevenueAccount.class);
        for(String revAcct : revenuePartyDto.getRevenueAccounts()){
            RevenueAccount ra = new RevenueAccount();
            ra.setName(revAcct);
            ra.setRevenuePartyId(revenuePartyIdResult.getInsertedId().asObjectId().getValue());
            ra.setRevenueAccountHash(HashGenerator.generateHash(revenuePartyDto.getEmail()+revAcct));
            ra.setCreatedOn(new Date());
            ra.setYtdBalance("0");
            partyRevenueAccounts.add(ra);
        }
        InsertManyResult revenueAccountsIdResult = revenueAccountColl.insertMany(partyRevenueAccounts);
        Map<Integer, BsonValue> revenueAccountsIdMap=revenueAccountsIdResult.getInsertedIds();
        Set<Integer> keySet = revenueAccountsIdMap.keySet();
        for (Integer key: keySet){
            BsonValue bsonValue = revenueAccountsIdMap.get(key);
            LOGGER.info(String.format("RevenueAccount created with Id of %s", bsonValue));
            //bsonValue.asObjectId().getValue();
        }
        //Next, we need to persist Access
        Access access =new Access();
        access.setEmail(revenuePartyDto.getEmail());
        access.setPassword("");
        access.setAccessType(AccessType.RevenueParty.toString());
        access.setPartyId(revenuePartyIdResult.getInsertedId().asObjectId().getValue());
        access.setLastAccessedOn(new Date());
        access.setFailedAttempts(0);
        MongoCollection<Access> accessColl = mongoDatabase.getCollection("Access", Access.class);
        InsertOneResult accessIdResult=accessColl.insertOne(access);
        LOGGER.info(String.format("Access created with Id of %s", accessIdResult.getInsertedId()));
        //And finally send email to the Party
    }
    
    public String getReturnValue(){
        submitRevenueParty();
        return "/flowreturns/RevenuePartyRegister-return?faces-redirect=true";
    }

    public RevenuePartyDto getRevenuePartyDto() {
        return revenuePartyDto;
    }

    public void setRevenuePartyDto(RevenuePartyDto revenuePartyDto) {
        this.revenuePartyDto = revenuePartyDto;
    }

    public List<ProofOfIdDocument> getProofOfIdDocList() {
        return proofOfIdDocList;
    }

    public void setProofOfIdDocList(List<ProofOfIdDocument> proofOfIdDocList) {
        this.proofOfIdDocList = proofOfIdDocList;
    }

    public List<RevenueCategory> getRevenueCategoryList() {
        return revenueCategoryList;
    }

    public void setRevenueCategoryList(List<RevenueCategory> revenueCategoryList) {
        this.revenueCategoryList = revenueCategoryList;
    }
    
    
    
    
}
