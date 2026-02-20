package porunit.w8.realtydb.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import porunit.w8.realtydb.data.RegisterRequest;
import porunit.w8.realtydb.data.domain.Role;
import porunit.w8.realtydb.data.domain.User;
import porunit.w8.realtydb.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.existsByUsernameIgnoreCase(req.username())) {
            throw new IllegalArgumentException("Username already exists: " + req.username());
        }
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new IllegalArgumentException("Email already exists: " + req.email());
        }
        User user = User.builder()
                .username(req.username().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .email(req.email().trim())
                .role(Role.USER)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return Optional.empty();
        }
        String username = auth.getName();
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public boolean hasRoleAdminInToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    @Transactional(readOnly = true)
    public User requireCurrentUser() {
        Optional<User> fromDb = getCurrentUser();
        if (fromDb.isPresent()) return fromDb.get();
        if (hasRoleAdminInToken()) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return User.builder()
                    .id(null)
                    .username(username)
                    .passwordHash("")
                    .email("config@local")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
        }
        throw new IllegalStateException("User not found or not authenticated");
    }

    @Transactional
    public User setRole(UUID userId, Role role) {
        User user = getById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }
}
