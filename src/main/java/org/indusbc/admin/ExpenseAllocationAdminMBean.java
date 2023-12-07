package org.indusbc.admin;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.indusbc.collections.ExpenseAllocation;

/**
 *
 * @author singh
 */
@Named(value = "expenseAllocationAdminMBean")
@ViewScoped
public class ExpenseAllocationAdminMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(ExpenseAllocationAdminMBean.class.getName());
    
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    
    private int year;
    private Part expenseAllocationFile;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
    }
    
    public String updateExpenseAllocations() throws IOException{
        Bson filter=Filters.eq("year", year);
        MongoCollection<ExpenseAllocation> expenseAllocationCol=mongoDatabase.getCollection("ExpenseAllocation", ExpenseAllocation.class);
        DeleteResult deleteResult=expenseAllocationCol.deleteMany(filter);
        LOGGER.info(String.format("ExpenseAllocation collection refreashed by deleting %d records.", deleteResult.getDeletedCount()));
        List<ExpenseAllocation> newExpAllocs=new ArrayList<>();
        Reader reader = new InputStreamReader(expenseAllocationFile.getInputStream());
        BufferedReader bufferedReader= new BufferedReader(reader);
        String fileLine=null;
        //skip the header line
        bufferedReader.readLine();
        while((fileLine=bufferedReader.readLine())!=null){
            StringTokenizer st = new StringTokenizer(fileLine, ",");
            ExpenseAllocation ral = new ExpenseAllocation();
            ral.setYear(Integer.parseInt(st.nextToken()));
            ral.setExpenseCategory(st.nextToken());
            ral.setAllocation(st.nextToken());
            ral.setPercentAllocation(st.nextToken());
            newExpAllocs.add(ral);
        }
        LOGGER.info(String.format("Count of new ExpenseAllocations is %d", newExpAllocs.size()));
        expenseAllocationCol.insertMany(newExpAllocs);
        return null;
    }
    

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Part getExpenseAllocationFile() {
        return expenseAllocationFile;
    }

    public void setExpenseAllocationFile(Part expenseAllocationFile) {
        this.expenseAllocationFile = expenseAllocationFile;
    }
    
    
    
}
