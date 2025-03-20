package com.example.tournamentbackend.repository;

import com.example.tournamentbackend.model.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRepository {
    Team save(Team team);
    Optional<Team> findById(int id);
    List<Team> findAll();
    void update(Team team);
    void deleteById(int id);
    List<Team> findByTournamentId(Long tournamentId);
    void addTeamToTournament(int teamId, Long tournamentId);
    void removeTeamFromTournament(int teamId, Long tournamentId);
}