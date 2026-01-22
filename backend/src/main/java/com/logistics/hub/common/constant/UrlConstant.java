package com.logistics.hub.common.constant;

public class UrlConstant {
    public static final String API_V1 = "/api/v1";

    private UrlConstant() {
    }

    public static class Order {
        private static final String PRE_FIX = API_V1 + "/orders";
        public static final String ORDER_COMMON = PRE_FIX;
        public static final String ORDER_ID = PRE_FIX + "/{id}";
        
        private Order() {
        }
    }
}
