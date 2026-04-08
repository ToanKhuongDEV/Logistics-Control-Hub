import apiClient from "./api";
import { Driver, DriverRequest, DriverStatistics, DriverFilterParams } from "@/types/driver-types";
import { PaginatedResponse } from "@/types/common-types";

export const driverApi = {
	async getDrivers(params: DriverFilterParams): Promise<PaginatedResponse<Driver>> {
		const searchParam = params.search ? `&search=${encodeURIComponent(params.search)}` : "";
		const depotParam = params.depotId !== undefined ? `&depotId=${params.depotId}` : "";
		const response = await apiClient.get(`/api/v1/drivers?page=${params.page}&size=${params.size}${searchParam}${depotParam}`);
		return response.data.data;
	},

	async getAll(): Promise<Driver[]> {
		const response = await apiClient.get("/api/v1/drivers?page=0&size=100");
		const responseData = response.data.data;
		if (Array.isArray(responseData)) {
			return responseData;
		}
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

	async getById(id: number): Promise<Driver> {
		const response = await apiClient.get(`/api/v1/drivers/${id}`);
		return response.data.data;
	},

	async create(data: DriverRequest): Promise<Driver> {
		const response = await apiClient.post("/api/v1/drivers", data);
		return response.data.data;
	},

	async update(id: number, data: DriverRequest): Promise<Driver> {
		const response = await apiClient.put(`/api/v1/drivers/${id}`, data);
		return response.data.data;
	},

	async delete(id: number): Promise<void> {
		await apiClient.delete(`/api/v1/drivers/${id}`);
	},

	async getStatistics(): Promise<DriverStatistics> {
		const response = await apiClient.get("/api/v1/drivers/statistics");
		return response.data.data;
	},
};

export type { Driver };
