package org.indusbc.collections;

import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class ProofOfIdDocument {
    
    private ObjectId _id;
    private String documentType;
    private String regex;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
    
    
    
}
