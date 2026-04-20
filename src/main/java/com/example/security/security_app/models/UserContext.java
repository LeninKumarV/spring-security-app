package com.example.security.security_app.models;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class UserContext {

    private static final ThreadLocal<UserContext> context = new ThreadLocal<>();

    private String userName;
    private String tenantId;
    private String token;
    private String requestId;
    private Set<String> rolesList;
    private String firstName;
    private String lastName;
    private Set<String> license;

    public static UserContext get() {
        return context.get();
    }
    public static void set(UserContext context) {
        UserContext.context.set(context);
    }
    public static void clear() {
        context.remove();
    }
}
