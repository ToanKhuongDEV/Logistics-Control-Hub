import apiClient from "./api";
import { Company, CompanyRequest, ApiResponse } from "@/types/company-types";

const COMPANY_API_BASE = "/api/v1/company";

export const companyApi = {
	async getCompanyInfo(): Promise<Company | null> {
		const response = await apiClient.get<ApiResponse<Company>>(COMPANY_API_BASE);
		return response.data.data;
	},

	async updateCompanyInfo(data: CompanyRequest): Promise<Company> {
		const response = await apiClient.post<ApiResponse<Company>>(COMPANY_API_BASE, data);
		return response.data.data;
	},
};
