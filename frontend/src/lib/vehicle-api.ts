import apiClient from "./api";
import { Vehicle, VehicleRequest, VehicleStatistics, PaginatedVehicleResponse, ApiResponse, VehicleFilterParams, VehicleStatus } from "@/types/vehicle-types";

const VEHICLE_API_BASE = "/api/v1/vehicles";

export const vehicleApi = {
	async getVehicles(params?: VehicleFilterParams): Promise<PaginatedVehicleResponse> {
		const queryParams = new URLSearchParams();

		if (params?.page !== undefined) {
			queryParams.append("page", params.page.toString());
		}
		if (params?.size !== undefined) {
			queryParams.append("size", params.size.toString());
		}
		if (params?.status && params.status !== "all") {
			queryParams.append("status", params.status);
		}
		if (params?.search) {
			queryParams.append("search", params.search);
		}

		const url = `${VEHICLE_API_BASE}${queryParams.toString() ? `?${queryParams.toString()}` : ""}`;
		const response = await apiClient.get<ApiResponse<PaginatedVehicleResponse>>(url);
		return response.data.data;
	},

	async getVehicleById(id: number): Promise<Vehicle> {
		const response = await apiClient.get<ApiResponse<Vehicle>>(`${VEHICLE_API_BASE}/${id}`);
		return response.data.data;
	},

	async createVehicle(data: VehicleRequest): Promise<Vehicle> {
		const response = await apiClient.post<ApiResponse<Vehicle>>(VEHICLE_API_BASE, data);
		return response.data.data;
	},

	async updateVehicle(id: number, data: VehicleRequest): Promise<Vehicle> {
		const response = await apiClient.put<ApiResponse<Vehicle>>(`${VEHICLE_API_BASE}/${id}`, data);
		return response.data.data;
	},

	async deleteVehicle(id: number): Promise<void> {
		await apiClient.delete<ApiResponse<null>>(`${VEHICLE_API_BASE}/${id}`);
	},

	async getStatistics(): Promise<VehicleStatistics> {
		const response = await apiClient.get<ApiResponse<VehicleStatistics>>(`${VEHICLE_API_BASE}/statistics`);
		return response.data.data;
	},
};
