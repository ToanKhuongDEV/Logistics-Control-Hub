import apiClient from "./api";

export interface Driver {
	id: number;
	name: string;
	licenseNumber: string;
	phoneNumber: string;
	email?: string;
}

export const driverApi = {
	async getAll(): Promise<Driver[]> {
		const response = await apiClient.get("/api/v1/drivers?page=0&size=100");
		// Handle both paginated and direct array responses
		const responseData = response.data.data;
		if (Array.isArray(responseData)) {
			return responseData;
		}
		// If it's a paginated response with {data: [...], pagination: {...}}
		if (responseData && Array.isArray(responseData.data)) {
			return responseData.data;
		}
		console.error("Unexpected driver API response format:", responseData);
		return [];
	},

	async getAvailable(includeDriverId?: number): Promise<Driver[]> {
		const url = includeDriverId ? `/api/v1/drivers/available?includeDriverId=${includeDriverId}` : "/api/v1/drivers/available";
		const response = await apiClient.get(url);
		return response.data.data;
	},
};
