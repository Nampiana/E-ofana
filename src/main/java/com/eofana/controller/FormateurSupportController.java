package com.eofana.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class FormateurSupportController {

    private final JdbcTemplate jdbcTemplate;

    public FormateurSupportController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================
    // MES FORMATIONS
    // GET /api/v1/formations/mes-formations
    // =========================
    @GetMapping("/api/v1/formations/mes-formations")
    public ResponseEntity<?> getMesFormations(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "statut", required = false) String statut
    ) {
        Long idUserFormateur = extraireIdUserDepuisToken(authorizationHeader);

        if (idUserFormateur == null) {
            idUserFormateur = 4L; // fallback test : Tsiky
        }

        String filtreStatut = "";
        Object[] params;

        if (statut != null && !statut.isBlank()) {
            filtreStatut = " AND f.statut = ? ";
            params = new Object[]{idUserFormateur, statut};
        } else {
            params = new Object[]{idUserFormateur};
        }

        String sql = """
            SELECT
                f."idFormation" AS "idFormation",
                f.titre AS titre,
                f.description AS description,
                f.image AS image,
                f.duree AS duree,
                f.lieu AS lieu,
                f.prix AS prix,
                f."prixRemise" AS "prixRemise",
                f.statut AS statut,
                f."noteMoyenne" AS "noteMoyenne",
                f."nbAvis" AS "nbAvis",
                COALESCE(cat.nom, '') AS categorie,
                COALESCE(SUM(s."placesRestantes"), 0) AS "placesRestantes",
                COUNT(i."idInscription") FILTER (WHERE i.statut = 'valide') AS "nombreInscrits",
                COUNT(i."idInscription") FILTER (WHERE i."typeInsc" = 'reservation') AS "nombreReservations"
            FROM eofana.formations f
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            LEFT JOIN eofana.categories cat ON cat."idCategorie" = f."idCategorie"
            LEFT JOIN eofana."sessionsFormation" s ON s."idFormation" = f."idFormation"
            LEFT JOIN eofana.inscriptions i ON i."idSession" = s."idSession"
            WHERE c."idUser" = ?
        """ + filtreStatut + """
            GROUP BY f."idFormation", cat.nom
            ORDER BY f."createdAt" DESC
        """;

        List<Map<String, Object>> formations = jdbcTemplate.queryForList(sql, params);

        return ResponseEntity.ok(formations);
    }

    // =========================
    // PROFIL CENTRE
    // GET /api/v1/formateurs/profil
    // =========================
    @GetMapping("/api/v1/formateurs/profil")
    public ResponseEntity<?> getProfilCentre(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Long idUserFormateur = extraireIdUserDepuisToken(authorizationHeader);

        if (idUserFormateur == null) {
            idUserFormateur = 4L; // fallback test : Tsiky
        }

        List<Map<String, Object>> centres = jdbcTemplate.queryForList("""
            SELECT
                c."idCentre" AS "idCentre",
                c."idUser" AS "idUser",
                c.nom AS nom,
                c.logo AS logo,
                c.description AS description,
                c.services AS services,
                c.ville AS ville,
                c.adresse AS adresse,
                c.telephone AS telephone,
                c.email AS email,
                c."siteWeb" AS "siteWeb",
                c.abonnement AS abonnement,
                c."tauxCommission" AS "tauxCommission",
                c."frequenceReversement" AS "frequenceReversement",
                c."mobileMoneyOperateur" AS "mobileMoneyOperateur",
                c."mobileMoneyNumero" AS "mobileMoneyNumero",
                c.statut AS statut
            FROM eofana.centres c
            WHERE c."idUser" = ?
            LIMIT 1
        """, idUserFormateur);

        if (centres.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Aucun centre trouvé pour ce formateur");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(centres.get(0));
    }

    // =========================
    // FINANCES
    // GET /api/v1/formateurs/finances
    // =========================
    @GetMapping("/api/v1/formateurs/finances")
    public ResponseEntity<?> getFinances(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Long idUserFormateur = extraireIdUserDepuisToken(authorizationHeader);

        if (idUserFormateur == null) {
            idUserFormateur = 4L; // fallback test : Tsiky
        }

        Long revenusBruts = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(i."montantPaye"), 0)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
              AND i.statut = 'valide'
        """, Long.class, idUserFormateur);

        Long commissions = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(i.commission), 0)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
              AND i.statut = 'valide'
        """, Long.class, idUserFormateur);

        Long revenusNets = jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(i."montantFormateur"), 0)
            FROM eofana.inscriptions i
            JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
            JOIN eofana.formations f ON f."idFormation" = s."idFormation"
            JOIN eofana.centres c ON c."idCentre" = f."idCentre"
            WHERE c."idUser" = ?
              AND i.statut = 'valide'
        """, Long.class, idUserFormateur);

        Map<String, Object> response = new HashMap<>();
        response.put("revenusBruts", revenusBruts);
        response.put("commissions", commissions);
        response.put("revenusNets", revenusNets);
        response.put("prochainVirementMontant", revenusNets);
        response.put("prochainVirementDate", "Prochain lundi");
        response.put("message", "Finances chargées");

        return ResponseEntity.ok(response);
    }

    // =========================
    // HISTORIQUE VIREMENTS
    // GET /api/v1/formateurs/finances/virements
    // =========================
    @GetMapping("/api/v1/formateurs/finances/virements")
    public ResponseEntity<?> getVirements(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Long idUserFormateur = extraireIdUserDepuisToken(authorizationHeader);

        if (idUserFormateur == null) {
            idUserFormateur = 4L;
        }

        List<Map<String, Object>> virements = jdbcTemplate.queryForList("""
            SELECT
                v."idVirement" AS "idVirement",
                v."montantBrut" AS "montantBrut",
                v.commission AS commission,
                v."montantNet" AS "montantNet",
                v.operateur AS operateur,
                v."referenceVirement" AS "referenceVirement",
                v.statut AS statut,
                v."dateVirement" AS "dateVirement",
                v."createdAt" AS "createdAt"
            FROM eofana.virements v
            JOIN eofana.centres c ON c."idCentre" = v."idCentre"
            WHERE c."idUser" = ?
            ORDER BY v."createdAt" DESC
        """, idUserFormateur);

        return ResponseEntity.ok(virements);
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
    @GetMapping("/api/v1/formateurs/inscrits")
public ResponseEntity<?> getInscritsFormation(@RequestParam Long formationId) {
    List<Map<String, Object>> inscrits = jdbcTemplate.queryForList("""
        SELECT
            i."idInscription" AS "idInscription",
            u.nom AS nom,
            u.prenom AS prenom,
            u.email AS email,
            i.statut AS statut,
            i."typeInsc" AS "typeInsc",
            i."montantPaye" AS "montantPaye",
            i."montantFormateur" AS "montantFormateur",
            i."createdAt" AS "createdAt"
        FROM eofana.inscriptions i
        JOIN eofana.utilisateurs u ON u."idUser" = i."idUser"
        JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
        WHERE s."idFormation" = ?
        ORDER BY i."createdAt" DESC
    """, formationId);

    return ResponseEntity.ok(inscrits);
}

@DeleteMapping("/api/v1/formations/{id}")
public ResponseEntity<?> supprimerFormation(@PathVariable Long id) {
    Map<String, Object> response = new HashMap<>();

    try {
        jdbcTemplate.update("""
            DELETE FROM eofana.inscriptions
            WHERE "idSession" IN (
                SELECT "idSession"
                FROM eofana."sessionsFormation"
                WHERE "idFormation" = ?
            )
        """, id);

        jdbcTemplate.update("""
            DELETE FROM eofana."sessionsFormation"
            WHERE "idFormation" = ?
        """, id);

        int deleted = jdbcTemplate.update("""
            DELETE FROM eofana.formations
            WHERE "idFormation" = ?
        """, id);

        response.put("success", deleted > 0);
        response.put("message", deleted > 0 ? "Formation supprimée" : "Formation introuvable");

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Suppression impossible : " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

@DeleteMapping("/api/v1/formateurs/formations/{id}")
public ResponseEntity<?> supprimerFormationFormateur(@PathVariable Long id) {
    Map<String, Object> response = new HashMap<>();

    try {
        jdbcTemplate.update("""
            DELETE FROM eofana.inscriptions
            WHERE "idSession" IN (
                SELECT "idSession"
                FROM eofana."sessionsFormation"
                WHERE "idFormation" = ?
            )
        """, id);

        jdbcTemplate.update("""
            DELETE FROM eofana."sessionsFormation"
            WHERE "idFormation" = ?
        """, id);

        int deleted = jdbcTemplate.update("""
            DELETE FROM eofana.formations
            WHERE "idFormation" = ?
        """, id);

        if (deleted > 0) {
            response.put("success", true);
            response.put("message", "Formation supprimée avec succès");
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "Formation introuvable");
        return ResponseEntity.status(404).body(response);

    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Suppression impossible : " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}
@PostMapping("/api/v1/formateurs/formations")
public ResponseEntity<?> creerFormationFormateur(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestBody Map<String, Object> request
) {
    Map<String, Object> response = new HashMap<>();

    try {
        Long idUserFormateur = extraireIdUserDepuisToken(authorizationHeader);

        if (idUserFormateur == null) {
            idUserFormateur = 4L; // fallback test : Tsiky
        }

        Long idCentre = jdbcTemplate.queryForObject("""
            SELECT "idCentre"
            FROM eofana.centres
            WHERE "idUser" = ?
            LIMIT 1
        """, Long.class, idUserFormateur);

        Long idCategorie;

        if (request.get("idCategorie") != null && !String.valueOf(request.get("idCategorie")).isBlank()) {
            idCategorie = Long.valueOf(String.valueOf(request.get("idCategorie")));
        } else {
            idCategorie = jdbcTemplate.queryForObject("""
                SELECT "idCategorie"
                FROM eofana.categories
                ORDER BY "idCategorie"
                LIMIT 1
            """, Long.class);
        }

        String titre = String.valueOf(request.getOrDefault("titre", ""));
        String description = String.valueOf(request.getOrDefault("description", ""));
        String image = String.valueOf(request.getOrDefault("image", ""));
        String duree = String.valueOf(request.getOrDefault("duree", ""));
        String lieu = String.valueOf(request.getOrDefault("lieu", ""));

        Integer prix = Integer.valueOf(String.valueOf(request.getOrDefault("prix", "0")));
        Integer prixRemise = Integer.valueOf(String.valueOf(request.getOrDefault("prixRemise", prix)));

        int inserted = jdbcTemplate.update("""
            INSERT INTO eofana.formations (
                "idCentre",
                "idCategorie",
                titre,
                description,
                image,
                duree,
                lieu,
                prix,
                "prixRemise",
                statut,
                "noteMoyenne",
                "nbAvis",
                "createdAt",
                "updatedAt"
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'enAttente', 0, 0, NOW(), NOW())
        """,
            idCentre,
            idCategorie,
            titre,
            description,
            image,
            duree,
            lieu,
            prix,
            prixRemise
        );

        response.put("success", inserted > 0);
        response.put("message", "Formation créée avec succès. Elle est en attente de validation.");

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Création impossible : " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

}