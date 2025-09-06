package user.user_service.adapter.web;

import user.user_service.application.dto.LoginRequest;
import user.user_service.application.dto.RegisterRequest;
import user.user_service.application.dto.UserResponse;
import user.user_service.domain.model.User;
import user.user_service.infrastructure.exception.ApiResponse;
import user.user_service.infrastructure.exception.BadRequestException;
import user.user_service.infrastructure.repository.UserRepository;
import user.user_service.infrastructure.security.JwtProvider;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final UserRepository repo;
    private final JwtProvider jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository repo, JwtProvider jwt) {
        this.repo = repo;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (repo.findByEmail(req.getEmail()).isPresent()) {
            throw new BadRequestException(
                    "Registration failed",
                    List.of("Email already used: " + req.getEmail()));
        }
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setRole("USER");
        u.setCreatedAt(Instant.now());
        repo.save(u);

        Map<String, Object> data = Map.of(
                "email", u.getEmail(),
                "name", u.getName(),
                "createdAt", u.getCreatedAt());

        return ResponseEntity.ok(ApiResponse.success("Registered", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest req) {
        var userOpt = repo.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            throw new BadRequestException("Invalid credentials", List.of("Email not found"));
        }

        var user = userOpt.get();
        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials", List.of("Wrong password"));
        }

        String token = jwt.generateToken(user.getEmail(), user.getRole(), user.getName());

        Map<String, Object> data = Map.of(
                "token", token,
                "email", user.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }

    @GetMapping("/users/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return repo.findByEmail(email)
                .map(user -> ResponseEntity.ok(
                        new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole())))
                .orElse(ResponseEntity.notFound().build());
    }
}