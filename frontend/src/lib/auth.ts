import apiClient from "./api";

export interface LoginRequest {
	username: string;
	password: string;
}

export interface LoginResponse {
	accessToken: string;
}

export interface User {
	id: number;
	username: string;
	email: string;
	fullName: string;
}

class AuthService {
	async login(username: string, password: string): Promise<LoginResponse> {
		const response = await apiClient.post<{ data: LoginResponse }>("/api/v1/auth/login", {
			username,
			password,
		});

		const { accessToken } = response.data.data;

		// Access token is stored client-side; refresh token is handled via HttpOnly cookie.
		localStorage.setItem("accessToken", accessToken);

		return response.data.data;
	}

	async logout(): Promise<void> {
		try {
			await apiClient.post("/api/v1/auth/logout");
		} catch {
			// Ignore logout transport errors and still clear local auth state.
		}

		localStorage.removeItem("accessToken");
	}

	getAccessToken(): string | null {
		return localStorage.getItem("accessToken");
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
