package com.eofana.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/formateurs")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class FormateurDashboardController {

    private final JdbcTemplate jdbcTemplate;

    public FormateurDashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/tableau-de-bord")
    public ResponseEntity<?> getDashboardSummary(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Map<String, Object> response = new HashMap<>();

        Long idUserFormateur = extraireIdUserDepuisToken(authorizationHeader);

        Integer totalInscrits;
        Integer reservations;
        Integer formationsActives;
        Integer placesRestantes;
        Long chiffreAffairesNetMois;
        Long revenusGeneres;

        if (idUserFormateur != null) {
            totalInscrits = countTotalInscritsByFormateur(idUserFormateur);
            reservations = countReservationsByFormateur(idUserFormateur);
            formationsActives = countFormationsByFormateur(idUserFormateur);
            placesRestantes = countPlacesRestantesByFormateur(idUserFormateur);
            chiffreAffairesNetMois = sumRevenusMoisByFormateur(idUserFormateur);
            revenusGeneres = sumRevenusTotalByFormateur(idUserFormateur);
        } else {
            // Fallback pour les tests si le frontend n'envoie pas encore le token.
            totalInscrits = countTotalInscritsGlobal();
            reservations = countReservationsGlobal();
            formationsActives = countFormationsGlobal();
            placesRestantes = countPlacesRestantesGlobal();
            chiffreAffairesNetMois = sumRevenusMoisGlobal();
            revenusGeneres = sumRevenusTotalGlobal();
        }

        response.put("totalInscrits", totalInscrits);
        response.put("nombreTotalInscrits", totalInscrits);

        response.put("reservations", reservations);
        response.put("formationsActives", formationsActives);
        response.put("placesRestantes", placesRestantes);

        response.put("revenuMois", chiffreAffairesNetMois);
        response.put("chiffreAffairesNetMois", chiffreAffairesNetMois);
        response.put("revenusGeneres", revenusGeneres);

        response.put("visitesDisponibles", false);
        response.put("nombreVisites", null);
        response.put("tauxInscription", null);

        response.put("message", "Tableau de bord formateur chargé");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tableau-de-bord/formations/{id}")
    public ResponseEntity<?> getCourseStats(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Integer nombreInscrits = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) 
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            WHERE s."idFormation" = ?
              AND i.statut = 'valide'
        """, Integer.class, id);

        Integer nombreReservations = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) 
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            WHERE s."idFormation" = ?
              AND i."typeInsc" = 'reservation'
        """, Integer.class, id);

        Integer placesRestantes = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(s."placesRestantes"), 0)
            FROM eofana."sessionsFormation" s
            WHERE s."idFormation" = ?
        """, Integer.class, id);

        Long revenusGeneres = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(i."montantFormateur"), 0)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            WHERE s."idFormation" = ?
              AND i.statut = 'valide'
        """, Long.class, id);

        response.put("idFormation", id);
        response.put("nombreInscrits", nombreInscrits);
        response.put("nombreReservations", nombreReservations);
        response.put("placesRestantes", placesRestantes);
        response.put("revenusGeneres", revenusGeneres);

        response.put("visitesDisponibles", false);
        response.put("nombreVisites", null);

        return ResponseEntity.ok(response);
    }

    private Long extraireIdUserDepuisToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        String token = authorizationHeader.replace("Bearer", "").trim();

        if (token.startsWith("formateur-token-")) {
            try {
                return Long.parseLong(token.replace("formateur-token-", ""));
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    private Integer countTotalInscritsByFormateur(Long idUserFormateur) {
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
              AND i.statut = 'valide'
        """, Integer.class, idUserFormateur);
    }

    private Integer countReservationsByFormateur(Long idUserFormateur) {
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
              AND i."typeInsc" = 'reservation'
        """, Integer.class, idUserFormateur);
    }

    private Integer countFormationsByFormateur(Long idUserFormateur) {
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM eofana.formations f
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
        """, Integer.class, idUserFormateur);
    }

    private Integer countPlacesRestantesByFormateur(Long idUserFormateur) {
        return jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(s."placesRestantes"), 0)
            FROM eofana."sessionsFormation" s
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
        """, Integer.class, idUserFormateur);
    }

    private Long sumRevenusMoisByFormateur(Long idUserFormateur) {
        return jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(i."montantFormateur"), 0)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
              AND i.statut = 'valide'
              AND DATE_TRUNC('month', i."createdAt") = DATE_TRUNC('month', CURRENT_DATE)
        """, Long.class, idUserFormateur);
    }

    private Long sumRevenusTotalByFormateur(Long idUserFormateur) {
        return jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(i."montantFormateur"), 0)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
              AND i.statut = 'valide'
        """, Long.class, idUserFormateur);
    }

    private Integer countTotalInscritsGlobal() {
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM eofana.inscriptions
            WHERE statut = 'valide'
        """, Integer.class);
    }

    private Integer countReservationsGlobal() {
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM eofana.inscriptions
            WHERE "typeInsc" = 'reservation'
        """, Integer.class);
    }

    private Integer countFormationsGlobal() {
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM eofana.formations
        """, Integer.class);
    }

    private Integer countPlacesRestantesGlobal() {
        return jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM("placesRestantes"), 0)
            FROM eofana."sessionsFormation"
        """, Integer.class);
    }

    private Long sumRevenusMoisGlobal() {
        return jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM("montantFormateur"), 0)
            FROM eofana.inscriptions
            WHERE statut = 'valide'
              AND DATE_TRUNC('month', "createdAt") = DATE_TRUNC('month', CURRENT_DATE)
        """, Long.class);
    }

    private Long sumRevenusTotalGlobal() {
        return jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM("montantFormateur"), 0)
            FROM eofana.inscriptions
            WHERE statut = 'valide'
        """, Long.class);
    }
}