package es.techbridge.techbridgehelprequest.domain.model;

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
