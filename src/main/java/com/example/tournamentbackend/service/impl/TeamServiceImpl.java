package com.example.tournamentbackend.service.impl;

import com.example.tournamentbackend.dto.TeamDTO;
import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Team;
import com.example.tournamentbackend.repository.TeamRepository;
import com.example.tournamentbackend.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = convertToEntity(teamDTO);
        // Initialize new team with default values
        if (team.getPlayed() == 0) {
            team.setWins(0);
            team.setDraws(0);
            team.setLosses(0);
            team.setGoalDifference(0);
            team.setGoalsScored(0);
            team.setGoalsConceded(0);
            team.setLast5Games("");
            team.setPoints(0);
        }
        Team savedTeam = teamRepository.save(team);
        return convertToDTO(savedTeam);
    }

    @Override
    public TeamDTO getTeamById(int id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return convertToDTO(team);
    }

    @Override
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeamDTO updateTeam(int id, TeamDTO teamDTO) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        existingTeam.setName(teamDTO.getName());
        existingTeam.setPlayed(teamDTO.getPlayed());
        existingTeam.setWins(teamDTO.getWins());
        existingTeam.setDraws(teamDTO.getDraws());
        existingTeam.setLosses(teamDTO.getLosses());
        existingTeam.setGoalDifference(teamDTO.getGoalDifference());
        existingTeam.setGoalsScored(teamDTO.getGoalsScored());
        existingTeam.setGoalsConceded(teamDTO.getGoalsConceded());
        existingTeam.setLast5Games(teamDTO.getLast5Games());
        existingTeam.setPoints(teamDTO.getPoints());

        teamRepository.update(existingTeam);
        return convertToDTO(existingTeam);
    }

    @Override
    public void deleteTeam(int id) {
        teamRepository.deleteById(id);
    }

    @Override
    public List<TeamDTO> getTeamsByTournamentId(Long tournamentId) {
        return teamRepository.findByTournamentId(tournamentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void addTeamToTournament(int teamId, Long tournamentId) {
        // Check if team exists
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        teamRepository.addTeamToTournament(teamId, tournamentId);
    }

    @Override
    public void removeTeamFromTournament(int teamId, Long tournamentId) {
        teamRepository.removeTeamFromTournament(teamId, tournamentId);
    }

    @Override
    public void updateTeamStats(int teamId, int goalsScored, int goalsConceded, String result) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Update team statistics
        team.setPlayed(team.getPlayed() + 1);
        team.setGoalsScored(team.getGoalsScored() + goalsScored);
        team.setGoalsConceded(team.getGoalsConceded() + goalsConceded);
        team.setGoalDifference(team.getGoalsScored() - team.getGoalsConceded());

        // Update wins, draws, losses, points and last 5 games
        String last5Games = team.getLast5Games();
        if (last5Games.length() >= 5) {
            last5Games = last5Games.substring(1);
        }

        switch (result) {
            case "W":
                team.setWins(team.getWins() + 1);
                team.setPoints(team.getPoints() + 3);
                last5Games += "W";
                break;
            case "D":
                team.setDraws(team.getDraws() + 1);
                team.setPoints(team.getPoints() + 1);
                last5Games += "D";
                break;
            case "L":
                team.setLosses(team.getLosses() + 1);
                last5Games += "L";
                break;
            default:
                throw new IllegalArgumentException("Invalid match result: " + result);
        }

        team.setLast5Games(last5Games);
        teamRepository.update(team);
    }

    private TeamDTO convertToDTO(Team team) {
        return new TeamDTO(
                team.getId(),
                team.getName(),
                team.getPlayed(),
                team.getWins(),
                team.getDraws(),
                team.getLosses(),
                team.getGoalDifference(),
                team.getGoalsScored(),
                team.getGoalsConceded(),
                team.getLast5Games(),
                team.getPoints()
        );
    }

    private Team convertToEntity(TeamDTO teamDTO) {
        return new Team(
                teamDTO.getId(),
                teamDTO.getName(),
                teamDTO.getPlayed(),
                teamDTO.getWins(),
                teamDTO.getDraws(),
                teamDTO.getLosses(),
                teamDTO.getGoalDifference(),
                teamDTO.getGoalsScored(),
                teamDTO.getGoalsConceded(),
                teamDTO.getLast5Games(),
                teamDTO.getPoints()
        );
    }
}