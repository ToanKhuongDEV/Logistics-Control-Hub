package com.logistics.hub.feature.audit.context;

public final class AuditRequestContextHolder {

    private static final ThreadLocal<AuditRequestContext> HOLDER = new ThreadLocal<>();

    private AuditRequestContextHolder() {
    }

    public static void set(AuditRequestContext context) {
        HOLDER.set(context);
    }

    public static AuditRequestContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
