package org.indusbc.ejbs;

import jakarta.ejb.Local;
import org.indusbc.collections.Access;

/**
 *
 * @author singh
 */
@Local
public interface EmailEjbLocal {
    
    public void sendEmail(Access access);
    
}
