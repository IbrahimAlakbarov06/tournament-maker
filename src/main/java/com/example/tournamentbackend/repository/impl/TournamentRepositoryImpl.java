package com.example.tournamentbackend.repository.impl;

import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Tournament;
import com.example.tournamentbackend.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class TournamentRepositoryImpl implements TournamentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Tournament> tournamentRowMapper;

    @Autowired
    public TournamentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.tournamentRowMapper = (rs, rowNum) -> mapRowToTournament(rs);
    }

    @Override
    public Tournament save(Tournament tournament) {
        String sql = "INSERT INTO tournaments (name, start_date, end_date, type, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tournament.getName());
            ps.setDate(2, Date.valueOf(tournament.getStartDate()));
            ps.setDate(3, tournament.getEndDate() != null ? Date.valueOf(tournament.getEndDate()) : null);
            ps.setString(4, tournament.getType() != null ? tournament.getType().toLowerCase() : null);
            ps.setString(5, tournament.getStatus() != null ? tournament.getStatus().toLowerCase() : "upcoming");
            return ps;
        }, keyHolder);

        Long id = ((Number) keyHolder.getKeys().get("id")).longValue();
        tournament.setId(id);
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
    }

    @Override
    public Optional<Tournament> findById(Long id) {
        try {
            Tournament tournament = jdbcTemplate.queryForObject(
                    "SELECT * FROM tournaments WHERE id = ?",
                    new Object[]{id},
                    tournamentRowMapper
            );
            return Optional.ofNullable(tournament);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Tournament> findAll() {
        return jdbcTemplate.query("SELECT * FROM tournaments", tournamentRowMapper);
    }

    @Override
    public void update(Tournament tournament) {
        String status = tournament.getStatus().trim().toLowerCase();
        List<String> allowedStatuses = Arrays.asList("upcoming", "ongoing", "completed");

        if (!allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status value: " + tournament.getStatus());
        }

        String sql = "UPDATE tournaments SET name = ?, start_date = ?, end_date = ?, status = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                tournament.getName(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                status,
                LocalDateTime.now(),
                tournament.getId()
        );
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM tournaments WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);

        if (deleted == 0) {
            throw new ResourceNotFoundException("Tournament not found with id: " + id);
        }
    }

    private Tournament mapRowToTournament(ResultSet rs) throws SQLException {
        Tournament tournament = new Tournament();
        tournament.setId(rs.getLong("id"));
        tournament.setName(rs.getString("name"));
        tournament.setStartDate(rs.getDate("start_date").toLocalDate());

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            tournament.setEndDate(endDate.toLocalDate());
        }

        tournament.setType(rs.getString("type"));
        tournament.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            tournament.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            tournament.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return tournament;
    }
}