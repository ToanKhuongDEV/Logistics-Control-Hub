import apiClient from "./api";
import { DashboardStatistics } from "@/types/dashboard-types";

const DASHBOARD_API_BASE = "/api/v1/dashboard";

export async function getStatistics(depotId?: number | null): Promise<DashboardStatistics> {
	const response = await apiClient.get<{ data: DashboardStatistics }>(`${DASHBOARD_API_BASE}/statistics`, {
		params: { depotId },
	});
	return response.data.data;
}

export const dashboardApi = {
	getStatistics,
};
