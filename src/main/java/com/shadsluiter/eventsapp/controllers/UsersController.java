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

/*
 * This controller handles the users page
 * It allows users to register and login
 * It uses UserService to interact with the database
 * It uses UserModel to represent users
 * It uses Logger to log messages
 */
@Controller
@RequestMapping("/users")
public class UsersController {

    // userservice allows the controller to interact with the database and users table
    @Autowired
    private UserService userService;

   
    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserModel());
        return "register";
    }

    // response to the form submission. create a new user and save it to the database
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserModel user, Model model) {
        UserModel existingUser = userService.findByLoginName(user.getUserName());
        if (existingUser != null) {
            model.addAttribute("error", "User already exists!");
            model.addAttribute("user", user);
            return "register";
        }
      
        setDefaultValues(user);
        // save the user to the database

        userService.save(user);
        logger.info("User registered: {}", user.getUserName());
        model.addAttribute("user", user);
        return "redirect:/users/loginForm";
    }

    // if register form does not have these values, set default values here
    private void setDefaultValues(UserModel user) {
        // set default values for the user
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
    }

    // can use spring security instead of this method to authenticate users
    /*@PostMapping("/login")
    public String login(@ModelAttribute UserModel user, Model model) {
        UserModel existingUser = userService.findByLoginName(user.getUserName());
        // first check if the user exists
        if (existingUser == null) {
            model.addAttribute("error", "User not found!");
            model.addAttribute("user", user);
            return "login";
        }
        // user exists, check the password
        if (!userService.verifyPassword(user) ){
            model.addAttribute("error", "Invalid password!");
            model.addAttribute("user", user);
            return "login";
        }
        // user exists and password is correct redirect to the home page
        return "redirect:/users/";
    }*/

    // show the login form.
    @GetMapping("/loginForm")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new UserModel());
        model.addAttribute("pageTitle", "Login");
        return "login";
    }
 
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/users/loginForm";
    }
}
