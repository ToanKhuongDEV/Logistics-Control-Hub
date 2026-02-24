export enum VehicleStatus {
	ACTIVE = "ACTIVE",
	MAINTENANCE = "MAINTENANCE",
	IDLE = "IDLE",
}

export interface Vehicle {
	id: number;
	code: string;
	maxWeightKg: number;
	maxVolumeM3: number;
	costPerKm: number;
	status: VehicleStatus;
	type?: string;
	driverId?: number;
	driverName?: string;
	depotId?: number;
	depotName?: string;
	locationId?: number;
	street?: string;
	city?: string;
	country?: string;
	createdAt?: string;
}

export interface VehicleRequest {
	code: string;
	maxWeightKg: number;
	maxVolumeM3: number;
	costPerKm: number;
	status: VehicleStatus;
	type?: string;
	driverId?: number | null;
	depotId: number;
}

export interface VehicleStatistics {
	total: number;
	active: number;
	maintenance: number;
	idle: number;
	averageCostPerKm: number;
	totalCapacityKg: number;
	totalCapacityM3: number;
}

export interface PaginationInfo {
	currentPage: number;
	pageSize: number;
	totalElements: number;
	totalPages: number;
}

export interface PaginatedVehicleResponse {
	data: Vehicle[];
	pagination: PaginationInfo;
}

export interface ApiResponse<T> {
	code: number;
	message: string;
	data: T;
}

export interface VehicleFilterParams {
	page?: number;
	size?: number;
	status?: VehicleStatus | "all";
	search?: string;
}
