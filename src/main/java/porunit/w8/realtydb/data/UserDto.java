package porunit.w8.realtydb.data;

import porunit.w8.realtydb.data.domain.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        Role role,
        boolean active,
        OffsetDateTime createdAt
) {}
