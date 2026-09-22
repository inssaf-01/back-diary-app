package backend.auth.dto;

import backend.user.AppUser;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        Long roleId) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getId());
    }
}