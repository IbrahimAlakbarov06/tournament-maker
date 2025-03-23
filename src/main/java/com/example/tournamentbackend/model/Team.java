package com.example.tournamentbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    private int id;
    private String name;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalDifference;
    private int goalsScored;
    private int goalsConceded;
    private String last5Games;
    private int points;
    private String logoPath;
}