import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";

const API_URL = process.env.NEXT_PUBLIC_API_URL;
const AUTH_EXCLUDED_PATHS = [
	"/api/v1/auth/login",
	"/api/v1/auth/refresh",
	"/api/v1/auth/forgot-password",
	"/api/v1/auth/reset-password",
];

// Create axios instance
const apiClient = axios.create({
	baseURL: API_URL,
	withCredentials: true,
	headers: {
		"Content-Type": "application/json",
	},
});

// Request interceptor to add token
apiClient.interceptors.request.use(
	(config: InternalAxiosRequestConfig) => {
		const token = localStorage.getItem("accessToken");
		if (token) {
			config.headers.Authorization = `Bearer ${token}`;
		}
		return config;
	},
	(error: AxiosError) => {
		return Promise.reject(error);
	},
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
	(response) => response,
	async (error: AxiosError) => {
		const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
		const requestUrl = originalRequest?.url ?? "";
		const isExcludedAuthRequest = AUTH_EXCLUDED_PATHS.some((path) => requestUrl.includes(path));

		// If 401 and not already retried, try to refresh token
		if (error.response?.status === 401 && !originalRequest._retry && !isExcludedAuthRequest) {
			originalRequest._retry = true;

			try {
				const response = await axios.post(
					`${API_URL}/api/v1/auth/refresh`,
					{},
					{ withCredentials: true },
				);

				const { accessToken } = response.data.data;

				localStorage.setItem("accessToken", accessToken);

				originalRequest.headers.Authorization = `Bearer ${accessToken}`;
				return apiClient(originalRequest);
			} catch (refreshError) {
				// Refresh failed, clear tokens and redirect to login
				localStorage.removeItem("accessToken");
				window.location.href = "/login";
				return Promise.reject(refreshError);
			}
		}

		return Promise.reject(error);
	},
);

export default apiClient;
