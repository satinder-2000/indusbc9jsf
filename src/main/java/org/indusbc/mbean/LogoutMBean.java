package org.indusbc.mbean;

import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.indusbc.collections.Access;
import org.indusbc.util.AccessType;

/**
 *
 * @author singh
 */
@Named(value = "logoutMBean")
public class LogoutMBean {
    
    public String canceLogout(){
        String toReturn=null;
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession(false);
        Access access =(Access) session.getAttribute("access");
        if(access.getAccessType().equals(AccessType.EXPENSE_PARTY.getShortName())){
            toReturn = "/home/ExpensePartyHome?faces-redirect=true";
        }else if(access.getAccessType().equals(AccessType.REVENUE_PARTY.getShortName())){
            toReturn = "/home/RevenuePartyHome?faces-redirect=true";
        } 
        return toReturn;
    }
    
    public String logout(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession(false);
        session.removeAttribute("access");
        return "/LogoutConfirm";
    }
    
}
