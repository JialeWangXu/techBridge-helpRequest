package es.techbridge.techbridgehelprequest.domain.model.user;

public enum UserRole {
    SENIOR,
    VOLUNTEER;

    public static UserRole from(String authority) {
        return UserRole.valueOf(authority.replace("ROLE_", ""));
    }

    public String jwtClaimValue() {
        return this.name();
    }
}
