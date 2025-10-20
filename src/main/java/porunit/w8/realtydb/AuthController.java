package porunit.w8.realtydb;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import porunit.w8.realtydb.config.JwtService;
import porunit.w8.realtydb.data.LoginRequest;
import porunit.w8.realtydb.data.TokenResponse;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Value("${auth.username}")
    private String cfgUser;

    @Value("${auth.password}")
    private String cfgPass;

    @Value("${security.jwt.ttl-minutes}")
    private long ttlMinutes;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse login(@RequestBody LoginRequest req) {
        if (req == null || req.username() == null || req.password() == null
                || !req.username().equals(cfgUser) || !req.password().equals(cfgPass)) {
            throw new Unauthorized("Invalid credentials");
        }
        String token = jwtService.generateToken(req.username(), List.of("ROLE_API"), null);
        return new TokenResponse(token, "Bearer", ttlMinutes * 60);
    }

    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    private static class Unauthorized extends RuntimeException {
        public Unauthorized(String m) { super(m); }
    }
}
