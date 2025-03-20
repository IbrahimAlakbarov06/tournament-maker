package com.example.tournamentbackend.repository;

import com.example.tournamentbackend.model.Match;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    Match save(Match match);
    Optional<Match> findById(Long id);
    List<Match> findAll();
    List<Match> findByTournamentId(Long tournamentId);
    List<Match> findByTeamId(int teamId);
    void update(Match match);
    void deleteById(Long id);
}