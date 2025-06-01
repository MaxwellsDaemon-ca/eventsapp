package com.shadsluiter.eventsapp.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.shadsluiter.eventsapp.models.EventEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC-based repository implementation for EventEntity.
 * 
 * Provides custom SQL queries to manage events without using Spring Data JPA.
 */
@Repository
public class EventRepository implements EventRepositoryInterface {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs the repository with a JdbcTemplate instance.
     * 
     * @param jdbcTemplate Spring JDBC template for database access
     */
    @Autowired
    public EventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Retrieves all events created by a specific organizer.
     * 
     * @param organizerid the ID of the organizer
     * @return list of matching EventEntity objects
     */
    @Override
    public List<EventEntity> findByOrganizerid(Long organizerid) {
        String sql = "SELECT * FROM events WHERE organizerid = ?";
        return jdbcTemplate.query(sql, new Object[]{organizerid}, new EventModelRowMapper());
    }

    /**
     * Retrieves all events from the database.
     * 
     * @return list of all EventEntity objects
     */
    @Override
    public List<EventEntity> findAll() {
        String sql = "SELECT * FROM events";
        return jdbcTemplate.query(sql, new EventModelRowMapper());
    }

    /**
     * Deletes an event by its ID.
     * 
     * @param id the ID of the event to delete
     */
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM events WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Saves a new event into the database.
     * 
     * @param event the event to save
     * @return the saved EventEntity with generated ID
     */
    @Override
    public EventEntity save(EventEntity event) {
        String sql = "INSERT INTO events (name, date, location, organizerid, description) " +
                     "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            event.getName(),
            event.getDate(),
            event.getLocation(),
            event.getOrganizerid(),
            event.getDescription()
        );

        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        event.setId(id);
        return event;
    }

    /**
     * Retrieves an event by its ID.
     * 
     * @param id the ID of the event
     * @return the matching EventEntity object
     */
    @Override
    public EventEntity findById(Long id) {
        String sql = "SELECT * FROM events WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new EventModelRowMapper());
    }

    /**
     * Checks whether an event exists by its ID.
     * 
     * @param id the ID to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM events WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Searches for events whose descriptions contain the given text.
     * 
     * @param description the search string
     * @return list of matching EventEntity objects
     */
    @Override
    public List<EventEntity> findByDescription(String description) { 
        String sql = "SELECT * FROM events WHERE description LIKE ?";
        return jdbcTemplate.query(sql, ps -> ps.setString(1, "%" + description + "%"), new EventModelRowMapper());
    }

    /**
     * Maps database rows to EventEntity objects.
     */
    private static class EventModelRowMapper implements RowMapper<EventEntity> {
        @Override
        public EventEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventEntity event = new EventEntity();
            event.setId(rs.getLong("id"));
            event.setName(rs.getString("name"));
            event.setDate(rs.getDate("date"));
            event.setLocation(rs.getString("location"));
            event.setOrganizerid(rs.getString("organizerid"));
            event.setDescription(rs.getString("description"));
            return event;
        }
    }
}
