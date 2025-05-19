package com.shadsluiter.eventsapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.shadsluiter.eventsapp.models.EventModel;
import com.shadsluiter.eventsapp.models.EventSearch;
import com.shadsluiter.eventsapp.models.UserModel;
import com.shadsluiter.eventsapp.service.EventService;
import com.shadsluiter.eventsapp.service.UserService;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller for handling event-related operations.
 * 
 * Provides endpoints for viewing, creating, editing, deleting, and searching events.
 * Utilizes EventService for business logic and UserService to populate user-related form data.
 */
@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    /**
     * Constructs the EventController with required services.
     * 
     * @param eventService the service used for event operations
     * @param userService the service used for user retrieval
     */
    @Autowired
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    /**
     * Displays all events on the main events page.
     */
    @GetMapping
    public String getAllEvents(Model model) {
        List<EventModel> events = eventService.findAll();
        model.addAttribute("events", events);
        model.addAttribute("message", "Showing all events");
        model.addAttribute("pageTitle", "Events");
        return "events";
    }

    /**
     * Displays the event creation form.
     */
    @GetMapping("/create")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new EventModel());
        model.addAttribute("pageTitle", "Create Event");

        // Populate user dropdown
        List<UserModel> users = userService.findAll();  
        model.addAttribute("users", users);

        return "create-event";
    }

    /**
     * Handles event creation form submission.
     * 
     * If validation fails, redisplays the form with user data.
     */
    @PostMapping("/create")
    public String createEvent(@ModelAttribute @Valid EventModel event, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Create Event");

            // Repopulate user dropdown on validation error
            List<UserModel> users = userService.findAll();  
            model.addAttribute("users", users);

            return "create-event";
        }

        eventService.save(event);
        return "redirect:/events";
    }

    /**
     * Displays the event editing form for a given event ID.
     */
    @GetMapping("/edit/{id}")
    public String showEditEventForm(@PathVariable String id, Model model) {
        EventModel event = eventService.findById(id);
        model.addAttribute("event", event);

        // Populate user dropdown
        List<UserModel> users = userService.findAll();
        model.addAttribute("users", users);

        return "edit-event";
    }

    /**
     * Handles form submission for editing an event.
     * 
     * If validation fails, repopulates user data and redisplays the form.
     */
    @PostMapping("/edit/{id}")
    public String updateEvent(@PathVariable String id, @ModelAttribute EventModel event, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("event", event);

            // Repopulate user dropdown on validation error
            List<UserModel> users = userService.findAll();
            model.addAttribute("users", users);

            return "edit-event";
        }

        eventService.updateEvent(id, event);
        return "redirect:/events";
    }

    /**
     * Deletes the specified event by ID.
     */
    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable String id) {
        eventService.delete(id);
        return "redirect:/events";
    }

    /**
     * Displays the event search form.
     */
    @GetMapping("/search")
    public String searchForm(Model model) {
        model.addAttribute("eventSearch", new EventSearch());
        return "searchForm";
    }

    /**
     * Processes the event search query and displays results.
     * 
     * If validation fails, returns to the search form.
     */
    @PostMapping("/search")
    public String search(@ModelAttribute @Valid EventSearch eventSearch, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "searchForm";
        }

        List<EventModel> events = eventService.findByDescription(eventSearch.getSearchString());
        model.addAttribute("message", "Search results for " + eventSearch.getSearchString());
        model.addAttribute("events", events);

        return "events";
    }
}
