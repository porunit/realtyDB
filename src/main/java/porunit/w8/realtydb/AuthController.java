package porunit.w8.realtydb;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import porunit.w8.realtydb.config.JwtService;
import porunit.w8.realtydb.data.LoginRequest;
import porunit.w8.realtydb.data.RegisterRequest;
import porunit.w8.realtydb.data.TokenResponse;
import porunit.w8.realtydb.data.domain.User;
import porunit.w8.realtydb.advice.UnauthorizedException;
import porunit.w8.realtydb.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.username:}")
    private String cfgUser;

    @Value("${auth.password:}")
    private String cfgPass;

    @Value("${security.jwt.ttl-minutes}")
    private long ttlMinutes;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req);
        List<String> roles = List.of(user.getAuthority());
        String token = jwtService.generateToken(user.getUsername(), roles, null);
        return new TokenResponse(token, "Bearer", ttlMinutes * 60);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse login(@RequestBody LoginRequest req) {
        if (req == null || req.username() == null || req.password() == null) {
            throw new UnauthorizedException("Invalid credentials");
        }
        var dbUser = userService.findByUsername(req.username());
        if (dbUser.isPresent()) {
            User u = dbUser.get();
            if (!u.isActive()) {
                throw new UnauthorizedException("Account is disabled");
            }
            if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
                throw new UnauthorizedException("Invalid credentials");
            }
            List<String> roles = List.of(u.getAuthority());
            String token = jwtService.generateToken(u.getUsername(), roles, null);
            return new TokenResponse(token, "Bearer", ttlMinutes * 60);
        }
        if (cfgUser != null && !cfgUser.isBlank() && req.username().equals(cfgUser) && req.password().equals(cfgPass)) {
            String token = jwtService.generateToken(req.username(), List.of("ROLE_ADMIN"), null);
            return new TokenResponse(token, "Bearer", ttlMinutes * 60);
        }
        throw new UnauthorizedException("Invalid credentials");
    }
}
