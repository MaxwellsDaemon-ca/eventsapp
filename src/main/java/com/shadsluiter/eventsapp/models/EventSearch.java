package com.shadsluiter.eventsapp.models;

public class EventSearch {
    private String searchString;

    public EventSearch() {
        searchString = "";
    }

    public EventSearch(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }  
}
