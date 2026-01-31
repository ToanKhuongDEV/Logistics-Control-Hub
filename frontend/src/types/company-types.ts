export interface Company {
	id: number;
	name: string;
	address: string;
	phone: string;
	email?: string;
	website?: string;
	taxId?: string;
	description?: string;
	createdAt: string;
	updatedAt?: string;
}

export interface CompanyRequest {
	name: string;
	address: string;
	phone: string;
	email?: string;
	website?: string;
	taxId?: string;
	description?: string;
}

export interface ApiResponse<T> {
	code: number;
	message: string;
	data: T;
}
