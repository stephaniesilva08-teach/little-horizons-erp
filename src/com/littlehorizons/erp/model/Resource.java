/**
 * Domain model class for Resource.
 */
package com.littlehorizons.erp.model;

public class Resource {

    
    private int idResource;

    
    private String resourceCode;          
    private String name;                  
    private int idCategory;               
    private String description;           

    
    private String acquisitionDate;       

    
    private String location;              

    
    private String state;                 

    
    private String creationDate;
    private String categoryName;
    private String updateDate;

    
    public Resource() {
    }

    
    public Resource(int idResource) {
        this.idResource = idResource;
    }

    
    public Resource(int idResource, String resourceCode, String name,
                    int idCategory, String description,
                    String acquisitionDate, String location, String state,
                    String creationDate, String updateDate) {
        this.idResource = idResource;
        this.resourceCode = resourceCode;
        this.name = name;
        this.idCategory = idCategory;
        this.description = description;
        this.acquisitionDate = acquisitionDate;
        this.location = location;
        this.state = state;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    
    
    

    public int getIdResource() { return idResource; }

    public void setIdResource(int idResource) { this.idResource = idResource; }

    public String getResourceCode() { return resourceCode; }

    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getIdCategory() { return idCategory; }

    public void setIdCategory(int idCategory) { this.idCategory = idCategory; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getAcquisitionDate() { return acquisitionDate; }

    public void setAcquisitionDate(String acquisitionDate) { this.acquisitionDate = acquisitionDate; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getCreationDate() { return creationDate; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getUpdateDate() { return updateDate; }

    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    
    @Override
    public String toString() {
        return "Resource{" +
                "idResource=" + idResource +
                ", code='" + resourceCode + '\'' +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Resource that)) return false;
        return idResource > 0 && idResource == that.idResource;
    }

    @Override
    public int hashCode() {
        return idResource > 0 ? Integer.hashCode(idResource)
                : System.identityHashCode(this);
    }
}
