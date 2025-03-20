package com.example.tournamentbackend.service;

import com.example.tournamentbackend.dto.MatchDTO;

import java.util.List;

public interface MatchService {
    MatchDTO createMatch(MatchDTO matchDTO);
    MatchDTO getMatchById(Long id);
    List<MatchDTO> getAllMatches();
    List<MatchDTO> getMatchesByTournamentId(Long tournamentId);
    List<MatchDTO> getMatchesByTeamId(int teamId);
    MatchDTO updateMatch(Long id, MatchDTO matchDTO);
    void deleteMatch(Long id);
    MatchDTO recordMatchResult(Long id, Integer homeTeamScore, Integer awayTeamScore);
}