package com.example.tournamentbackend.dao.impl;

import com.example.tournamentbackend.config.ConnectionHelper;
import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Team;
import com.example.tournamentbackend.dao.TeamDao;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TeamDaoImpl implements TeamDao {

    @Override
    public Team save(Team team) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "INSERT INTO teams (name, played, wins, draws, losses, goal_difference, goals_scored, goals_conceded, last_5_games, points, logo_path) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, team.getName());
            ps.setInt(2, team.getPlayed());
            ps.setInt(3, team.getWins());
            ps.setInt(4, team.getDraws());
            ps.setInt(5, team.getLosses());
            ps.setInt(6, team.getGoalDifference());
            ps.setInt(7, team.getGoalsScored());
            ps.setInt(8, team.getGoalsConceded());
            ps.setString(9, team.getLast5Games());
            ps.setInt(10, team.getPoints());
            ps.setString(11, team.getLogoPath());

            int row = ps.executeUpdate();

            if (row == 0) {
                throw new ResourceNotFoundException("Creating team failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    team.setId(generatedKeys.getInt(1));
                } else {
                    throw new ResourceNotFoundException("Creating team failed, no ID obtained.");
                }
            }

            return team;
        } catch (SQLException e) {
            System.out.println("Error saving team: " + e.getMessage());
            throw new RuntimeException("Database error occurred", e);
        }
    }

    @Override
    public Optional<Team> findById(int id) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "SELECT * FROM teams WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Team team = mapResultSetToTeam(rs);
                    return Optional.of(team);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding team by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Team> findAll() {
        List<Team> teams = new ArrayList<>();

        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "SELECT * FROM teams ORDER BY points DESC, goal_difference DESC";
            Statement statement = connection.createStatement();

            try (ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) {
                    Team team = mapResultSetToTeam(rs);
                    teams.add(team);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding all teams: " + e.getMessage());
        }

        return teams;
    }

    @Override
    public void update(Team team) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "UPDATE teams SET name = ?, played = ?, wins = ?, draws = ?, losses = ?, " +
                    "goal_difference = ?, goals_scored = ?, goals_conceded = ?, last_5_games = ?, points = ?, logo_path = ? " +
                    "WHERE id = ?";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, team.getName());
            ps.setInt(2, team.getPlayed());
            ps.setInt(3, team.getWins());
            ps.setInt(4, team.getDraws());
            ps.setInt(5, team.getLosses());
            ps.setInt(6, team.getGoalDifference());
            ps.setInt(7, team.getGoalsScored());
            ps.setInt(8, team.getGoalsConceded());
            ps.setString(9, team.getLast5Games());
            ps.setInt(10, team.getPoints());
            ps.setString(11, team.getLogoPath());
            ps.setInt(12, team.getId());

            int updatedRow = ps.executeUpdate();

            if (updatedRow == 0) {
                throw new ResourceNotFoundException("Team not found with id: " + team.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error updating team: " + e.getMessage());
            throw new RuntimeException("Database error occurred", e);
        }
    }

    @Override
    public void deleteById(int id) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "DELETE FROM teams WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            int deletedRow = ps.executeUpdate();

            if (deletedRow == 0) {
                throw new ResourceNotFoundException("Team not found with id: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting team: " + e.getMessage());
            throw new RuntimeException("Database error occurred", e);
        }
    }

    @Override
    public List<Team> findByTournamentId(Long tournamentId) {
        List<Team> teams = new ArrayList<>();

        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "SELECT t.* FROM teams t " +
                    "JOIN tournament_teams tt ON t.id = tt.team_id " +
                    "WHERE tt.tournament_id = ? " +
                    "ORDER BY t.points DESC, t.goal_difference DESC";

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Team team = mapResultSetToTeam(rs);
                    teams.add(team);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding teams by tournament ID: " + e.getMessage());
        }

        return teams;
    }

    @Override
    public void addTeamToTournament(int teamId, Long tournamentId) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "INSERT INTO tournament_teams (tournament_id, team_id) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, tournamentId);
            ps.setInt(2, teamId);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding team to tournament: " + e.getMessage());
            throw new RuntimeException("Database error occurred", e);
        }
    }

    @Override
    public void removeTeamFromTournament(int teamId, Long tournamentId) {
        try (Connection connection = ConnectionHelper.getConnection()) {
            String sql = "DELETE FROM tournament_teams WHERE tournament_id = ? AND team_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, tournamentId);
            ps.setInt(2, teamId);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing team from tournament: " + e.getMessage());
            throw new RuntimeException("Database error occurred", e);
        }
    }

    // Helper method to map ResultSet to Team entity
    private Team mapResultSetToTeam(ResultSet rs) throws SQLException {
        return new Team(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("played"),
                rs.getInt("wins"),
                rs.getInt("draws"),
                rs.getInt("losses"),
                rs.getInt("goal_difference"),
                rs.getInt("goals_scored"),
                rs.getInt("goals_conceded"),
                rs.getString("last_5_games"),
                rs.getInt("points"),
                rs.getString("logo_path")
        );
    }
}