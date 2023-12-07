package org.indusbc.ejbs;

import jakarta.ejb.AccessTimeout;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Local;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import java.util.concurrent.Future;


/**
 *
 * @author singh
 */
@Local
public interface AllocationEjbLocal {
    
    @Asynchronous
    @Lock(LockType.READ)
    @AccessTimeout(-1)
    public Future<String> performAllocations(String allocationJobName, String granularity, int year);
    
}
