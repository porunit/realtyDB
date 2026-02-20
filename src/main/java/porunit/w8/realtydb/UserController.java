package porunit.w8.realtydb;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import porunit.w8.realtydb.data.AssignRoleRequest;
import porunit.w8.realtydb.data.UserDto;
import porunit.w8.realtydb.data.domain.User;
import porunit.w8.realtydb.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserDto> list() {
        return userService.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isActive(), u.getCreatedAt()))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    public UserDto assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest req) {
        User u = userService.setRole(id, req.role());
        return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getRole(), u.isActive(), u.getCreatedAt());
    }
}
