package com.example.tournamentbackend.service.impl;

import com.example.tournamentbackend.dto.MatchDTO;
import com.example.tournamentbackend.exception.ResourceNotFoundException;
import com.example.tournamentbackend.model.Match;
import com.example.tournamentbackend.dao.MatchDao;
import com.example.tournamentbackend.dao.TeamDao;
import com.example.tournamentbackend.service.MatchService;
import com.example.tournamentbackend.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchDao matchRepository;
    private final TeamDao teamRepository;
    private final TeamService teamService;

    @Autowired
    public MatchServiceImpl(MatchDao matchRepository, TeamDao teamRepository, TeamService teamService) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.teamService = teamService;
    }

    @Override
    public MatchDTO createMatch(MatchDTO matchDTO) {
        // Validate that teams exist
        verifyTeamExists(matchDTO.getHomeTeamId());
        verifyTeamExists(matchDTO.getAwayTeamId());

        Match match = convertToEntity(matchDTO);
        match.setStatus("scheduled");
        if (match.getMatchDate() == null) {
            match.setMatchDate(LocalDateTime.now().plusDays(7)); // Default to one week from now
        }
        Match savedMatch = matchRepository.save(match);
        return enrichMatchDTO(convertToDTO(savedMatch));
    }

    @Override
    public MatchDTO getMatchById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
        return enrichMatchDTO(convertToDTO(match));
    }

    @Override
    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(this::convertToDTO)
                .map(this::enrichMatchDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDTO> getMatchesByTournamentId(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId).stream()
                .map(this::convertToDTO)
                .map(this::enrichMatchDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDTO> getMatchesByTeamId(int teamId) {
        return matchRepository.findByTeamId(teamId).stream()
                .map(this::convertToDTO)
                .map(this::enrichMatchDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MatchDTO updateMatch(Long id, MatchDTO matchDTO) {
        Match existingMatch = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));

        // Validate that teams exist
        verifyTeamExists(matchDTO.getHomeTeamId());
        verifyTeamExists(matchDTO.getAwayTeamId());

        existingMatch.setTournamentId(matchDTO.getTournamentId());
        existingMatch.setHomeTeamId(matchDTO.getHomeTeamId());
        existingMatch.setAwayTeamId(matchDTO.getAwayTeamId());
        existingMatch.setMatchDate(matchDTO.getMatchDate());
        existingMatch.setRound(matchDTO.getRound());
        existingMatch.setStatus(matchDTO.getStatus());

        // Only update scores if they are provided and match is completed
        if (matchDTO.getHomeTeamScore() != null && matchDTO.getAwayTeamScore() != null) {
            existingMatch.setHomeTeamScore(matchDTO.getHomeTeamScore());
            existingMatch.setAwayTeamScore(matchDTO.getAwayTeamScore());
        }

        matchRepository.update(existingMatch);
        return enrichMatchDTO(convertToDTO(existingMatch));
    }

    @Override
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

    @Override
    @Transactional
    public MatchDTO recordMatchResult(Long id, Integer homeTeamScore, Integer awayTeamScore) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));

        // Update match scores and status
        match.setHomeTeamScore(homeTeamScore);
        match.setAwayTeamScore(awayTeamScore);
        match.setStatus("completed");
        matchRepository.update(match);

        // Update team statistics
        updateTeamStats(match);

        return enrichMatchDTO(convertToDTO(match));
    }

    private void updateTeamStats(Match match) {
        int homeTeamId = match.getHomeTeamId();
        int awayTeamId = match.getAwayTeamId();
        int homeScore = match.getHomeTeamScore();
        int awayScore = match.getAwayTeamScore();

        // Determine the result for each team
        String homeResult;
        String awayResult;

        if (homeScore > awayScore) {
            homeResult = "W";
            awayResult = "L";
        } else if (homeScore < awayScore) {
            homeResult = "L";
            awayResult = "W";
        } else {
            homeResult = "D";
            awayResult = "D";
        }

        // Update both teams' statistics
        teamService.updateTeamStats(homeTeamId, homeScore, awayScore, homeResult);
        teamService.updateTeamStats(awayTeamId, awayScore, homeScore, awayResult);
    }

    private void verifyTeamExists(int teamId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
    }

    private MatchDTO convertToDTO(Match match) {
        return new MatchDTO(
                match.getId(),
                match.getTournamentId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                match.getHomeTeamScore(),
                match.getAwayTeamScore(),
                match.getMatchDate(),
                match.getStatus(),
                match.getRound(),
                null, // homeTeamName to be set in enrichMatchDTO
                null  // awayTeamName to be set in enrichMatchDTO
        );
    }

    private Match convertToEntity(MatchDTO matchDTO) {
        Match match = new Match();
        match.setId(matchDTO.getId());
        match.setTournamentId(matchDTO.getTournamentId());
        match.setHomeTeamId(matchDTO.getHomeTeamId());
        match.setAwayTeamId(matchDTO.getAwayTeamId());
        match.setHomeTeamScore(matchDTO.getHomeTeamScore());
        match.setAwayTeamScore(matchDTO.getAwayTeamScore());
        match.setMatchDate(matchDTO.getMatchDate());
        match.setStatus(matchDTO.getStatus());
        match.setRound(matchDTO.getRound());
        return match;
    }

    private MatchDTO enrichMatchDTO(MatchDTO matchDTO) {
        // Add team names to the DTO for convenience
        teamRepository.findById(matchDTO.getHomeTeamId())
                .ifPresent(team -> matchDTO.setHomeTeamName(team.getName()));

        teamRepository.findById(matchDTO.getAwayTeamId())
                .ifPresent(team -> matchDTO.setAwayTeamName(team.getName()));

        return matchDTO;
    }
}