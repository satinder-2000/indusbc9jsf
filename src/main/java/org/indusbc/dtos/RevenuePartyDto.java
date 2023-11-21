package org.indusbc.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 *
 * @author singh
 */
public class RevenuePartyDto {
    
    @NotBlank
    @Size(min = 2, max = 90, message = "Name must be 2-90 chars long")
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 2, max = 90, message = "Organisation Name must be 2-90 chars long")
    private String organisation;
    private String proofOfIdDocument;
    @NotBlank
    @Size(min = 8, max = 16, message = "Ducument Id must be 8-16 chars long")
    private String documentId;
    private String[] revenueAccounts;
    @NotBlank
    private String memorableDate;
    
    private String partyHash;

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

    public String getPartyHash() {
        return partyHash;
    }

    public void setPartyHash(String partyHash) {
        this.partyHash = partyHash;
    }
    
    
    
}
