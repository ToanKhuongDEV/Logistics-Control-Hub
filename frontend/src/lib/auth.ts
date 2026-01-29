import apiClient from "./api";

export interface LoginRequest {
	username: string;
	password: string;
}

export interface LoginResponse {
	accessToken: string;
	refreshToken: string;
}

export interface User {
	id: number;
	username: string;
	email: string;
	fullName: string;
}

class AuthService {
	async login(username: string, password: string): Promise<LoginResponse> {
		const response = await apiClient.post<LoginResponse>("/api/v1/auth/login", {
			username,
			password,
		});

		const { accessToken, refreshToken } = response.data;

		// Store tokens in localStorage
		localStorage.setItem("accessToken", accessToken);
		localStorage.setItem("refreshToken", refreshToken);

		return response.data;
	}

	logout(): void {
		localStorage.removeItem("accessToken");
		localStorage.removeItem("refreshToken");
	}

	getAccessToken(): string | null {
		return localStorage.getItem("accessToken");
	}

	getRefreshToken(): string | null {
		return localStorage.getItem("refreshToken");
	}

	isAuthenticated(): boolean {
		return !!this.getAccessToken();
	}

	async getCurrentUser(): Promise<User> {
		const response = await apiClient.get<{ data: User }>("/api/v1/auth/me");
		return response.data.data;
	}
}

export const authService = new AuthService();
