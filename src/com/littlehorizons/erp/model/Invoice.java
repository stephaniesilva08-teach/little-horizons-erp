/**
 * Domain model class for Invoice.
 */
package com.littlehorizons.erp.model;

import java.math.BigDecimal;

public class Invoice {

    
    private int idInvoice;

    
    private String invoiceNumber;           
    private int idPayment;                  

    
    private String issueDate;               

    
    private String cufe;                    
    private String txtFileName;             
    private String txtPath;                 

    
    private String creationDate;

    
    public Invoice() {
    }

    
    public Invoice(int idInvoice) {
        this.idInvoice = idInvoice;
    }

    
    public Invoice(int idInvoice, String invoiceNumber, int idPayment,
                   String issueDate, String cufe, String txtFileName,
                   String txtPath, String creationDate) {
        this.idInvoice = idInvoice;
        this.invoiceNumber = invoiceNumber;
        this.idPayment = idPayment;
        this.issueDate = issueDate;
        this.cufe = cufe;
        this.txtFileName = txtFileName;
        this.txtPath = txtPath;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdInvoice() { return idInvoice; }

    public void setIdInvoice(int idInvoice) { this.idInvoice = idInvoice; }

    public String getInvoiceNumber() { return invoiceNumber; }

    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public int getIdPayment() { return idPayment; }

    public void setIdPayment(int idPayment) { this.idPayment = idPayment; }

    public String getIssueDate() { return issueDate; }

    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getCufe() { return cufe; }

    public void setCufe(String cufe) { this.cufe = cufe; }

    public String getTxtFileName() { return txtFileName; }

    public void setTxtFileName(String txtFileName) { this.txtFileName = txtFileName; }

    public String getTxtPath() { return txtPath; }

    public void setTxtPath(String txtPath) { this.txtPath = txtPath; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Invoice{" +
                "idInvoice=" + idInvoice +
                ", number='" + invoiceNumber + '\'' +
                ", paymentId=" + idPayment +
                ", cufe='" + cufe + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Invoice)) return false;
        Invoice that = (Invoice) other;
        return invoiceNumber != null && invoiceNumber.equals(that.invoiceNumber);
    }
}
