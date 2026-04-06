package com.logistics.hub.feature.auth.policy;

import com.logistics.hub.common.exception.ValidationException;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class AuthorizationPolicy {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DISPATCHER = "DISPATCHER";
    public static final String ROLE_USER = "USER";

    public static final String PERMISSION_ACCOUNT_MANAGE = "account.manage";
    public static final String PERMISSION_AUDIT_READ = "audit.read";
    public static final String PERMISSION_COMPANY_MANAGE = "company.manage";
    public static final String PERMISSION_DASHBOARD_READ = "dashboard.read";
    public static final String PERMISSION_DEPOT_READ = "depot.read";
    public static final String PERMISSION_DEPOT_MANAGE = "depot.manage";
    public static final String PERMISSION_DRIVER_READ = "driver.read";
    public static final String PERMISSION_DRIVER_MANAGE = "driver.manage";
    public static final String PERMISSION_ORDER_READ = "order.read";
    public static final String PERMISSION_ORDER_MANAGE = "order.manage";
    public static final String PERMISSION_ORDER_CANCEL_CONFIRMED = "order.cancel.confirmed";
    public static final String PERMISSION_ROUTING_EXECUTE = "routing.execute";
    public static final String PERMISSION_ROUTING_READ = "routing.read";
    public static final String PERMISSION_SETTINGS_READ = "settings.read";
    public static final String PERMISSION_VEHICLE_MANAGE = "vehicle.manage";
    public static final String PERMISSION_VEHICLE_READ = "vehicle.read";
    public static final String PERMISSION_VEHICLE_REASSIGN = "vehicle.reassign";

    private AuthorizationPolicy() {
    }

    public static String normalizeRole(String role) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!Set.of(ROLE_ADMIN, ROLE_DISPATCHER, ROLE_USER).contains(normalizedRole)) {
            throw new ValidationException("Role is not supported.");
        }
        return normalizedRole;
    }

    public static boolean hasGlobalScope(String role) {
        return ROLE_ADMIN.equals(normalizeRole(role));
    }

    public static Set<String> permissionsForRole(String role) {
        String normalizedRole = normalizeRole(role);
        LinkedHashSet<String> permissions = new LinkedHashSet<>();

        permissions.add(PERMISSION_DASHBOARD_READ);
        permissions.add(PERMISSION_DEPOT_READ);
        permissions.add(PERMISSION_DRIVER_READ);
        permissions.add(PERMISSION_ORDER_READ);
        permissions.add(PERMISSION_ROUTING_READ);
        permissions.add(PERMISSION_SETTINGS_READ);
        permissions.add(PERMISSION_VEHICLE_READ);

        if (ROLE_DISPATCHER.equals(normalizedRole) || ROLE_ADMIN.equals(normalizedRole)) {
            permissions.add(PERMISSION_ORDER_MANAGE);
            permissions.add(PERMISSION_ROUTING_EXECUTE);
            permissions.add(PERMISSION_VEHICLE_MANAGE);
        }

        if (ROLE_ADMIN.equals(normalizedRole)) {
            permissions.add(PERMISSION_ACCOUNT_MANAGE);
            permissions.add(PERMISSION_AUDIT_READ);
            permissions.add(PERMISSION_COMPANY_MANAGE);
            permissions.add(PERMISSION_DEPOT_MANAGE);
            permissions.add(PERMISSION_DRIVER_MANAGE);
            permissions.add(PERMISSION_ORDER_CANCEL_CONFIRMED);
            permissions.add(PERMISSION_VEHICLE_REASSIGN);
        }

        return Set.copyOf(permissions);
    }
}
