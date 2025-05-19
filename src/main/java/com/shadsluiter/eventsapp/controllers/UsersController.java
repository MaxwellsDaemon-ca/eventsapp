package com.shadsluiter.eventsapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.shadsluiter.eventsapp.models.UserModel;
import com.shadsluiter.eventsapp.service.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for handling user-related web requests.
 * 
 * Provides endpoints for user registration, login form display, and logout.
 * Relies on UserService for persistence and Logger for registration tracking.
 */
@Controller
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    /**
     * Displays the home page.
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Displays the user registration form.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserModel());
        return "register";
    }

    /**
     * Handles user registration form submission.
     * 
     * Checks for duplicate users and sets default account values before saving.
     * 
     * @param user the user input from the form
     * @param model the UI model for passing data to the view
     * @return redirection or registration form depending on success
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserModel user, Model model) {
        UserModel existingUser = userService.findByLoginName(user.getUserName());
        if (existingUser != null) {
            model.addAttribute("error", "User already exists!");
            model.addAttribute("user", user);
            return "register";
        }

        setDefaultValues(user);
        userService.save(user);
        logger.info("User registered: {}", user.getUserName());

        model.addAttribute("user", user);
        return "redirect:/users/loginForm";
    }

    /**
     * Sets default values for account flags on a new user.
     * 
     * @param user the user to apply defaults to
     */
    private void setDefaultValues(UserModel user) {
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
    }

    /**
     * Displays the login form page.
     */
    @GetMapping("/loginForm")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new UserModel());
        model.addAttribute("pageTitle", "Login");
        return "login";
    }

    /**
     * Logs the user out by invalidating the session.
     * 
     * @param session the current HTTP session
     * @return redirect to the login form
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/users/loginForm";
    }

    /*
     * Deprecated manual login method (now handled by Spring Security)
     * 
     * Uncomment and modify only if bypassing Spring Security is necessary.
     */
    /*
    @PostMapping("/login")
    public String login(@ModelAttribute UserModel user, Model model) {
        UserModel existingUser = userService.findByLoginName(user.getUserName());

        if (existingUser == null) {
            model.addAttribute("error", "User not found!");
            model.addAttribute("user", user);
            return "login";
        }

        if (!userService.verifyPassword(user)) {
            model.addAttribute("error", "Invalid password!");
            model.addAttribute("user", user);
            return "login";
        }

        return "redirect:/users/";
    }
    */
}
