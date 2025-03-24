package com.example.tournamentbackend.dao.impl;

import com.example.tournamentbackend.config.ConnectionHelper;
import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Tournament;
import com.example.tournamentbackend.dao.TournamentDao;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class TournamentDaoImpl implements TournamentDao {

    @Override
    public Tournament save(Tournament tournament) {
        String sql = "INSERT INTO tournaments (name, start_date, end_date, type, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tournament.getName());
            ps.setDate(2, Date.valueOf(tournament.getStartDate()));
            ps.setDate(3, tournament.getEndDate() != null ? Date.valueOf(tournament.getEndDate()) : null);
            ps.setString(4, tournament.getType() != null ? tournament.getType().toLowerCase() : null);
            ps.setString(5, tournament.getStatus() != null ? tournament.getStatus().toLowerCase() : "upcoming");

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tournament.setId(generatedKeys.getLong(1));
                    return findById(tournament.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournament.getId()));
                } else {
                    throw new SQLException("Creating tournament failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error saving tournament: " + e.getMessage());
            throw new RuntimeException("Error saving tournament", e);
        }
    }

    @Override
    public Optional<Tournament> findById(Long id) {
        String sql = "SELECT * FROM tournaments WHERE id = ?";

        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTournament(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding tournament: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Tournament> findAll() {
        String sql = "SELECT * FROM tournaments";
        List<Tournament> tournaments = new ArrayList<>();

        try (Connection connection = ConnectionHelper.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tournaments.add(mapRowToTournament(rs));
            }

            return tournaments;
        } catch (SQLException e) {
            System.out.println("Error finding all tournaments: " + e.getMessage());
            return tournaments;
        }
    }

    @Override
    public void update(Tournament tournament) {
        String status = tournament.getStatus().trim().toLowerCase();
        List<String> allowedStatuses = Arrays.asList("upcoming", "ongoing", "completed");

        if (!allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status value: " + tournament.getStatus());
        }

        String sql = "UPDATE tournaments SET name = ?, start_date = ?, end_date = ?, status = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, tournament.getName());
            ps.setDate(2, Date.valueOf(tournament.getStartDate()));
            ps.setDate(3, tournament.getEndDate() != null ? Date.valueOf(tournament.getEndDate()) : null);
            ps.setString(4, status);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(6, tournament.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new ResourceNotFoundException("Tournament not found with id: " + tournament.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error updating tournament: " + e.getMessage());
            throw new RuntimeException("Error updating tournament", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM tournaments WHERE id = ?";

        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new ResourceNotFoundException("Tournament not found with id: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting tournament: " + e.getMessage());
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