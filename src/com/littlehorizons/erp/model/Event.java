/**
 * Domain model class for Event.
 */
package com.littlehorizons.erp.model;

public class Event {

    
    private int idEvent;

    
    private String name;                
    private String description;         

    
    private String eventType;           

    
    private String eventDate;           

    
    private String place;               

    
    private boolean hasCompetition;     

    
    private int organizerId;            

    
    private boolean isActive;           

    
    private String creationDate;
    private String organizerName;

    
    public Event() {
    }

    
    public Event(int idEvent) {
        this.idEvent = idEvent;
    }

    
    public Event(int idEvent, String name, String description,
                 String eventType, String eventDate, String place,
                 boolean hasCompetition, int organizerId, boolean isActive,
                 String creationDate) {
        this.idEvent = idEvent;
        this.name = name;
        this.description = description;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.place = place;
        this.hasCompetition = hasCompetition;
        this.organizerId = organizerId;
        this.isActive = isActive;
        this.creationDate = creationDate;
    }

    
    
    

    public int getIdEvent() { return idEvent; }

    public void setIdEvent(int idEvent) { this.idEvent = idEvent; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getEventType() { return eventType; }

    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventDate() { return eventDate; }

    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getPlace() { return place; }

    public void setPlace(String place) { this.place = place; }

    public boolean isHasCompetition() { return hasCompetition; }

    public void setHasCompetition(boolean hasCompetition) { this.hasCompetition = hasCompetition; }

    public int getOrganizerId() { return organizerId; }

    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public boolean isActive() { return isActive; }

    public void setActive(boolean isActive) { this.isActive = isActive; }

    public String getCreationDate() { return creationDate; }
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    
    @Override
    public String toString() {
        return "Event{" +
                "idEvent=" + idEvent +
                ", name='" + name + '\'' +
                ", eventType='" + eventType + '\'' +
                ", date='" + eventDate + '\'' +
                '}';
    }

    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Event)) return false;
        Event that = (Event) other;
        return name != null && name.equals(that.name)
                && eventDate.equals(that.eventDate);
    }
}
