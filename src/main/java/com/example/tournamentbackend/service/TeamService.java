package com.example.tournamentbackend.service;

import com.example.tournamentbackend.dto.TeamDTO;

import java.util.List;

public interface TeamService {
    TeamDTO createTeam(TeamDTO teamDTO);
    TeamDTO getTeamById(int id);
    List<TeamDTO> getAllTeams();
    TeamDTO updateTeam(int id, TeamDTO teamDTO);
    void deleteTeam(int id);
    List<TeamDTO> getTeamsByTournamentId(Long tournamentId);
    void addTeamToTournament(int teamId, Long tournamentId);
    void removeTeamFromTournament(int teamId, Long tournamentId);
    void updateTeamStats(int teamId, int goalsScored, int goalsConceded, String result);
}