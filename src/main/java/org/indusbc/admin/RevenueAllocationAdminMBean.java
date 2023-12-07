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
import org.indusbc.collections.ExpenseCategory;
import org.indusbc.collections.RevenueAllocation;

/**
 *
 * @author singh
 */
@Named(value = "revenueAllocationAdminMBean")
@ViewScoped
public class RevenueAllocationAdminMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(RevenueAllocationAdminMBean.class.getName());
    
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    
    private int year;
    private Part revenueAllocationFile;
    
    @PostConstruct
    public void init(){
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
    }
    
    public String updateRevenueAllocations() throws IOException{
        Bson filter=Filters.eq("year", year);
        MongoCollection<RevenueAllocation> revenueAllocationCol=mongoDatabase.getCollection("RevenueAllocation", RevenueAllocation.class);
        DeleteResult deleteResult=revenueAllocationCol.deleteMany(filter);
        LOGGER.info(String.format("RevenueAllocation collection refreashed by deleting %d records.", deleteResult.getDeletedCount()));
        List<RevenueAllocation> newRevAllocs=new ArrayList<>();
        Reader reader = new InputStreamReader(revenueAllocationFile.getInputStream());
        BufferedReader bufferedReader= new BufferedReader(reader);
        String fileLine=null;
        //skip the header line
        bufferedReader.readLine();
        while((fileLine=bufferedReader.readLine())!=null){
            StringTokenizer st = new StringTokenizer(fileLine, ",");
            RevenueAllocation ral = new RevenueAllocation();
            ral.setYear(Integer.parseInt(st.nextToken()));
            ral.setRevenueCategory(st.nextToken());
            ral.setAllocation(st.nextToken());
            ral.setPercentAllocation(st.nextToken());
            newRevAllocs.add(ral);
        }
        LOGGER.info(String.format("Count of new RevenueAllocations is %d", newRevAllocs.size()));
        revenueAllocationCol.insertMany(newRevAllocs);
        return null;
    }
    

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Part getRevenueAllocationFile() {
        return revenueAllocationFile;
    }

    public void setRevenueAllocationFile(Part revenueAllocationFile) {
        this.revenueAllocationFile = revenueAllocationFile;
    }
    
    
    
}
