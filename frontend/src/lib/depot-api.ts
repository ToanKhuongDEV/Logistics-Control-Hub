import api from "./api";
import { Depot, DepotRequest, DepotStatistics, PaginatedResponse } from "@/types/depot-types";

interface GetDepotsParams {
	page?: number;
	size?: number;
	search?: string;
}

export const depotApi = {
	async getDepots(params: GetDepotsParams = {}): Promise<PaginatedResponse<Depot>> {
		const { page = 0, size = 10, search } = params;
		const searchParam = search ? `&search=${encodeURIComponent(search)}` : "";
		const response = await api.get(`/api/v1/depots?page=${page}&size=${size}${searchParam}`);
		return response.data.data;
	},

	async getDepotById(id: number): Promise<Depot> {
		const response = await api.get(`/api/v1/depots/${id}`);
		return response.data.data;
	},

	async createDepot(data: DepotRequest): Promise<Depot> {
		const response = await api.post("/api/v1/depots", data);
		return response.data.data;
	},

	async updateDepot(id: number, data: DepotRequest): Promise<Depot> {
		const response = await api.put(`/api/v1/depots/${id}`, data);
		return response.data.data;
	},

	async deleteDepot(id: number): Promise<void> {
		await api.delete(`/api/v1/depots/${id}`);
	},

	async getStatistics(): Promise<DepotStatistics> {
		const response = await api.get("/api/v1/depots/statistics");
		return response.data.data;
	},
};
