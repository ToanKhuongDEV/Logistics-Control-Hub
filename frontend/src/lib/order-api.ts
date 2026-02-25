import apiClient from "./api";
import { Order, OrderRequest, OrderStatistics, PaginatedOrderResponse, ApiResponse, OrderFilterParams } from "@/types/order-types";

const ORDER_API_BASE = "/api/v1/orders";

export const orderApi = {
	async getOrders(params?: OrderFilterParams): Promise<PaginatedOrderResponse> {
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
		if (params?.depotId !== undefined && params?.depotId !== null) {
			queryParams.append("depotId", params.depotId.toString());
		}

		const url = `${ORDER_API_BASE}${queryParams.toString() ? `?${queryParams.toString()}` : ""}`;
		const response = await apiClient.get<ApiResponse<PaginatedOrderResponse>>(url);
		return response.data.data;
	},

	async getOrderById(id: number): Promise<Order> {
		const response = await apiClient.get<ApiResponse<Order>>(`${ORDER_API_BASE}/${id}`);
		return response.data.data;
	},

	async createOrder(data: OrderRequest): Promise<Order> {
		const response = await apiClient.post<ApiResponse<Order>>(ORDER_API_BASE, data);
		return response.data.data;
	},

	async updateOrder(id: number, data: OrderRequest): Promise<Order> {
		const response = await apiClient.put<ApiResponse<Order>>(`${ORDER_API_BASE}/${id}`, data);
		return response.data.data;
	},

	async deleteOrder(id: number): Promise<void> {
		await apiClient.delete<ApiResponse<null>>(`${ORDER_API_BASE}/${id}`);
	},

	async getStatistics(): Promise<OrderStatistics> {
		const response = await apiClient.get<ApiResponse<OrderStatistics>>(`${ORDER_API_BASE}/statistics`);
		return response.data.data;
	},
};
