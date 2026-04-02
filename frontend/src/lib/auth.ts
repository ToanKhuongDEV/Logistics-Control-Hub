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
	role: string;
}

export interface CreateAccountRequest {
	username: string;
	fullName: string;
	email: string;
	password: string;
}

export interface ChangePasswordRequest {
	currentPassword: string;
	newPassword: string;
}

export interface ForgotPasswordRequest {
	email: string;
}

export interface ResetPasswordRequest {
	token: string;
	newPassword: string;
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

	async createAccount(payload: CreateAccountRequest): Promise<User> {
		const response = await apiClient.post<{ data: User }>("/api/v1/auth/accounts", payload);
		return response.data.data;
	}

	async changePassword(payload: ChangePasswordRequest): Promise<void> {
		await apiClient.post("/api/v1/auth/change-password", payload);
	}

	async forgotPassword(payload: ForgotPasswordRequest): Promise<void> {
		await apiClient.post("/api/v1/auth/forgot-password", payload);
	}

	async resetPassword(payload: ResetPasswordRequest): Promise<void> {
		await apiClient.post("/api/v1/auth/reset-password", payload);
	}
}

export const authService = new AuthService();
