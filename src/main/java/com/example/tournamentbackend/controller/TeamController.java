package com.example.tournamentbackend.controller;

import com.example.tournamentbackend.dto.TeamDTO;
import com.example.tournamentbackend.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    @PostMapping(value = "/with-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeamDTO> createTeamWithLogo(
            @RequestPart("team") TeamDTO teamDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {

        // In your createTeamWithLogo method
        if (logo != null && !logo.isEmpty()) {
            String filename = saveLogoFile(logo);
            teamDTO.setPhotoPath(filename);
        }

        TeamDTO createdTeam = teamService.createTeam(teamDTO);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TeamDTO> updateTeamLogo(
            @PathVariable int id,
            @RequestPart("logo") MultipartFile logo) {

        TeamDTO team = teamService.getTeamById(id);

        // Delete old logo if it exists and is not the default
        if (team.getPhotoPath() != null && !team.getPhotoPath().contains("default-logo")) {
            try {
                Path oldLogoPath = Paths.get(uploadDir, team.getPhotoPath().substring(team.getPhotoPath().lastIndexOf("/") + 1));
                Files.deleteIfExists(oldLogoPath);
            } catch (IOException e) {
                // Log error but continue
                System.err.println("Could not delete old logo: " + e.getMessage());
            }
        }

        // Save new logo
        String photoPath = saveLogoFile(logo);
        team.setPhotoPath(photoPath);

        TeamDTO updatedTeam = teamService.updateTeam(id, team);
        return ResponseEntity.ok(updatedTeam);
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
        // Get team to potentially delete logo file
        TeamDTO team = teamService.getTeamById(id);

        // Delete logo file if it's not the default
        if (team.getPhotoPath() != null && !team.getPhotoPath().contains("default-logo")) {
            try {
                Path logoPath = Paths.get(uploadDir, team.getPhotoPath().substring(team.getPhotoPath().lastIndexOf("/") + 1));
                Files.deleteIfExists(logoPath);
            } catch (IOException e) {
                // Log error but continue with team deletion
                System.err.println("Could not delete logo file: " + e.getMessage());
            }
        }

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

    @GetMapping("/team-images/{filename}")
    public ResponseEntity<Resource> getTeamImage(@PathVariable String filename) throws IOException {
        Path imagePath = Paths.get(uploadDir).resolve(filename);
        Resource resource = new UrlResource(imagePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType determineMediaType(String filename) {
        if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (filename.toLowerCase().endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String saveLogoFile(MultipartFile logo) {
        try {
            // Create the upload directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate a unique filename to prevent overwriting
            String originalFilename = logo.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save the file
            Path path = Paths.get(uploadDir, uniqueFilename);
            Files.write(path, logo.getBytes());

            // Return just the filename to be stored in database
            return "/uploads/images/teams/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save logo: " + e.getMessage());
        }
    }
}