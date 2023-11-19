package org.indusbc.dtos;

/**
 *
 * @author singh
 */
public class RevenuePartyDto {
    
    private String name;
    private String email;
    private String organisation;
    private String proofOfIdDocument;
    private String documentId;
    private String[] revenueAccounts;
    private String memorableDate;

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

    public String[] getRevenueAccounts() {
        return revenueAccounts;
    }

    public void setRevenueAccounts(String[] revenueAccounts) {
        this.revenueAccounts = revenueAccounts;
    }

    public String getMemorableDate() {
        return memorableDate;
    }

    public void setMemorableDate(String memorableDate) {
        this.memorableDate = memorableDate;
    }
    
    
    
}
