export enum OrderStatus {
	CREATED = "CREATED",
	IN_TRANSIT = "IN_TRANSIT",
	DELIVERED = "DELIVERED",
	CANCELLED = "CANCELLED",
}

export interface Location {
	id: number;
	street: string;
	city: string;
	country: string;
	latitude: number;
	longitude: number;
}

export interface Order {
	id: number;
	code: string;
	deliveryLocationName: string;
	deliveryStreet?: string;
	deliveryCity?: string;
	deliveryCountry?: string;
	weightKg: number;
	volumeM3: number;
	driverId?: number;
	driverName?: string;
	status: OrderStatus;
	createdAt: string;
}

export interface LocationRequest {
	street: string;
	city: string;
	country: string;
}

export interface OrderRequest {
	code: string;
	deliveryLocation: LocationRequest;
	weightKg?: number;
	volumeM3?: number;
	status?: OrderStatus;
}

export interface OrderStatistics {
	total: number;
	pending: number;
	inTransit: number;
}

export interface PaginationInfo {
	currentPage: number;
	pageSize: number;
	totalElements: number;
	totalPages: number;
}

export interface PaginatedOrderResponse {
	data: Order[];
	pagination: PaginationInfo;
}

export interface ApiResponse<T> {
	code: number;
	message: string;
	data: T;
}

export interface OrderFilterParams {
	page?: number;
	size?: number;
	status?: OrderStatus | "all";
	search?: string;
}
