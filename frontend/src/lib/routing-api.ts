import apiClient from "./api";

// Base URL constant
const ROUTING_API_BASE = "/api/v1/routing";

// Type definitions
export interface RouteStop {
	id: number;
	stopSequence: number;
	locationId: number;
	orderId: number | null;
	distanceFromPrevKm: number;
	durationFromPrevMin: number;
	arrivalTime?: string;
	departureTime?: string;
	latitude?: number;
	longitude?: number;
}

export interface Route {
	id: number;
	vehicleId: number;
	polyline: string;
	totalDistanceKm: number;
	totalDurationMin: number;
	totalCost: number;
	status: string;
	stops: RouteStop[];
}

export interface RoutingRun {
	id: number;
	status: string;
	startTime: string;
	endTime: string;
	createdAt: string;
	totalDistanceKm: number;
	totalCost: number;
	configuration: string;
	routes: Route[];
}

export interface RoutingHistoryPage {
	content: RoutingRun[];
	totalElements: number;
	totalPages: number;
	currentPage: number;
	pageSize: number;
}

interface ApiResponse<T> {
	code: number;
	message: string;
	data: T;
}

// API methods
export const routingApi = {
	async optimize(depotId: number): Promise<RoutingRun> {
		const response = await apiClient.post<ApiResponse<RoutingRun>>(`${ROUTING_API_BASE}/optimize?depotId=${depotId}`);
		return response.data.data;
	},

	async getRoutingRunById(id: number): Promise<RoutingRun> {
		const response = await apiClient.get<ApiResponse<RoutingRun>>(`${ROUTING_API_BASE}/runs/${id}`);
		return response.data.data;
	},

	async getLatestRun(depotId: number): Promise<RoutingRun | null> {
		const response = await apiClient.get<ApiResponse<RoutingRun>>(`${ROUTING_API_BASE}/latest/${depotId}`);
		return response.data.data;
	},

	async getHistoryByDepot(depotId: number, page = 0, size = 20): Promise<RoutingHistoryPage> {
		const response = await apiClient.get<ApiResponse<RoutingHistoryPage>>(`${ROUTING_API_BASE}/history/${depotId}?page=${page}&size=${size}`);
		return response.data.data;
	},
};
