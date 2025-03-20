package com.example.tournamentbackend.controller;

import com.example.tournamentbackend.dto.TeamDTO;
import com.example.tournamentbackend.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO teamDTO) {
        TeamDTO createdTeam = teamService.createTeam(teamDTO);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable int id) {
        TeamDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<TeamDTO> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable int id, @RequestBody TeamDTO teamDTO) {
        TeamDTO updatedTeam = teamService.updateTeam(id, teamDTO);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable int id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<TeamDTO>> getTeamsByTournamentId(@PathVariable Long tournamentId) {
        List<TeamDTO> teams = teamService.getTeamsByTournamentId(tournamentId);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/{teamId}/tournament/{tournamentId}")
    public ResponseEntity<Void> addTeamToTournament(@PathVariable int teamId, @PathVariable Long tournamentId) {
        teamService.addTeamToTournament(teamId, tournamentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/tournament/{tournamentId}")
    public ResponseEntity<Void> removeTeamFromTournament(@PathVariable int teamId, @PathVariable Long tournamentId) {
        teamService.removeTeamFromTournament(teamId, tournamentId);
        return ResponseEntity.noContent().build();
    }
}