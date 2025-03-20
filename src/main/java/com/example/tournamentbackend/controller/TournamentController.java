package com.example.tournamentbackend.controller;

import com.example.tournamentbackend.dto.TournamentDTO;
import com.example.tournamentbackend.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    @Autowired
    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    public ResponseEntity<TournamentDTO> createTournament(@RequestBody TournamentDTO tournamentDTO) {
        TournamentDTO createdTournament = tournamentService.createTournament(tournamentDTO);
        return new ResponseEntity<>(createdTournament, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getTournamentById(@PathVariable Long id) {
        TournamentDTO tournament = tournamentService.getTournamentById(id);
        return ResponseEntity.ok(tournament);
    }

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getAllTournaments() {
        List<TournamentDTO> tournaments = tournamentService.getAllTournaments();
        return ResponseEntity.ok(tournaments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentDTO> updateTournament(@PathVariable Long id, @RequestBody TournamentDTO tournamentDTO) {
        TournamentDTO updatedTournament = tournamentService.updateTournament(id, tournamentDTO);
        return ResponseEntity.ok(updatedTournament);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }
}