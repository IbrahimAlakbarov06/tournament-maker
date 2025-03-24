package com.example.tournamentbackend.dao.impl;

import com.example.tournamentbackend.config.ConnectionHelper;
import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Match;
import com.example.tournamentbackend.dao.MatchDao;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MatchDaoImpl implements MatchDao {

    @Override
    public Match save(Match match) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "INSERT INTO matches (tournament_id, home_team_id, away_team_id, home_team_score, " +
                    "away_team_score, match_date, status, round, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating match failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    match.setId(id);
                    return findById(id).orElseThrow(() ->
                            new ResourceNotFoundException("Match not found with id: " + id));
                } else {
                    throw new SQLException("Creating match failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Match> findById(Long id) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "SELECT * FROM matches WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToMatch(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Match> findAll() {
        List<Match> matches = new ArrayList<>();
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "SELECT * FROM matches ORDER BY match_date";
            Statement stmt = connection.createStatement();

            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
            return matches;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Match> findByTournamentId(Long tournamentId) {
        List<Match> matches = new ArrayList<>();
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "SELECT * FROM matches WHERE tournament_id = ? ORDER BY match_date";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
            return matches;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Match> findByTeamId(int teamId) {
        List<Match> matches = new ArrayList<>();
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "SELECT * FROM matches WHERE home_team_id = ? OR away_team_id = ? ORDER BY match_date";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
            return matches;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Match match) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "UPDATE matches SET tournament_id = ?, home_team_id = ?, away_team_id = ?, " +
                    "home_team_score = ?, away_team_score = ?, match_date = ?, status = ?, round = ?, " +
                    "updated_at = ? WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, match.getTournamentId());
            ps.setInt(2, match.getHomeTeamId());
            ps.setInt(3, match.getAwayTeamId());
            ps.setObject(4, match.getHomeTeamScore());
            ps.setObject(5, match.getAwayTeamScore());
            ps.setTimestamp(6, match.getMatchDate() != null ? Timestamp.valueOf(match.getMatchDate()) : null);
            ps.setString(7, match.getStatus());
            ps.setString(8, match.getRound());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(10, match.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new ResourceNotFoundException("Match not found with id: " + match.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "DELETE FROM matches WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new ResourceNotFoundException("Match not found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    private Match mapRowToMatch(ResultSet rs) throws SQLException {
        return new Match(
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
}