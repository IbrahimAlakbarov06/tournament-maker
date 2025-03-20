package com.example.tournamentbackend.repository.impl;

import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Match;
import com.example.tournamentbackend.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MatchRepositoryImpl implements MatchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Match> matchRowMapper;

    @Autowired
    public MatchRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.matchRowMapper = (rs, rowNum) -> new Match(
                rs.getLong("id"),
                rs.getLong("tournament_id"),
                rs.getInt("home_team_id"),
                rs.getInt("away_team_id"),
                (Integer) rs.getObject("home_team_score"),
                (Integer) rs.getObject("away_team_score"),
                rs.getTimestamp("match_date") != null ? rs.getTimestamp("match_date").toLocalDateTime() : null,
                rs.getString("status"),
                rs.getString("round"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }

    @Override
    public Match save(Match match) {
        String sql = "INSERT INTO matches (tournament_id, home_team_id, away_team_id, home_team_score, " +
                "away_team_score, match_date, status, round, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, match.getTournamentId());
            ps.setInt(2, match.getHomeTeamId());
            ps.setInt(3, match.getAwayTeamId());
            ps.setObject(4, match.getHomeTeamScore());
            ps.setObject(5, match.getAwayTeamScore());
            ps.setTimestamp(6, match.getMatchDate() != null ? Timestamp.valueOf(match.getMatchDate()) : null);
            ps.setString(7, match.getStatus() != null ? match.getStatus().toLowerCase() : "scheduled");
            ps.setString(8, match.getRound());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Long id = ((Number) keyHolder.getKeys().get("id")).longValue();
        match.setId(id);
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
    }

    @Override
    public Optional<Match> findById(Long id) {
        try {
            Match match = jdbcTemplate.queryForObject(
                    "SELECT * FROM matches WHERE id = ?",
                    new Object[]{id},
                    matchRowMapper
            );
            return Optional.ofNullable(match);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Match> findAll() {
        return jdbcTemplate.query("SELECT * FROM matches ORDER BY match_date", matchRowMapper);
    }

    @Override
    public List<Match> findByTournamentId(Long tournamentId) {
        return jdbcTemplate.query(
                "SELECT * FROM matches WHERE tournament_id = ? ORDER BY match_date",
                new Object[]{tournamentId},
                matchRowMapper
        );
    }

    @Override
    public List<Match> findByTeamId(int teamId) {
        return jdbcTemplate.query(
                "SELECT * FROM matches WHERE home_team_id = ? OR away_team_id = ? ORDER BY match_date",
                new Object[]{teamId, teamId},
                matchRowMapper
        );
    }

    @Override
    public void update(Match match) {
        String sql = "UPDATE matches SET tournament_id = ?, home_team_id = ?, away_team_id = ?, " +
                "home_team_score = ?, away_team_score = ?, match_date = ?, status = ?, round = ?, " +
                "updated_at = ? WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                match.getTournamentId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                match.getHomeTeamScore(),
                match.getAwayTeamScore(),
                match.getMatchDate() != null ? Timestamp.valueOf(match.getMatchDate()) : null,
                match.getStatus(),
                match.getRound(),
                Timestamp.valueOf(LocalDateTime.now()),
                match.getId()
        );

        if (updated == 0) {
            throw new ResourceNotFoundException("Match not found with id: " + match.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM matches WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);

        if (deleted == 0) {
            throw new ResourceNotFoundException("Match not found with id: " + id);
        }
    }
}