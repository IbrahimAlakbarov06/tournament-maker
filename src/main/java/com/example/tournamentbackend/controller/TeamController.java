package com.example.tournamentbackend.controller;

import com.example.tournamentbackend.dto.TeamDTO;
import com.example.tournamentbackend.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO teamDTO) {
        TeamDTO createdTeam = teamService.createTeam(teamDTO);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable int id) {
        TeamDTO team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<TeamDTO> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable int id, @RequestBody TeamDTO teamDTO) {
        TeamDTO updatedTeam = teamService.updateTeam(id, teamDTO);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable int id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<TeamDTO>> getTeamsByTournamentId(@PathVariable Long tournamentId) {
        List<TeamDTO> teams = teamService.getTeamsByTournamentId(tournamentId);
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/{teamId}/tournament/{tournamentId}")
    public ResponseEntity<Void> addTeamToTournament(@PathVariable int teamId, @PathVariable Long tournamentId) {
        teamService.addTeamToTournament(teamId, tournamentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/tournament/{tournamentId}")
    public ResponseEntity<Void> removeTeamFromTournament(@PathVariable int teamId, @PathVariable Long tournamentId) {
        teamService.removeTeamFromTournament(teamId, tournamentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/logo")
    public ResponseEntity<String> uploadTeamLogo(
            @PathVariable int teamId,
            @RequestParam("logo") MultipartFile file) {

        try {
            System.out.println("Received upload request for team ID: " + teamId);
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());

            teamService.getTeamById(teamId);

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            // Save the file
            Files.copy(file.getInputStream(), filePath);

            // Update the team with the logo path
            String logoPath = filename;
            teamService.updateTeamLogo(teamId, logoPath);

            return ResponseEntity.ok(logoPath);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload logo: " + e.getMessage());
        }
    }

    @GetMapping("/{teamId}/logo")
    public ResponseEntity<Resource> getTeamLogo(@PathVariable int teamId) {
        try {
            TeamDTO team = teamService.getTeamById(teamId);

            if (team.getLogoPath() == null || team.getLogoPath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path logoPath = Paths.get(uploadDir).resolve(team.getLogoPath());
            Resource resource = new UrlResource(logoPath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // You may need to detect the actual content type
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}