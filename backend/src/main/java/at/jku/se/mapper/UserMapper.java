package at.jku.se.mapper;

import at.jku.se.dto.response.UserResponse;
import at.jku.se.entity.User;

/** Maps {@link User} entities to response DTOs. */
public class UserMapper {

    private UserMapper() {
    }

    /**
     * Converts a User entity to a UserResponse DTO (password hash is never included).
     *
     * @param user the entity to convert
     * @return the response DTO
     */
    public static UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.id = user.id;
        r.email = user.email;
        r.role = user.role;
        r.createdAt = user.createdAt;
        return r;
    }
}
