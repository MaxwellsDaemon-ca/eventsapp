package com.shadsluiter.eventsapp.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.shadsluiter.eventsapp.models.UserEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * JDBC-based custom UserRepository implementation.
 * 
 * Handles persistence and retrieval of UserEntity objects, including user roles,
 * using explicit SQL queries instead of Spring Data JPA.
 */
@Repository
public class UserRepository implements UserRepositoryInterface {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor injecting JdbcTemplate for database access.
     * 
     * @param jdbcTemplate Spring JDBC template
     */
    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds a user by their login name.
     * 
     * @param loginName the login name to search for
     * @return the UserEntity or null if not found
     */
    @Override
    public UserEntity findByLoginName(String loginName) {
        String sql = "SELECT u.*, r.role FROM users u LEFT JOIN roles r ON u.id = r.user_id WHERE u.login_name = ?";
        try {
            List<UserEntity> users = jdbcTemplate.query(sql, ps -> ps.setString(1, loginName), new UserWithRolesExtractor());
            return users.isEmpty() ? null : users.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Retrieves all users and their roles.
     * 
     * @return list of UserEntity objects
     */
    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT u.*, r.role FROM users u LEFT JOIN roles r ON u.id = r.user_id";
        return jdbcTemplate.query(sql, new UserWithRolesExtractor());
    }

    /**
     * Deletes a user by ID.
     * 
     * @param id the user ID
     */
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Saves a new or existing user entity to the database.
     * 
     * Handles both insert and update logic, along with roles management.
     * 
     * @param userEntity the user entity to save
     * @return the saved UserEntity with ID populated
     */
    @Override
    public UserEntity save(UserEntity userEntity) {
        if (userEntity.getId() == null) {
            // Insert new user with default roles if not provided
            if (userEntity.getRoles() == null) {
                userEntity.setRoles(new HashSet<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN")));
            } else {
                userEntity.getRoles().add("ROLE_USER");
            }

            String sql = "INSERT INTO users (login_name, password, enabled, account_non_expired, credentials_non_expired, account_non_locked) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                userEntity.getUserName(),
                userEntity.getPassword(),
                userEntity.isEnabled(),
                userEntity.isAccountNonExpired(),
                userEntity.isCredentialsNonExpired(),
                userEntity.isAccountNonLocked()
            );
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            userEntity.setId(id);
        } else {
            // Update existing user
            String sql = "UPDATE users SET login_name = ?, password = ?, enabled = ?, account_non_expired = ?, credentials_non_expired = ?, account_non_locked = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                userEntity.getUserName(),
                userEntity.getPassword(),
                userEntity.isEnabled(),
                userEntity.isAccountNonExpired(),
                userEntity.isCredentialsNonExpired(),
                userEntity.isAccountNonLocked(),
                userEntity.getId()
            );
        }

        // Update roles
        deleteRoles(userEntity);
        saveRoles(userEntity);

        return userEntity;
    }

    /**
     * Saves user roles into the roles table.
     * 
     * @param userEntity the user whose roles are being saved
     */
    public void saveRoles(UserEntity userEntity) {
        if (userEntity.getRoles() != null) {
            String sql = "INSERT INTO roles (user_id, role) VALUES (?, ?)";
            for (String role : userEntity.getRoles()) {
                jdbcTemplate.update(sql, userEntity.getId(), role);
            }
        }
    }

    /**
     * Deletes all roles assigned to the user.
     * 
     * @param userEntity the user whose roles to delete
     */
    public void deleteRoles(UserEntity userEntity) {
        String sql = "DELETE FROM roles WHERE user_id = ?";
        jdbcTemplate.update(sql, userEntity.getId());
    }

    /**
     * Finds a user by ID.
     * 
     * @param id the user ID
     * @return the UserEntity or null if not found
     */
    @Override
    public UserEntity findById(Long id) {
        String sql = "SELECT u.*, r.role FROM users u LEFT JOIN roles r ON u.id = r.user_id WHERE u.id = ?";
        try {
            List<UserEntity> users = jdbcTemplate.query(sql, new Object[]{id}, new UserWithRolesExtractor());
            return users.isEmpty() ? null : users.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Returns the total user count.
     */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result != null ? result : 0;
    }

    /**
     * Deletes a user by entity object.
     * 
     * @param user the user to delete
     */
    @Override
    public void delete(UserEntity user) {
        deleteById(user.getId());
    }

    /**
     * Deletes all users from the database.
     */
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }

    /**
     * Deletes a collection of users.
     * 
     * @param users iterable collection of users
     */
    @Override
    public void deleteAll(Iterable<? extends UserEntity> users) {
        for (UserEntity user : users) {
            delete(user);
        }
    }

    /**
     * Saves multiple users in a batch operation.
     * 
     * @param users iterable collection of users
     * @return list of saved users
     */
    @Override
    public List<UserEntity> saveAll(Iterable<UserEntity> users) {
        for (UserEntity user : users) {
            save(user);
        }
        return (List<UserEntity>) users;
    }

    /**
     * ResultSetExtractor implementation for extracting users with roles from joined query.
     */
    private static class UserWithRolesExtractor implements ResultSetExtractor<List<UserEntity>> {
        @Override
        public List<UserEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, UserEntity> userMap = new HashMap<>();
            while (rs.next()) {
                Long id = rs.getLong("id");
                UserEntity user = userMap.get(id);
                if (user == null) {
                    user = new UserEntity();
                    user.setId(id);
                    user.setUserName(rs.getString("login_name"));
                    user.setPassword(rs.getString("password"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                    user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    user.setRoles(new HashSet<>());
                    userMap.put(id, user);
                }
                String role = rs.getString("role");
                if (role != null) {
                    user.getRoles().add(role);
                }
            }
            return new ArrayList<>(userMap.values());
        }
    }
}
