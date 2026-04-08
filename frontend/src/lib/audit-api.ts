import apiClient from "./api";
import { PaginatedResponse } from "@/types/depot-types";

export interface AuditLog {
	id: number;
	actorUserId: number | null;
	actorUsername: string | null;
	actorRole: string | null;
	action: string;
	resourceType: string;
	resourceId: string | null;
	resourceName: string | null;
	scopeDepotId: number | null;
	status: string;
	message: string | null;
	beforeData: unknown;
	afterData: unknown;
	metadata: unknown;
	ipAddress: string | null;
	userAgent: string | null;
	requestId: string | null;
	createdAt: string;
}

export interface GetAuditLogsParams {
	page?: number;
	size?: number;
	action?: string;
	resourceType?: string;
	actorUsername?: string;
	scopeDepotId?: number;
	status?: string;
	search?: string;
	from?: string;
	to?: string;
}

export const auditApi = {
	async getAuditLogs(params: GetAuditLogsParams = {}): Promise<PaginatedResponse<AuditLog>> {
		const searchParams = new URLSearchParams();
		searchParams.set("page", String(params.page ?? 0));
		searchParams.set("size", String(params.size ?? 20));

		if (params.action) searchParams.set("action", params.action);
		if (params.resourceType) searchParams.set("resourceType", params.resourceType);
		if (params.actorUsername) searchParams.set("actorUsername", params.actorUsername);
		if (typeof params.scopeDepotId === "number") searchParams.set("scopeDepotId", String(params.scopeDepotId));
		if (params.status) searchParams.set("status", params.status);
		if (params.search) searchParams.set("search", params.search);
		if (params.from) searchParams.set("from", params.from);
		if (params.to) searchParams.set("to", params.to);

		const response = await apiClient.get<{ data: PaginatedResponse<AuditLog> }>(`/api/v1/audit-logs?${searchParams.toString()}`);
		return response.data.data;
	},
};
