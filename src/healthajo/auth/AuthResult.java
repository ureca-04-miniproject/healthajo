package healthajo.auth;

public final class AuthResult {

    public enum Role { USER, ADMIN }

    private final boolean success;
    private final Role    role;
    private final Long    userId;
    private final String  message;

    private AuthResult(boolean success, Role role, Long userId, String message) {
        this.success = success;
        this.role    = role;
        this.userId  = userId;
        this.message = message;
    }

    public static AuthResult ok(Role role)              { return new AuthResult(true,  role, null,   null); }
    public static AuthResult ok(Role role, Long userId) { return new AuthResult(true,  role, userId, null); }
    public static AuthResult fail(String message)       { return new AuthResult(false, null, null,   message); }

    public boolean isSuccess() { return success; }
    public Role    role()      { return role; }
    public Long    userId()    { return userId; }
    public String  message()   { return message; }
}
