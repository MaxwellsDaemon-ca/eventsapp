package com.shadsluiter.eventsapp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shadsluiter.eventsapp.data.UserRepository;
import com.shadsluiter.eventsapp.models.UserEntity;
import com.shadsluiter.eventsapp.models.UserModel;

/**
 * Service class for managing user data and authentication logic.
 * 
 * Implements Spring Security's UserDetailsService for loading users by username.
 * Handles user persistence, transformation between entity/model, and password encryption.
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a UserService with dependencies on user repository and password encoder.
     * 
     * @param userRepository the data layer for user persistence
     * @param passwordEncoder the encoder used to hash user passwords
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Saves a new or updated user after encoding the password.
     * 
     * @param userModel the user data to persist
     * @return the persisted user
     */
    public UserModel save(UserModel userModel) {
        UserEntity userEntity = convertToEntity(userModel);
        userEntity.setPassword(passwordEncoder.encode(userModel.getPassword()));
        UserEntity savedUser = userRepository.save(userEntity);
        return convertToModel(savedUser);
    }

    /**
     * Loads a user by username for authentication purposes.
     * 
     * Converts roles into Spring Security authorities.
     * 
     * @param username the login name
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByLoginName(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException("User not found");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : userEntity.getRoles()) {
            authorities.add((GrantedAuthority) () -> role);
        }

        return new User(userEntity.getUserName(), userEntity.getPassword(), authorities);
    }

    /**
     * Finds a user by their login name.
     * 
     * @param loginName the login name to search for
     * @return a UserModel if found, otherwise null
     */
    public UserModel findByLoginName(String loginName) {
        UserEntity userEntity = userRepository.findByLoginName(loginName);
        if (userEntity == null) {
            return null;
        }
        return convertToModel(userEntity);
    }

    /**
     * Verifies the user's raw password against the encoded password in the database.
     * 
     * Deprecated with Spring Security handling authentication logic.
     * 
     * @param user the user with credentials to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(UserModel user) {
        UserEntity userEntity = userRepository.findByLoginName(user.getUserName());
        if (userEntity == null) {
            return false;
        }

        return passwordEncoder.matches(user.getPassword(), userEntity.getPassword());
    }

    /**
     * Finds a user by their numeric ID.
     * 
     * @param id the string ID to search
     * @return the user model if found
     */
    public UserModel findById(String id) {
        UserEntity userEntity = userRepository.findById(Long.parseLong(id));
        return convertToModel(userEntity);
    }

    /**
     * Deletes a user by ID.
     * 
     * @param id the user ID to delete
     */
    public void delete(String id) {
        userRepository.deleteById(Long.parseLong(id));
    }

    /**
     * Retrieves all users from the database.
     * 
     * @return a list of all user models
     */
    public List<UserModel> findAll() {
        List<UserEntity> userEntities = userRepository.findAll();
        return convertToModels(userEntities);
    }

    /**
     * Converts a list of UserEntity objects to UserModel objects.
     * 
     * @param userEntities the entities to convert
     * @return a list of models
     */
    private List<UserModel> convertToModels(List<UserEntity> userEntities) {
        List<UserModel> userModels = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            userModels.add(convertToModel(userEntity));
        }
        return userModels;
    }

    /**
     * Converts a UserEntity to a UserModel.
     * 
     * @param userEntity the entity to convert
     * @return the corresponding model
     */
    private UserModel convertToModel(UserEntity userEntity) {
        UserModel userModel = new UserModel();
        userModel.setId(userEntity.getId().toString());
        userModel.setUserName(userEntity.getUserName());
        userModel.setPassword(userEntity.getPassword());
        userModel.setEnabled(userEntity.isEnabled());
        userModel.setAccountNonExpired(userEntity.isAccountNonExpired());
        userModel.setCredentialsNonExpired(userEntity.isCredentialsNonExpired());
        userModel.setAccountNonLocked(userEntity.isAccountNonLocked());
        userModel.setRoles(userEntity.getRoles());
        return userModel;
    }

    /**
     * Converts a UserModel to a UserEntity.
     * 
     * @param userModel the model to convert
     * @return the corresponding entity
     */
    private UserEntity convertToEntity(UserModel userModel) {
        UserEntity userEntity = new UserEntity();

        if (userModel.getId() != null) {
            userEntity.setId(Long.parseLong(userModel.getId()));
        }

        userEntity.setUserName(userModel.getUserName());
        userEntity.setPassword(userModel.getPassword());
        userEntity.setEnabled(userModel.isEnabled());
        userEntity.setAccountNonExpired(userModel.isAccountNonExpired());
        userEntity.setCredentialsNonExpired(userModel.isCredentialsNonExpired());
        userEntity.setAccountNonLocked(userModel.isAccountNonLocked());
        userEntity.setRoles(userModel.getRoles());

        return userEntity;
    }
}
