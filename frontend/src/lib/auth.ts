import apiClient from "./api";
import { PaginatedResponse } from "@/types/common-types";

export type UserRole = "ADMIN" | "DISPATCHER" | "USER";
export type UserPermission =
	| "account.manage"
	| "audit.read"
	| "company.manage"
	| "dashboard.read"
	| "depot.read"
	| "depot.manage"
	| "driver.read"
	| "driver.manage"
	| "order.read"
	| "order.manage"
	| "order.cancel.confirmed"
	| "routing.execute"
	| "routing.read"
	| "settings.read"
	| "vehicle.manage"
	| "vehicle.read"
	| "vehicle.reassign";

export interface AssignedDepot {
	id: number;
	name: string;
}

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
	role: UserRole;
	permissions?: UserPermission[];
	assignedDepots?: AssignedDepot[];
}

export interface AccountQueryParams {
	page: number;
	size: number;
	search?: string;
	role?: UserRole;
	depotId?: number;
}

export interface CreateAccountRequest {
	username: string;
	fullName: string;
	email: string;
	password: string;
	role: UserRole;
	assignedDepotIds?: number[];
}

export interface UpdateAccountRequest {
	fullName: string;
	email: string;
	role: UserRole;
	assignedDepotIds?: number[];
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

	async getAccounts(params: AccountQueryParams): Promise<PaginatedResponse<User>> {
		const queryParams = new URLSearchParams({
			page: params.page.toString(),
			size: params.size.toString(),
		});

		if (params.search) {
			queryParams.set("search", params.search);
		}
		if (params.role) {
			queryParams.set("role", params.role);
		}
		if (params.depotId !== undefined) {
			queryParams.set("depotId", params.depotId.toString());
		}

		const response = await apiClient.get<{ data: PaginatedResponse<User> }>(`/api/v1/auth/accounts?${queryParams.toString()}`);
		return response.data.data;
	}

	async getAccountById(id: number): Promise<User> {
		const response = await apiClient.get<{ data: User }>(`/api/v1/auth/accounts/${id}`);
		return response.data.data;
	}

	async updateAccount(id: number, payload: UpdateAccountRequest): Promise<User> {
		const response = await apiClient.put<{ data: User }>(`/api/v1/auth/accounts/${id}`, payload);
		return response.data.data;
	}

	async deleteAccount(id: number): Promise<void> {
		await apiClient.delete(`/api/v1/auth/accounts/${id}`);
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

export function hasPermission(user: User | null | undefined, permission: UserPermission): boolean {
	return !!user?.permissions?.includes(permission);
}
