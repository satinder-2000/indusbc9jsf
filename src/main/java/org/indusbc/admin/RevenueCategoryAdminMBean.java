package org.indusbc.admin;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
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
import org.indusbc.collections.RevenueCategory;

/**
 *
 * @author singh
 */
@Named(value = "revenueCategoryAdminMBean")
@ViewScoped
public class RevenueCategoryAdminMBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(RevenueCategoryAdminMBean.class.getName());
    
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    
    private int year;
    private Part revenueCategoryFile;

    @PostConstruct
    public void init(){
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        mongoClient=(MongoClient) servletContext.getAttribute("mongoClient");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(pojoCodecProvider));
        mongoDatabase=mongoClient.getDatabase(servletContext.getInitParameter("MONGODB_DB")).withCodecRegistry(pojoCodecRegistry);
    }
    
    public String updateRevenueCategories() throws IOException{
        Bson filter=Filters.eq("year", year);
        MongoCollection<RevenueCategory> revenueCategoryCol=mongoDatabase.getCollection("RevenueCategory", RevenueCategory.class);
        DeleteResult deleteResult=revenueCategoryCol.deleteMany(filter);
        LOGGER.info(String.format("RevenueCategory collection refreashed by deleting %d records.", deleteResult.getDeletedCount()));
        List<RevenueCategory> newRevCats=new ArrayList<>();
        Reader reader = new InputStreamReader(revenueCategoryFile.getInputStream());
        BufferedReader bufferedReader= new BufferedReader(reader);
        String fileLine=null;
        //skip the header line
        bufferedReader.readLine();
        while((fileLine=bufferedReader.readLine())!=null){
            StringTokenizer st = new StringTokenizer(fileLine, ",");
            RevenueCategory rc= new RevenueCategory();
            
            rc.setYear(Integer.parseInt(st.nextToken()));
            rc.setRevenueCategory(st.nextToken());
            newRevCats.add(rc);
        }
        LOGGER.info(String.format("Count of new RevenueCategories is %d", newRevCats.size()));
        revenueCategoryCol.insertMany(newRevCats);
        return null;
    }
    
    

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Part getRevenueCategoryFile() {
        return revenueCategoryFile;
    }

    public void setRevenueCategoryFile(Part revenueCategoryFile) {
        this.revenueCategoryFile = revenueCategoryFile;
    }
    
    
    
    
}
