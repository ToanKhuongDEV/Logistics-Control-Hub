export interface Depot {
	id: number;
	name: string;
	locationId: number;
	street: string;
	city: string;
	country: string;
	description: string;
	isActive: boolean;
	createdAt: string;
}

export interface LocationRequest {
	street: string;
	city: string;
	country: string;
}

export interface DepotRequest {
	name: string;
	locationRequest: LocationRequest;
	description?: string;
	isActive?: boolean;
}

export interface DepotStatistics {
	total: number;
	active: number;
	inactive: number;
}

export interface PaginatedResponse<T> {
	data: T[];
	pagination: {
		page: number;
		size: number;
		totalElements: number;
		totalPages: number;
	};
}
