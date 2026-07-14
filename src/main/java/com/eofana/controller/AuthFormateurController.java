package com.eofana.controller;

import com.eofana.entity.Utilisateur;
import com.eofana.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth-formateur")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class AuthFormateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthFormateurController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/connexion")
    public ResponseEntity<?> connexionFormateur(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String motDePasse = request.get("motDePasse");

        if (motDePasse == null || motDePasse.isBlank()) {
            motDePasse = request.get("password");
        }

        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            Map<String, Object> erreur = new HashMap<>();
            erreur.put("message", "Email et mot de passe obligatoires");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
        }

        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findByEmail(email);

        if (utilisateurOptional.isEmpty()) {
            Map<String, Object> erreur = new HashMap<>();
            erreur.put("message", "Email ou mot de passe incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erreur);
        }

        Utilisateur utilisateur = utilisateurOptional.get();

        String role = String.valueOf(utilisateur.getRole());

        if (!"FORMATEUR".equalsIgnoreCase(role)) {
            Map<String, Object> erreur = new HashMap<>();
            erreur.put("message", "Ce compte n'est pas un compte formateur");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erreur);
        }

        String motDePasseEnBase = utilisateur.getMotDePasse();

        boolean motDePasseCorrect =
                motDePasse.equals(motDePasseEnBase)
                        || passwordEncoder.matches(motDePasse, motDePasseEnBase);

        if (!motDePasseCorrect) {
            Map<String, Object> erreur = new HashMap<>();
            erreur.put("message", "Email ou mot de passe incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erreur);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", "formateur-token-" + utilisateur.getIdUser());
        response.put("typeUtilisateur", "FORMATEUR");
        response.put("idUser", utilisateur.getIdUser());
        response.put("nom", utilisateur.getNom());
        response.put("prenom", utilisateur.getPrenom());
        response.put("email", utilisateur.getEmail());
        response.put("role", utilisateur.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deconnexion")
    public ResponseEntity<?> deconnexionFormateur() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Déconnexion formateur réussie");
        return ResponseEntity.ok(response);
    }
}