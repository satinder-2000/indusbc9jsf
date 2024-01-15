package org.indusbc.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.indusbc.collections.Access;
import org.indusbc.util.AccessType;

/**
 *
 * @author singh
 */
@Stateless
public class EmailEjb implements EmailEjbLocal {
    
    private static final Logger LOGGER = Logger.getLogger(EmailEjb.class.getName());
    private static final String HEADER_HTML_EMAIL = "text/html; charset=UTF-8";
    private static final String TEMPLATE_DIRECTORY = "templates/";
    private static final String VELOCITY_RESOURCE_CLASS_LOADER_KEY = "resource.loader.class.class";
    private static final String VELOCITY_RESOURCE_CLASS_LOADER = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
    private static final String VELOCITY_RESOURCE_LOADER_KEY = "resource.loaders";
    private static final String VELOCITY_RESOURCE_LOADER = "class";

    @Resource(mappedName = "java:comp/env/mail/indusbc")
    private Session mailSession;
    
    @Resource(name = "webURI")
    private String webURI;
    
    @Resource(name = "accessCreateURI")
    private String accessCreateURI;
    
    private VelocityEngine velocityEngine;
    
    @PostConstruct
    public void init(){
        /**/
        final Properties prop = new Properties();
        prop.setProperty(VELOCITY_RESOURCE_LOADER_KEY, VELOCITY_RESOURCE_LOADER);
        prop.setProperty(VELOCITY_RESOURCE_CLASS_LOADER_KEY, VELOCITY_RESOURCE_CLASS_LOADER);
        velocityEngine = new VelocityEngine();
        velocityEngine.init(prop);
        
        final String username=mailSession.getProperty("mail.smtp.user");
        final String password=mailSession.getProperty("mail.smtp.password");
        
        final URLName url= new URLName(mailSession.getProperty("mail.transport.protocol"), mailSession.getProperty("mail.smtp.host"),
                -1, null, username, null);
        
        mailSession.setPasswordAuthentication(url, new PasswordAuthentication(username, password));
        LOGGER.info("MailSession set successfully!!");
        
    }
    @Override
    public void sendEmail(Access access) {
        MimeMessage mimeMessage = new MimeMessage(mailSession);
        Multipart multipart = new MimeMultipart();
        StringBuilder htmlMsg = new StringBuilder("<html><body>");
        htmlMsg.append("<h2>Dear, ").append(access.getEmail()).append("</h2>");
        AccessType accessType = AccessType.getByShortName(access.getAccessType());
        htmlMsg.append("<p>Congratulations on registering yourself successfully as ").append(accessType.getName()).append(".</p>");
        htmlMsg.append("<p>As a final step, please create your account password by following the link below:</p>");
        String accessCreate=String.format(accessCreateURI, access.getEmail(), accessType.getShortName());
        htmlMsg.append("<a href=\"").append(webURI).append(accessCreate).append("\">")
                .append(webURI).append(accessCreate)
                .append("</a>");
        //htmlMsg.append("<p>"+accessCreate+"</p>");
        htmlMsg.append("<p>Best Wishes, <br/>www.indusbc.org Admin</p>");
        htmlMsg.append("</body></html>");
        MimeBodyPart htmlPart = new MimeBodyPart();
        try {
            htmlPart.setContent( htmlMsg.toString(), "text/html; charset=utf-8" );
            multipart.addBodyPart(htmlPart);
            mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(access.getEmail()));
            mimeMessage.setContent(multipart);
            mimeMessage.setSubject(accessType.getName()+" Registration");
            Transport.send(mimeMessage);
            LOGGER.info("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.severe(ex.getMessage());
        }
        
        /*String to="satinder_2000@outlook.com";
        String from="ssingh.2023@gmail.com";
        MimeMessage message = new MimeMessage(mailSession);
        try {
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Message from Jakarta Mail");
            message.setText("Attmpting Jakarta Emailing");
            
            Transport.send(message);
            System.out.println("Email sent successfully!!");
        } catch (AddressException ex) {
            Logger.getLogger(EmailEjb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(EmailEjb.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

}
