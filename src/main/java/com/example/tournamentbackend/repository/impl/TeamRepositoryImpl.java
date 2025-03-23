package com.example.tournamentbackend.repository.impl;

import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Team;
import com.example.tournamentbackend.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class TeamRepositoryImpl implements TeamRepository {

    private final JdbcTemplate jdbcTemplate;
    //RowMapper-sql sorgusundan qaytarilan her bir setri goturub onu bir Java obyektine cevirir
    private final RowMapper<Team> teamRowMapper;

    @Autowired
    public TeamRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.teamRowMapper = (rs, rowNum) -> new Team(
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

    @Override
    public Team save(Team team) {
        String sql = "INSERT INTO teams (name, played, wins, draws, losses, goal_difference, goals_scored, goals_conceded, last_5_games, points, logo_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //Keyholder bir magazada en esas acarlara sahib olan iscidir,tehlukesizlk
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
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
            return ps;
        }, keyHolder);

        int id = ((Number) keyHolder.getKeys().get("id")).intValue();
        team.setId(id);
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }

    @Override
    public void update(Team team) {
        String sql = "UPDATE teams SET name = ?, played = ?, wins = ?, draws = ?, losses = ?, " +
                "goal_difference = ?, goals_scored = ?, goals_conceded = ?, last_5_games = ?, points = ?, logo_path = ? " +
                "WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                team.getName(),
                team.getPlayed(),
                team.getWins(),
                team.getDraws(),
                team.getLosses(),
                team.getGoalDifference(),
                team.getGoalsScored(),
                team.getGoalsConceded(),
                team.getLast5Games(),
                team.getPoints(),
                team.getLogoPath(),
                team.getId()
        );

        if (updated == 0) {
            throw new ResourceNotFoundException("Team not found with id: " + team.getId());
        }
    }

    @Override
    public Optional<Team> findById(int id) {
        try {
            Team team = jdbcTemplate.queryForObject(
                    "SELECT * FROM teams WHERE id = ?",
                    new Object[]{id},
                    teamRowMapper
            );
            return Optional.ofNullable(team);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Team> findAll() {
        return jdbcTemplate.query("SELECT * FROM teams ORDER BY points DESC, goal_difference DESC", teamRowMapper);
    }


    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM teams WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);

        if (deleted == 0) {
            throw new ResourceNotFoundException("Team not found with id: " + id);
        }
    }

    @Override
    public List<Team> findByTournamentId(Long tournamentId) {
        String sql = "SELECT t.* FROM teams t " +
                "JOIN tournament_teams tt ON t.id = tt.team_id " +
                "WHERE tt.tournament_id = ? " +
                "ORDER BY t.points DESC, t.goal_difference DESC";

        return jdbcTemplate.query(sql, new Object[]{tournamentId}, teamRowMapper);
    }

    @Override
    public void addTeamToTournament(int teamId, Long tournamentId) {
        String sql = "INSERT INTO tournament_teams (tournament_id, team_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, tournamentId, teamId);
    }

    @Override
    public void removeTeamFromTournament(int teamId, Long tournamentId) {
        String sql = "DELETE FROM tournament_teams WHERE tournament_id = ? AND team_id = ?";
        jdbcTemplate.update(sql, tournamentId, teamId);
    }
}