package org.indusbc.listeners;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 *
 * @author singh
 */
@WebListener
public class IndusbcContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext=sce.getServletContext();
        String connectionURL=servletContext.getInitParameter("MONGODB_URL");
        MongoClient mongoClient= MongoClients.create(connectionURL);
        servletContext.setAttribute("mongoClient", mongoClient);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        MongoClient mongoClient=(MongoClient)servletContext.getAttribute("mongoClient");
        mongoClient.close();
    }

    
    
}
