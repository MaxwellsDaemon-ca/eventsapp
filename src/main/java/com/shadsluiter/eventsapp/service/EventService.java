package com.shadsluiter.eventsapp.service;

import com.shadsluiter.eventsapp.data.EventRepositoryInterface;
import com.shadsluiter.eventsapp.models.EventEntity;
import com.shadsluiter.eventsapp.models.EventModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing event-related operations.
 * 
 * Handles business logic and model/entity conversions for Event data.
 */
@Service
public class EventService {

    private final EventRepositoryInterface eventRepository;

    /**
     * Constructs the EventService with a repository implementation.
     * 
     * @param eventRepository the repository interface for Event persistence
     */
    @Autowired
    public EventService(EventRepositoryInterface eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Retrieves all events from the database.
     * 
     * @return a list of EventModel objects
     */
    public List<EventModel> findAll() {
        List<EventEntity> eventEntities = eventRepository.findAll();
        return convertToModels(eventEntities);
    }

    /**
     * Retrieves events for a specific organizer.
     * 
     * @param organizerid the ID of the organizer
     * @return a list of EventModel objects for the given organizer
     */
    public List<EventModel> findByOrganizerid(String organizerid) {
        List<EventEntity> eventEntities = eventRepository.findByOrganizerid(Long.valueOf(organizerid));
        return convertToModels(eventEntities);
    }

    /**
     * Saves a new or existing event to the database.
     * 
     * @param event the event to be saved
     * @return the saved EventModel
     */
    public EventModel save(EventModel event) {
        EventEntity eventEntity = convertToEntity(event);
        EventEntity savedEvent = eventRepository.save(eventEntity);
        return convertToModel(savedEvent);
    }

    /**
     * Deletes an event by its ID.
     * 
     * @param id the ID of the event to delete
     */
    public void delete(String id) {
        eventRepository.deleteById(Long.valueOf(id));
    }

    /**
     * Updates an existing event based on its ID.
     * 
     * @param id the ID of the event to update
     * @param event the new event data
     * @return the updated EventModel
     */
    public EventModel updateEvent(String id, EventModel event) {
        event.setId(id);
        return save(event);
    }

    /**
     * Finds an event by its ID.
     * 
     * @param id the ID of the event
     * @return the corresponding EventModel
     */
    public EventModel findById(String id) {
        EventEntity eventEntity = eventRepository.findById(Long.valueOf(id));
        return convertToModel(eventEntity);
    }

    /**
     * Searches for events whose descriptions match the given search string.
     * 
     * @param searchString the string to search for in descriptions
     * @return a list of matching EventModel objects
     */
    public List<EventModel> findByDescription(String searchString) {
        List<EventEntity> eventEntities = eventRepository.findByDescription(searchString);
        return convertToModels(eventEntities);  
    }

    /**
     * Converts a list of EventEntity objects into a list of EventModel objects.
     * 
     * @param eventEntities the list of EventEntity objects
     * @return a list of EventModel objects
     */
    private List<EventModel> convertToModels(List<EventEntity> eventEntities) {
        List<EventModel> eventModels = new ArrayList<>();
        for (EventEntity eventEntity : eventEntities) {
            eventModels.add(convertToModel(eventEntity));
        }
        return eventModels;
    }

    /**
     * Converts an EventEntity object to an EventModel.
     * 
     * @param eventEntity the EventEntity to convert
     * @return the resulting EventModel
     */
    private EventModel convertToModel(EventEntity eventEntity) {
        return new EventModel(
            eventEntity.getId().toString(),
            eventEntity.getName(),
            eventEntity.getDate(),
            eventEntity.getLocation(),
            eventEntity.getOrganizerid(),
            eventEntity.getDescription()
        );
    }

    /**
     * Converts an EventModel object to an EventEntity.
     * 
     * @param eventModel the EventModel to convert
     * @return the resulting EventEntity
     */
    private EventEntity convertToEntity(EventModel eventModel) {
        EventEntity eventEntity = new EventEntity();

        if (eventModel.getId() != null) {
            eventEntity.setId(Long.parseLong(eventModel.getId()));
        }

        eventEntity.setName(eventModel.getName());
        eventEntity.setDate(new Date(eventModel.getDate().getTime()));
        eventEntity.setLocation(eventModel.getLocation());
        eventEntity.setOrganizerid(eventModel.getOrganizerid());
        eventEntity.setDescription(eventModel.getDescription());

        return eventEntity;
    }
}
