package com.shadsluiter.eventsapp.data;
 
import java.util.List;

import com.shadsluiter.eventsapp.models.UserEntity;

public interface UserRepositoryInterface {
    UserEntity findByLoginName(String loginName);
    List<UserEntity> findAll();
    void deleteById(Long id);
    UserEntity save(UserEntity user);
    UserEntity findById(Long id); 
    long count();
    void delete(UserEntity user);
    void deleteAll();
    void deleteAll(Iterable<? extends UserEntity> users); 
    List<UserEntity> saveAll(Iterable<UserEntity> users);
}
