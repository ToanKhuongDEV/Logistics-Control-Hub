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

        private Auth() {
        }
    }

    public static class Order {
        public static final String PREFIX = API_V1 + "/orders";
        public static final String BY_ID = "/{id}";
        public static final String STATISTICS = "/statistics";

        private Order() {
        }
    }

    public static class Location {
        public static final String PREFIX = API_V1 + "/locations";
        public static final String BY_ID = "/{id}";

        private Location() {
        }
    }

    public static class Vehicle {
        public static final String PREFIX = API_V1 + "/vehicles";
        public static final String BY_ID = "/{id}";
        public static final String STATISTICS = "/statistics";

        private Vehicle() {
        }
    }

    public static class Company {
        public static final String PREFIX = API_V1 + "/company";

        private Company() {
        }
    }

    public static class Driver {
        public static final String PREFIX = API_V1 + "/drivers";
        public static final String BY_ID = "/{id}";
        public static final String AVAILABLE = "/available";
        public static final String STATISTICS = "/statistics";

        private Driver() {
        }
    }

    public static class Depot {
        public static final String PREFIX = API_V1 + "/depots";
        public static final String BY_ID = "/{id}";

        private Depot() {
        }
    }

    public static class Routing {
        public static final String PREFIX = API_V1 + "/routing";
        public static final String OPTIMIZE = "/optimize";
        public static final String RUNS = "/runs";
        public static final String RUN_BY_ID = "/runs/{id}";

        private Routing() {
        }
    }
}
