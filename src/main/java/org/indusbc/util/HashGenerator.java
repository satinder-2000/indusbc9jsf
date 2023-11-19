package org.indusbc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 *
 * @author singh
 */
public class HashGenerator {
    
    private static final Logger LOGGER=Logger.getLogger(HashGenerator.class.getName());
    
    public static String generateHash(String dataToHash) {
	MessageDigest digest = null;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
        	LOGGER.severe(ex.getMessage());
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
       LOGGER.info(sb.toString());
       return sb.toString();
	}
    
}
