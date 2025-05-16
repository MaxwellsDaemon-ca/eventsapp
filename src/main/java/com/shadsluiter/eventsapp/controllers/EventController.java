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

/*
 * This controller handles the events page
 * It allows users to view, create, edit, and delete events
 * It also allows users to search for events
 * It uses EventService to interact with the database
 * It uses UserService to retrieve users
 * It uses EventSearch to search for events
 * It uses EventModel to represent events
 */
@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;  // Inject UserService to retrieve users

    @Autowired
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping
    public String getAllEvents(Model model) {
        List<EventModel> events = eventService.findAll();
        model.addAttribute("events", events);
        model.addAttribute("message", "Showing all events");
        model.addAttribute("pageTitle", "Events");
        return "events";
    }

    @GetMapping("/create")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new EventModel());
        model.addAttribute("pageTitle", "Create Event");

        // Fetch all users to populate the dropdown
        List<UserModel> users = userService.findAll();  
        model.addAttribute("users", users);

        return "create-event";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute @Valid EventModel event, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Create Event");

            // Repopulate users if there are validation errors
            List<UserModel> users = userService.findAll();  
            model.addAttribute("users", users);

            return "create-event";
        }
        eventService.save(event);
        return "redirect:/events";
    }

    @GetMapping("/edit/{id}")
    public String showEditEventForm(@PathVariable String id, Model model) {
        EventModel event = eventService.findById(id);
        model.addAttribute("event", event);

        // Fetch all users to populate the dropdown
        List<UserModel> users = userService.findAll();
        model.addAttribute("users", users);

        return "edit-event";
    }

    @PostMapping("/edit/{id}")
    public String updateEvent(@PathVariable String id, @ModelAttribute EventModel event, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("event", event);

            // Repopulate users if there are validation errors
            List<UserModel> users = userService.findAll();
            model.addAttribute("users", users);

            return "edit-event";
        }
        eventService.updateEvent(id, event);
        return "redirect:/events";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable String id) {
        eventService.delete(id);
        return "redirect:/events";
    }

    @GetMapping("/search")
    public String searchForm(Model model) {
        model.addAttribute("eventSearch", new EventSearch());
        return "searchForm";
    }

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
