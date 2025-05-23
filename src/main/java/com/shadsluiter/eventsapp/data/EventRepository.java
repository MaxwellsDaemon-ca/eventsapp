package com.shadsluiter.eventsapp.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.shadsluiter.eventsapp.models.EventEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EventRepository implements EventRepositoryInterface {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<EventEntity> findByOrganizerid(Long organizerid) {
        String sql = "SELECT * FROM events WHERE organizerid = " + organizerid;
        return jdbcTemplate.query(sql, new EventModelRowMapper());
    }

    @Override
    public List<EventEntity> findAll() {
        String sql = "SELECT * FROM events";
        return jdbcTemplate.query(sql, new EventModelRowMapper());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM events WHERE id = " + id;
        jdbcTemplate.update(sql);
    }

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



    @Override
    public EventEntity findById(Long id) {
        String sql = "SELECT * FROM events WHERE id = " + id;
        return jdbcTemplate.queryForObject(sql, new EventModelRowMapper());
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM events WHERE id = " + id;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null && count > 0;
    }

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

    @Override
    public List<EventEntity> findByDescription(String description) { 
        String sql = "SELECT * FROM events WHERE description LIKE '%" + description + "%'";
        return jdbcTemplate.query(sql, new EventModelRowMapper());
    }
}
