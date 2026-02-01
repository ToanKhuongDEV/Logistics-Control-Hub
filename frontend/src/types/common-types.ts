export interface PaginationInfo {
	currentPage: number;
	pageSize: number;
	totalElements: number;
	totalPages: number;
}

export interface PaginatedResponse<T> {
	data: T[];
	pagination: PaginationInfo;
}
