package com.shadsluiter.eventsapp.models; 

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class EventModel {

    private String id;
    
    private String name;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    
    private String location;
    
    private String organizerid;
    
    private String description;

    public EventModel() {}

    public EventModel(String id, String name, Date date, String location, String organizerid, String description) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.organizerid = organizerid;
        this.description = description;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrganizerid() {
        return organizerid;
    }

    public void setOrganizerid(String organizerid) {
        this.organizerid = organizerid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
