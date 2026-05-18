import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";

const API_URL = process.env.NEXT_PUBLIC_API_URL;
const AUTH_EXCLUDED_PATHS = [
	"/api/v1/auth/login",
	"/api/v1/auth/refresh",
	"/api/v1/auth/forgot-password",
	"/api/v1/auth/reset-password",
];
const PUBLIC_AUTH_ROUTES = ["/login", "/forgot-password", "/reset-password"];

// Create axios instance
const apiClient = axios.create({
	baseURL: API_URL,
	withCredentials: true,
	headers: {
		"Content-Type": "application/json",
	},
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
	(response) => response,
	async (error: AxiosError) => {
		const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
		if (!originalRequest) {
			return Promise.reject(error);
		}

		const requestUrl = originalRequest?.url ?? "";
		const isExcludedAuthRequest = AUTH_EXCLUDED_PATHS.some((path) => requestUrl.includes(path));

		// If 401 and not already retried, try to refresh token
		if (error.response?.status === 401 && !originalRequest._retry && !isExcludedAuthRequest) {
			originalRequest._retry = true;

			try {
				await axios.post(
					`${API_URL}/api/v1/auth/refresh`,
					{},
					{ withCredentials: true },
				);

				return apiClient(originalRequest);
			} catch (refreshError) {
				// Refresh failed; backend clears auth cookies when possible.
				const isPublicAuthRoute =
					typeof window !== "undefined" &&
					PUBLIC_AUTH_ROUTES.some((route) => window.location.pathname.startsWith(route));
				if (typeof window !== "undefined" && !isPublicAuthRoute) {
					window.location.href = "/login";
				}
				return Promise.reject(refreshError);
			}
		}

		return Promise.reject(error);
	},
);

export default apiClient;
