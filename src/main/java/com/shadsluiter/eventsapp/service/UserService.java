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

@Service
public class UserService implements UserDetailsService{

    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository; 
    }
 

    public UserModel save(UserModel userModel) { 
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        UserEntity userEntity = convertToEntity(userModel);
        UserEntity savedUser = userRepository.save(userEntity);
        return convertToModel(savedUser);
    }

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

    // will be replaced by loadUserByUsername in the UserDetailsService using Spring Security
    public UserModel findByLoginName(String loginName) {

       UserEntity userEntity = userRepository.findByLoginName(loginName);
        if (userEntity == null) {
            return null;
        }
       
        return convertToModel(userEntity);
    }

    // will be replaced when using Spring Security
    public boolean verifyPassword(UserModel user) {
        UserEntity userEntity = userRepository.findByLoginName(user.getUserName());
        if (userEntity == null) {
            return false;
        }
        
        return passwordEncoder.matches(user.getPassword(), userEntity.getPassword());
    }


    public UserModel findById(String id) {
        UserEntity userEntity = userRepository.findById(Long.parseLong(id));
        return convertToModel(userEntity);
    }

    public void delete(String id) {
        userRepository.deleteById(Long.parseLong(id));
    }

    public List<UserModel> findAll() {
        List<UserEntity> userEntities = userRepository.findAll();
        List<UserModel> userModels =  convertToModels(userEntities);
        return userModels;
    }  

    private List<UserModel> convertToModels(List<UserEntity> userEntities) {
        List<UserModel> userModels = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            userModels.add(convertToModel(userEntity));
        }
        return userModels;
    }
 
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

    private UserEntity convertToEntity(UserModel userModel) {
        UserEntity userEntity = new UserEntity();
        if (userModel.getId() != null) {
            userEntity.setId(Long.parseLong(userModel.getId()));
        }
        userEntity.setUserName(userModel.getUserName());
        userEntity.setPassword(userModel.getPassword()); 
          
        userEntity.setEnabled( userModel.isEnabled());
        userEntity.setAccountNonExpired(userModel.isAccountNonExpired());
        userEntity.setCredentialsNonExpired(userModel.isCredentialsNonExpired());
        userEntity.setAccountNonLocked(userModel.isAccountNonLocked());
        userEntity.setRoles(userModel.getRoles());
        return userEntity;

    } 
   
}
