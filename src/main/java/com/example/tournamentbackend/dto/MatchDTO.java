package com.example.tournamentbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private Long tournamentId;
    private int homeTeamId;
    private int awayTeamId;
    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private LocalDateTime matchDate;
    private String status;
    private String round;
    private String homeTeamName;
    private String awayTeamName;
}