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
}