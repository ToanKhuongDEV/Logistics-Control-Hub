export interface Driver {
	id: number;
	name: string;
	licenseNumber: string;
	phoneNumber: string;
	email?: string;
	createdAt?: string;
	updatedAt?: string;
}

export interface DriverRequest {
	name: string;
	licenseNumber: string;
	phoneNumber: string;
	email?: string;
}

export interface DriverStatistics {
	total: number;
	available: number;
	assigned: number;
}
