package com.example.tournamentbackend.service;

import com.example.tournamentbackend.dto.TournamentDTO;

import java.util.List;

public interface TournamentService {
    TournamentDTO createTournament(TournamentDTO tournamentDTO);
    TournamentDTO getTournamentById(Long id);
    List<TournamentDTO> getAllTournaments();
    TournamentDTO updateTournament(Long id, TournamentDTO tournamentDTO);
    void deleteTournament(Long id);
}