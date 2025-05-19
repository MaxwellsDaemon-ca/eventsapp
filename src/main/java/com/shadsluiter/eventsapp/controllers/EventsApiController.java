package com.shadsluiter.eventsapp.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.shadsluiter.eventsapp.models.EventModel;
import com.shadsluiter.eventsapp.models.UserModel;
import com.shadsluiter.eventsapp.service.EventService;
import com.shadsluiter.eventsapp.service.UserService;

/**
 * REST API controller for managing event data.
 * 
 * Exposes endpoints for creating, retrieving, updating, and deleting events via JSON.
 * Primarily used for interaction with frontend or external clients over HTTP.
 */
@RestController
@RequestMapping("/api/events")
public class EventsApiController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    /**
     * Creates a new event associated with the authenticated user.
     * Defaults to fallback values if any key fields are missing.
     * 
     * @param event the event data from the client
     * @param authentication the authenticated user's credentials
     * @return the created event wrapped in a response
     */
    @PostMapping
    public ResponseEntity<EventModel> createEvent(@RequestBody EventModel event, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserModel user = userService.findByLoginName(userDetails.getUsername());

        event.setOrganizerid(user.getId());

        if (event.getDate() == null) {
            event.setDate(new Date());
        }

        if (event.getName() == null || event.getName().isEmpty()) {
            event.setName("New Event");
        }

        if (event.getDescription() == null || event.getDescription().isEmpty()) {
            event.setDescription("No description provided");
        }

        if (event.getLocation() == null || event.getLocation().isEmpty()) {
            event.setLocation("No location provided");
        }

        EventModel createdEvent = eventService.save(event);
        return ResponseEntity.ok(createdEvent);
    }

    /**
     * Retrieves a list of all events.
     * 
     * @return a list of all events in the system
     */
    @GetMapping
    public ResponseEntity<List<EventModel>> getAllEvents() {
        List<EventModel> events = eventService.findAll();
        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves all events created by a specific organizer.
     * 
     * @param organizerid the organizer's user ID
     * @return a list of events associated with the organizer
     */
    @GetMapping("/{organizerid}")
    public ResponseEntity<List<EventModel>> getEventsByOrganizerId(@PathVariable String organizerid) {
        List<EventModel> events = eventService.findByOrganizerid(organizerid);
        return ResponseEntity.ok(events);
    }

    /**
     * Deletes an event by its ID.
     * 
     * @param id the ID of the event to delete
     * @return a generic OK response on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable String id) {
        eventService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates an existing event by its ID.
     * 
     * Performs validation on required fields and updates the event if found.
     * Returns appropriate status codes depending on success or failure.
     * 
     * @param id the ID of the event to update
     * @param event the updated event data
     * @return a message indicating success or the type of error
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateEvent(@PathVariable String id, @RequestBody EventModel event) {
        if (event == null) {
            return new ResponseEntity<>("Error: event cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (event.getDate() == null) {
            event.setDate(new Date());
        }

        try {
            if (event.getName() == null || event.getName().isEmpty()) {
                return new ResponseEntity<>("Error: event name cannot be null or empty", HttpStatus.BAD_REQUEST);
            }

            if (event.getDescription() == null || event.getDescription().isEmpty()) {
                return new ResponseEntity<>("Error: event description cannot be null or empty", HttpStatus.BAD_REQUEST);
            }

            if (event.getLocation() == null || event.getLocation().isEmpty()) {
                return new ResponseEntity<>("Error: event location cannot be null or empty", HttpStatus.BAD_REQUEST);
            }

            EventModel updatedEvent = eventService.updateEvent(id, event);

            if (updatedEvent == null) {
                return new ResponseEntity<>("Error: event not found", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>("Event updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
