package com.example.tournamentbackend.service.impl;

import com.example.tournamentbackend.dto.TournamentDTO;
import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Tournament;
import com.example.tournamentbackend.repository.TournamentRepository;
import com.example.tournamentbackend.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;

    @Autowired
    public TournamentServiceImpl(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public TournamentDTO createTournament(TournamentDTO tournamentDTO) {
        Tournament tournament = convertToEntity(tournamentDTO);
        Tournament savedTournament = tournamentRepository.save(tournament);
        return convertToDTO(savedTournament);
    }

    @Override
    public TournamentDTO getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
        return convertToDTO(tournament);
    }

    @Override
    public List<TournamentDTO> getAllTournaments() {
        return tournamentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TournamentDTO updateTournament(Long id, TournamentDTO tournamentDTO) {
        Tournament existingTournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));

        existingTournament.setName(tournamentDTO.getName());
        existingTournament.setStartDate(tournamentDTO.getStartDate());
        existingTournament.setEndDate(tournamentDTO.getEndDate());
        existingTournament.setType(tournamentDTO.getType());
        existingTournament.setStatus(tournamentDTO.getStatus());

        tournamentRepository.update(existingTournament);

        return convertToDTO(existingTournament);
    }

    @Override
    public void deleteTournament(Long id) {
        tournamentRepository.deleteById(id);
    }

    private TournamentDTO convertToDTO(Tournament tournament) {
        return new TournamentDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getType(),
                tournament.getStatus()
        );
    }

    private Tournament convertToEntity(TournamentDTO tournamentDTO) {
        Tournament tournament = new Tournament();
        tournament.setId(tournamentDTO.getId());
        tournament.setName(tournamentDTO.getName());
        tournament.setStartDate(tournamentDTO.getStartDate());
        tournament.setEndDate(tournamentDTO.getEndDate());
        tournament.setType(tournamentDTO.getType());
        tournament.setStatus(tournamentDTO.getStatus() != null ? tournamentDTO.getStatus() : "upcoming");
        return tournament;
    }
}