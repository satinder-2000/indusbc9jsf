package org.indusbc.admin;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 *
 * @author singh
 */
@Named(value = "adminLoginMBean")
@ViewScoped
public class AdminLoginMBean implements Serializable {
    
    private String email;
    private String password;
    
    public String adminLogin(){
        if (email.equals("admin@indusbc.org") && (password.equals("Wether@lDr69"))){
            return "/admin/adminHome?faces-redirect=true";
        }else{
           return "/admin/adminLoginError?faces-redirect=true"; 
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
    
}
