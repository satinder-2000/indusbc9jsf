package org.indusbc.mbean;

import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author singh
 */
@Named(value = "logoutMBean")
public class LogoutMBean {
    
    public String logout(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession(false);
        session.removeAttribute("access");
        return "/LogoutConfirm?faces-redirect=true";
    }
    
}
