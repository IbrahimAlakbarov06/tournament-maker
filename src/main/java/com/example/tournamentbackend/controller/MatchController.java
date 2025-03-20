package com.example.tournamentbackend.controller;

import com.example.tournamentbackend.dto.MatchDTO;
import com.example.tournamentbackend.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchDTO> createMatch(@RequestBody MatchDTO matchDTO) {
        MatchDTO createdMatch = matchService.createMatch(matchDTO);
        return new ResponseEntity<>(createdMatch, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable Long id) {
        MatchDTO match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches() {
        List<MatchDTO> matches = matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<MatchDTO>> getMatchesByTournamentId(@PathVariable Long tournamentId) {
        List<MatchDTO> matches = matchService.getMatchesByTournamentId(tournamentId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<MatchDTO>> getMatchesByTeamId(@PathVariable int teamId) {
        List<MatchDTO> matches = matchService.getMatchesByTeamId(teamId);
        return ResponseEntity.ok(matches);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchDTO> updateMatch(@PathVariable Long id, @RequestBody MatchDTO matchDTO) {
        MatchDTO updatedMatch = matchService.updateMatch(id, matchDTO);
        return ResponseEntity.ok(updatedMatch);
    }

    @PatchMapping("/{id}/result")
    public ResponseEntity<MatchDTO> recordMatchResult(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> result) {

        Integer homeTeamScore = result.get("homeTeamScore");
        Integer awayTeamScore = result.get("awayTeamScore");

        if (homeTeamScore == null || awayTeamScore == null) {
            return ResponseEntity.badRequest().build();
        }

        MatchDTO updatedMatch = matchService.recordMatchResult(id, homeTeamScore, awayTeamScore);
        return ResponseEntity.ok(updatedMatch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}