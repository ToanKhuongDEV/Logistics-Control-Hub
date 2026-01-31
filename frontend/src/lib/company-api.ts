import { Company, CompanyRequest, ApiResponse } from "@/types/company-types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export const companyApi = {
	async getCompanyInfo(): Promise<Company | null> {
		const response = await fetch(`${API_BASE_URL}/api/v1/company`);
		if (!response.ok) {
			throw new Error("Failed to fetch company info");
		}
		const result: ApiResponse<Company> = await response.json();
		return result.data;
	},

	async updateCompanyInfo(data: CompanyRequest): Promise<Company> {
		const response = await fetch(`${API_BASE_URL}/api/v1/company`, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(data),
		});

		if (!response.ok) {
			throw new Error("Failed to update company info");
		}

		const result: ApiResponse<Company> = await response.json();
		return result.data;
	},
};
