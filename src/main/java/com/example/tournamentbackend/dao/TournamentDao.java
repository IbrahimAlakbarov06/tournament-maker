package com.example.tournamentbackend.dao;

import com.example.tournamentbackend.model.Tournament;

import java.util.List;
import java.util.Optional;

public interface TournamentDao {
    Tournament save(Tournament tournament);
    Optional<Tournament> findById(Long id);
    List<Tournament> findAll();
    void update(Tournament tournament);
    void deleteById(Long id);
}