import apiClient from "./api";
import { PaginatedResponse } from "@/types/common-types";
import { OrderStatus } from "@/types/order-types";
import { RoutingHistoryPage, RoutingRun } from "@/lib/routing-api";

const DRIVER_PORTAL_API_BASE = "/api/v1/driver";

interface ApiResponse<T> {
	code: number;
	message: string;
	data: T;
}

export interface DriverDeliveryOrder {
	id: number;
	code: string;
	deliveryLocationName: string;
	deliveryStreet?: string | null;
	deliveryCity?: string | null;
	deliveryCountry?: string | null;
	weightKg?: number | null;
	volumeM3?: number | null;
	depotId?: number | null;
	depotName?: string | null;
	latitude?: number | null;
	longitude?: number | null;
	status: OrderStatus;
	createdAt: string;
	routeId?: number | null;
	stopId?: number | null;
	stopSequence?: number | null;
}

export interface DriverOrdersQuery {
	page?: number;
	size?: number;
}

export const driverPortalApi = {
	async getMyOrders(params: DriverOrdersQuery = {}): Promise<PaginatedResponse<DriverDeliveryOrder>> {
		const page = params.page ?? 0;
		const size = params.size ?? 20;
		const response = await apiClient.get<ApiResponse<PaginatedResponse<DriverDeliveryOrder>>>(`${DRIVER_PORTAL_API_BASE}/me/orders?page=${page}&size=${size}`);
		return response.data.data;
	},

	async getMyOrder(orderId: number): Promise<DriverDeliveryOrder> {
		const response = await apiClient.get<ApiResponse<DriverDeliveryOrder>>(`${DRIVER_PORTAL_API_BASE}/me/orders/${orderId}`);
		return response.data.data;
	},

	async completeMyOrder(orderId: number): Promise<DriverDeliveryOrder> {
		const response = await apiClient.patch<ApiResponse<DriverDeliveryOrder>>(`${DRIVER_PORTAL_API_BASE}/me/orders/${orderId}/complete`);
		return response.data.data;
	},

	async getMyRoutingHistory(page = 0, size = 20): Promise<RoutingHistoryPage> {
		const response = await apiClient.get<ApiResponse<RoutingHistoryPage>>(`${DRIVER_PORTAL_API_BASE}/me/routing/history?page=${page}&size=${size}`);
		return response.data.data;
	},

	async getMyRoutingRun(runId: number): Promise<RoutingRun> {
		const response = await apiClient.get<ApiResponse<RoutingRun>>(`${DRIVER_PORTAL_API_BASE}/me/routing/runs/${runId}`);
		return response.data.data;
	},

	async getMyLatestRoutingRun(): Promise<RoutingRun | null> {
		const response = await apiClient.get<ApiResponse<RoutingRun | null>>(`${DRIVER_PORTAL_API_BASE}/me/routing/latest`);
		return response.data.data;
	},
};
