/**
 * Domain model class for PaymentConcept.
 */
package com.littlehorizons.erp.model;

import java.math.BigDecimal;

public class PaymentConcept {

    
    private int idConcept;

    
    private String conceptCode;         
    private String name;                
    private String description;         

    
    private BigDecimal baseAmount;      

    
    private boolean isRecurring;        

    
    private boolean isActive;           

    
    private String creationDate;
    private String updateDate;

    
    public PaymentConcept() {
    }

    
    public PaymentConcept(int idConcept) {
        this.idConcept = idConcept;
    }

    
    public PaymentConcept(int idConcept, String conceptCode, String name,
                          String description, BigDecimal baseAmount,
                          boolean isRecurring, boolean isActive,
                          String creationDate, String updateDate) {
        this.idConcept = idConcept;
        this.conceptCode = conceptCode;
        this.name = name;
        this.description = description;
        this.baseAmount = baseAmount;
        this.isRecurring = isRecurring;
        this.isActive = isActive;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdConcept() { return idConcept; }

    public void setIdConcept(int idConcept) { this.idConcept = idConcept; }

    public String getConceptCode() { return conceptCode; }

    public void setConceptCode(String conceptCode) { this.conceptCode = conceptCode; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBaseAmount() { return baseAmount; }

    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public boolean isRecurring() { return isRecurring; }

    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    @Override
    public String toString() {
        return "PaymentConcept{" +
                "idConcept=" + idConcept +
                ", code='" + conceptCode + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + baseAmount +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PaymentConcept that)) return false;
        return idConcept > 0 && idConcept == that.idConcept;
    }

    @Override
    public int hashCode() {
        return idConcept > 0 ? Integer.hashCode(idConcept)
                : System.identityHashCode(this);
    }
}
