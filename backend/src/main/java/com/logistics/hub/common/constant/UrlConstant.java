package com.logistics.hub.common.constant;

public class UrlConstant {
    public static final String API_V1 = "/api/v1";

    private UrlConstant() {
    }

    public static class Auth {
        public static final String PREFIX = API_V1 + "/auth";
        public static final String LOGIN = "/login";
        public static final String REFRESH = "/refresh";
        public static final String ME = "/me";
        
        private Auth() {}
    }

    public static class Order {
        public static final String PREFIX = API_V1 + "/orders";
        public static final String BY_ID = "/{id}";
        
        private Order() {}
    }
    
    public static class Location {
        public static final String PREFIX = API_V1 + "/locations";
        public static final String BY_ID = "/{id}";
        
        private Location() {}
    }
}
