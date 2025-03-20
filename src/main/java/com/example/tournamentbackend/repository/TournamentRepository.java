package com.example.tournamentbackend.repository;

import com.example.tournamentbackend.model.Tournament;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository {
    Tournament save(Tournament tournament);
    Optional<Tournament> findById(Long id);
    List<Tournament> findAll();
    void update(Tournament tournament);
    void deleteById(Long id);
}