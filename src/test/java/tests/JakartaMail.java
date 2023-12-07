package tests;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 *
 * @author singh
 */
public class JakartaMail {
    
    public static void main(String[] args){
        String to="satinder_2000@outlook.com";
        String from="ssingh.2023@gmail.com";
        //final String username="8539c5a2ac4063";
        //final String password="3fc522a638acf1";
        //final String username="ssingh.2023@gmail.com";
        //final String password="vsfkgalaxzesolsd";
        //String host="smtp.gmail.com";
        final String username="lorenz61@ethereal.email";
        final String password="cPwd9zNyhpVQR6qp3G";
        String host="smtp.ethereal.email";
        
        Properties prop =new Properties();
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", true);
        prop.put("mail.smtp.port", "587");
        
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(username,password);
            }
        };
        
        Session session = Session.getDefaultInstance(prop, authenticator);
        
        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Message from Jakarta Mail");
            message.setText("Attmpting Jakarta Emailing");
            
            Transport.send(message);
            System.out.println("Email sent successfully!!");
        }catch(MessagingException ex){
            ex.printStackTrace();
        }
    }
    
}
