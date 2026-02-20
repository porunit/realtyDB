package porunit.w8.realtydb.data;

import jakarta.validation.constraints.NotNull;
import porunit.w8.realtydb.data.domain.Role;

public record AssignRoleRequest(@NotNull Role role) {}
