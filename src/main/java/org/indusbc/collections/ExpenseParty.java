package org.indusbc.collections;

import java.util.Date;
import org.bson.types.ObjectId;

/**
 *
 * @author singh
 */
public class ExpenseParty {
    
    private ObjectId _id;
    private String name;
    private String email;
    private String organisation;
    private String proofOfIdDocument;
    private String documentId;
    private String memorableDate;
    private String partyHash;
    private String password;
    private Date createdOn;

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getProofOfIdDocument() {
        return proofOfIdDocument;
    }

    public void setProofOfIdDocument(String proofOfIdDocument) {
        this.proofOfIdDocument = proofOfIdDocument;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getMemorableDate() {
        return memorableDate;
    }

    public void setMemorableDate(String memorableDate) {
        this.memorableDate = memorableDate;
    }

    public String getPartyHash() {
        return partyHash;
    }

    public void setPartyHash(String partyHash) {
        this.partyHash = partyHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
    
    
    
}
