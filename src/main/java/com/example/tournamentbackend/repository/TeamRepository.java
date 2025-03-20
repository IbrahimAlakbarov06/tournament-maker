//package com.example.tournamentbackend.repository;
//
//import com.example.tournamentbackend.config.DatabaseConfig;
//import com.example.tournamentbackend.config.DatabaseConfig;
//import com.example.tournamentbackend.model.Team;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class TeamRepository {
//    List<Team> teams = new ArrayList<>();
//
//    public TeamRepository() {
//    }
//
//    public void add(Team team) {
//        try (Connection connection = DatabaseConfig.getConnection()) {
//            String query = "INSERT INTO teams (name,played, wins, draws, losses, goal_difference, goals_scored, goals_conceded, last_5_games, points) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//            preparedStatement.setString(1, team.getName());
//            preparedStatement.setInt(2, team.getPlayed());
//            preparedStatement.setInt(3, team.getWins());
//            preparedStatement.setInt(4, team.getDraws());
//            preparedStatement.setInt(5, team.getLosses());
//            preparedStatement.setInt(6, team.getGoalDifference());
//            preparedStatement.setInt(7, team.getGoalsScored());
//            preparedStatement.setInt(8, team.getGoalsConceded());
//            preparedStatement.setString(9, team.getLast5Games());
//            preparedStatement.setInt(10, team.getPoints());
//            preparedStatement.executeUpdate();
//
//
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    public List<Team> getAllTeams() {
//        List<Team> teams = new ArrayList<>();
//        try (Connection connection = DatabaseConfig.getConnection()) {
//            String query = "SELECT * FROM teams";
//            PreparedStatement preparedStatement = connection.prepareStatement(query);
//            try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                while (resultSet.next()) {
//                    int id = resultSet.getInt("id");
//                    String name = resultSet.getString("name");
//                    int played = resultSet.getInt("played");
//                    int wins = resultSet.getInt("wins");
//                    int draws = resultSet.getInt("draws");
//                    int losses = resultSet.getInt("losses");
//                    int goalDifference = resultSet.getInt("goal_difference");
//                    int goalsScored = resultSet.getInt("goals_scored");
//                    int goalsConceded = resultSet.getInt("goals_conceded");
//                    String last5Games = resultSet.getString("last_5_games");
//                    int points = resultSet.getInt("points");
//
//                    teams.add(new Team(id, name, played, wins, draws, losses, goalDifference, goalsScored, goalsConceded, last5Games, points));
//                }
//            }
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//        return teams;
//    }
//
//
//}
