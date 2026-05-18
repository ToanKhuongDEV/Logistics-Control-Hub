package com.logistics.hub.feature.auth.policy;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationPolicyTest {

    @Test
    void normalizeRole_shouldSupportDriverRole() {
        assertEquals(AuthorizationPolicy.ROLE_DRIVER, AuthorizationPolicy.normalizeRole("driver"));
    }

    @Test
    void normalizeRole_shouldRejectUserRole() {
        assertThrows(com.logistics.hub.common.exception.ValidationException.class,
                () -> AuthorizationPolicy.normalizeRole("USER"));
    }

    @Test
    void permissionsForRole_shouldLimitDriverToDeliveryPermissions() {
        Set<String> permissions = AuthorizationPolicy.permissionsForRole("DRIVER");

        assertTrue(permissions.contains(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ));
        assertTrue(permissions.contains(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_UPDATE));
        assertFalse(permissions.contains(AuthorizationPolicy.PERMISSION_ORDER_READ));
        assertFalse(permissions.contains(AuthorizationPolicy.PERMISSION_VEHICLE_READ));
        assertFalse(permissions.contains(AuthorizationPolicy.PERMISSION_DRIVER_MANAGE));
    }
}
