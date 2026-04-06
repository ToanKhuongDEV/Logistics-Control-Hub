"use client";

import { useEffect, useMemo, useState } from "react";
import { ShieldAlert, Search, RefreshCw, ChevronLeft, ChevronRight } from "lucide-react";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useAuth } from "@/contexts/auth-context";
import { hasPermission } from "@/lib/auth";
import { auditApi, AuditLog } from "@/lib/audit-api";
import { depotApi } from "@/lib/depot-api";
import { Depot } from "@/types/depot-types";
import { toast } from "sonner";

const ACTION_OPTIONS = ["LOGIN", "LOGOUT", "CHANGE_PASSWORD", "CREATE", "UPDATE"] as const;
const RESOURCE_OPTIONS = ["AUTH", "USER"] as const;
const STATUS_OPTIONS = ["SUCCESS", "FAILED"] as const;

type Filters = {
	search: string;
	action: string;
	resourceType: string;
	actorUsername: string;
	scopeDepotId: string;
	status: string;
};

const INITIAL_FILTERS: Filters = {
	search: "",
	action: "ALL",
	resourceType: "ALL",
	actorUsername: "",
	scopeDepotId: "ALL",
	status: "ALL",
};

export default function AuditPage() {
	const { user } = useAuth();
	const canReadAudit = hasPermission(user, "audit.read");
	const [depots, setDepots] = useState<Depot[]>([]);
	const [logs, setLogs] = useState<AuditLog[]>([]);
	const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);
	const [filters, setFilters] = useState(INITIAL_FILTERS);
	const [page, setPage] = useState(0);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [isLoading, setIsLoading] = useState(true);

	const canGoPrev = page > 0;
	const canGoNext = page + 1 < totalPages;

	const fetchAuditLogs = async (nextPage = page, nextFilters = filters) => {
		if (!canReadAudit) {
			return;
		}

		setIsLoading(true);
		try {
			const response = await auditApi.getAuditLogs({
				page: nextPage,
				size: 20,
				search: nextFilters.search || undefined,
				action: nextFilters.action !== "ALL" ? nextFilters.action : undefined,
				resourceType: nextFilters.resourceType !== "ALL" ? nextFilters.resourceType : undefined,
				actorUsername: nextFilters.actorUsername || undefined,
				scopeDepotId: nextFilters.scopeDepotId !== "ALL" ? Number(nextFilters.scopeDepotId) : undefined,
				status: nextFilters.status !== "ALL" ? nextFilters.status : undefined,
			});

			setLogs(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
			setSelectedLog((current) => response.data.find((item) => item.id === current?.id) || response.data[0] || null);
		} catch (error: any) {
			console.error("Error fetching audit logs:", error);
			toast.error(error?.response?.data?.message || "Không thể tải nhật ký audit");
		} finally {
			setIsLoading(false);
		}
	};

	useEffect(() => {
		if (!canReadAudit) {
			return;
		}

		const bootstrap = async () => {
			try {
				const depotResult = await depotApi.getDepots({ page: 0, size: 100 });
				setDepots(depotResult.data);
			} catch (error) {
				console.error("Error fetching depots for audit filters:", error);
			}

			await fetchAuditLogs(0, INITIAL_FILTERS);
		};

		void bootstrap();
	}, [canReadAudit]);

	const resultSummary = useMemo(() => {
		if (isLoading) {
			return "Đang tải dữ liệu audit...";
		}
		if (totalElements === 0) {
			return "Chưa có bản ghi phù hợp với bộ lọc hiện tại.";
		}
		return `Hiển thị ${logs.length} / ${totalElements} bản ghi`;
	}, [isLoading, logs.length, totalElements]);

	const applyFilters = async () => {
		setPage(0);
		await fetchAuditLogs(0, filters);
	};

	const resetFilters = async () => {
		setFilters(INITIAL_FILTERS);
		setPage(0);
		await fetchAuditLogs(0, INITIAL_FILTERS);
	};

	const handlePageChange = async (nextPage: number) => {
		setPage(nextPage);
		await fetchAuditLogs(nextPage, filters);
	};

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex h-full flex-col">
					<div className="border-b border-border bg-card">
						<div className="px-8 py-6">
							<h1 className="text-3xl font-bold text-foreground">Audit Logs</h1>
							<p className="mt-2 text-muted-foreground">Theo dõi thao tác của người dùng, tra cứu thay đổi trước và sau trên các hành động nhạy cảm.</p>
						</div>
					</div>

					<div className="space-y-6 p-8">
						{canReadAudit ? (
							<>
								<Card className="p-6">
									<div className="grid gap-4 lg:grid-cols-3 xl:grid-cols-6">
										<div className="xl:col-span-2">
											<Label htmlFor="audit-search">Tìm nhanh</Label>
											<Input
												id="audit-search"
												className="mt-2"
												placeholder="Username, resource, message, request id..."
												value={filters.search}
												onChange={(event) => setFilters((current) => ({ ...current, search: event.target.value }))}
											/>
										</div>
										<div>
											<Label>Action</Label>
											<Select value={filters.action} onValueChange={(value) => setFilters((current) => ({ ...current, action: value }))}>
												<SelectTrigger className="mt-2 w-full">
													<SelectValue placeholder="Tất cả action" />
												</SelectTrigger>
												<SelectContent>
													<SelectItem value="ALL">Tất cả action</SelectItem>
													{ACTION_OPTIONS.map((action) => (
														<SelectItem key={action} value={action}>
															{action}
														</SelectItem>
													))}
												</SelectContent>
											</Select>
										</div>
										<div>
											<Label>Resource</Label>
											<Select value={filters.resourceType} onValueChange={(value) => setFilters((current) => ({ ...current, resourceType: value }))}>
												<SelectTrigger className="mt-2 w-full">
													<SelectValue placeholder="Tất cả resource" />
												</SelectTrigger>
												<SelectContent>
													<SelectItem value="ALL">Tất cả resource</SelectItem>
													{RESOURCE_OPTIONS.map((resource) => (
														<SelectItem key={resource} value={resource}>
															{resource}
														</SelectItem>
													))}
												</SelectContent>
											</Select>
										</div>
										<div>
											<Label>Status</Label>
											<Select value={filters.status} onValueChange={(value) => setFilters((current) => ({ ...current, status: value }))}>
												<SelectTrigger className="mt-2 w-full">
													<SelectValue placeholder="Tất cả trạng thái" />
												</SelectTrigger>
												<SelectContent>
													<SelectItem value="ALL">Tất cả trạng thái</SelectItem>
													{STATUS_OPTIONS.map((status) => (
														<SelectItem key={status} value={status}>
															{status}
														</SelectItem>
													))}
												</SelectContent>
											</Select>
										</div>
										<div>
											<Label htmlFor="audit-actor">Người thao tác</Label>
											<Input
												id="audit-actor"
												className="mt-2"
												placeholder="Nhập username"
												value={filters.actorUsername}
												onChange={(event) => setFilters((current) => ({ ...current, actorUsername: event.target.value }))}
											/>
										</div>
										<div>
											<Label>Kho phụ trách</Label>
											<Select value={filters.scopeDepotId} onValueChange={(value) => setFilters((current) => ({ ...current, scopeDepotId: value }))}>
												<SelectTrigger className="mt-2 w-full">
													<SelectValue placeholder="Tất cả kho" />
												</SelectTrigger>
												<SelectContent>
													<SelectItem value="ALL">Tất cả kho</SelectItem>
													{depots.map((depot) => (
														<SelectItem key={depot.id} value={String(depot.id)}>
															{depot.name}
														</SelectItem>
													))}
												</SelectContent>
											</Select>
										</div>
									</div>

									<div className="mt-5 flex flex-wrap gap-3">
										<Button onClick={() => void applyFilters()} className="gap-2">
											<Search className="h-4 w-4" />
											Áp dụng bộ lọc
										</Button>
										<Button variant="outline" onClick={() => void resetFilters()} className="gap-2">
											<RefreshCw className="h-4 w-4" />
											Đặt lại
										</Button>
										<p className="self-center text-sm text-muted-foreground">{resultSummary}</p>
									</div>
								</Card>

								<div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
									<Card className="overflow-hidden">
										<div className="border-b border-border px-6 py-4">
											<h2 className="text-lg font-semibold text-foreground">Danh sách bản ghi</h2>
										</div>
										<div className="overflow-x-auto">
											<table className="min-w-full divide-y divide-border text-sm">
												<thead className="bg-muted/40">
													<tr>
														<th className="px-4 py-3 text-left font-medium text-muted-foreground">Thời gian</th>
														<th className="px-4 py-3 text-left font-medium text-muted-foreground">Người dùng</th>
														<th className="px-4 py-3 text-left font-medium text-muted-foreground">Action</th>
														<th className="px-4 py-3 text-left font-medium text-muted-foreground">Resource</th>
														<th className="px-4 py-3 text-left font-medium text-muted-foreground">Trạng thái</th>
													</tr>
												</thead>
												<tbody className="divide-y divide-border">
													{logs.length > 0 ? (
														logs.map((log) => (
															<tr key={log.id} className={`cursor-pointer transition-colors hover:bg-muted/50 ${selectedLog?.id === log.id ? "bg-primary/5" : ""}`} onClick={() => setSelectedLog(log)}>
																<td className="px-4 py-3 text-foreground">{new Date(log.createdAt).toLocaleString("vi-VN")}</td>
																<td className="px-4 py-3 text-foreground">{log.actorUsername || "Ẩn danh"}</td>
																<td className="px-4 py-3">
																	<span className="rounded-full bg-muted px-2.5 py-1 text-xs font-medium text-foreground">{log.action}</span>
																</td>
																<td className="px-4 py-3 text-foreground">
																	<div>{log.resourceType}</div>
																	<div className="text-xs text-muted-foreground">{log.resourceName || log.resourceId || "Không có mã"}</div>
																</td>
																<td className="px-4 py-3">
																	<span className={`rounded-full px-2.5 py-1 text-xs font-medium ${log.status === "SUCCESS" ? "bg-emerald-100 text-emerald-700" : "bg-rose-100 text-rose-700"}`}>{log.status}</span>
																</td>
															</tr>
														))
													) : (
														<tr>
															<td colSpan={5} className="px-4 py-10 text-center text-muted-foreground">
																{isLoading ? "Đang tải dữ liệu..." : "Không có bản ghi audit phù hợp."}
															</td>
														</tr>
													)}
												</tbody>
											</table>
										</div>
										<div className="flex items-center justify-between border-t border-border px-6 py-4">
											<p className="text-sm text-muted-foreground">
												Trang {Math.min(page + 1, Math.max(totalPages, 1))} / {Math.max(totalPages, 1)}
											</p>
											<div className="flex gap-2">
												<Button variant="outline" size="sm" onClick={() => void handlePageChange(page - 1)} disabled={!canGoPrev || isLoading} className="gap-2">
													<ChevronLeft className="h-4 w-4" />
													Trước
												</Button>
												<Button variant="outline" size="sm" onClick={() => void handlePageChange(page + 1)} disabled={!canGoNext || isLoading} className="gap-2">
													Sau
													<ChevronRight className="h-4 w-4" />
												</Button>
											</div>
										</div>
									</Card>

									<Card className="p-6">
										<div className="mb-4">
											<h2 className="text-lg font-semibold text-foreground">Chi tiết bản ghi</h2>
											<p className="mt-1 text-sm text-muted-foreground">Xem thông tin request và dữ liệu thay đổi trước/sau của thao tác đã chọn.</p>
										</div>

										{selectedLog ? (
											<div className="space-y-4">
												<div className="grid gap-3 md:grid-cols-2">
													<AuditMeta label="Người thao tác" value={selectedLog.actorUsername || "Ẩn danh"} />
													<AuditMeta label="Vai trò" value={selectedLog.actorRole || "N/A"} />
													<AuditMeta label="Action" value={selectedLog.action} />
													<AuditMeta label="Resource" value={selectedLog.resourceType} />
													<AuditMeta label="Mã đối tượng" value={selectedLog.resourceId || "N/A"} />
													<AuditMeta label="Kho" value={selectedLog.scopeDepotId ? `Kho #${selectedLog.scopeDepotId}` : "Global"} />
													<AuditMeta label="Request ID" value={selectedLog.requestId || "N/A"} />
													<AuditMeta label="IP" value={selectedLog.ipAddress || "N/A"} />
												</div>
												<div>
													<Label>Thông điệp</Label>
													<p className="mt-2 rounded-lg border border-border bg-muted/30 px-3 py-2 text-sm text-foreground">{selectedLog.message || "Không có thông điệp bổ sung."}</p>
												</div>
												<AuditJsonBlock title="Before Data" value={selectedLog.beforeData} />
												<AuditJsonBlock title="After Data" value={selectedLog.afterData} />
												<AuditJsonBlock title="Metadata" value={selectedLog.metadata} />
											</div>
										) : (
											<div className="rounded-xl border border-dashed border-border px-6 py-10 text-center text-muted-foreground">
												Chọn một bản ghi ở danh sách bên trái để xem chi tiết.
											</div>
										)}
									</Card>
								</div>
							</>
						) : (
							<Card className="max-w-2xl p-10 text-center">
								<div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
									<ShieldAlert className="h-6 w-6" />
								</div>
								<h2 className="text-xl font-semibold text-foreground">Chỉ admin mới được xem audit logs</h2>
								<p className="mt-2 text-muted-foreground">Trang này dùng để điều tra thao tác hệ thống và theo dõi thay đổi nhạy cảm, nên đang được giới hạn ở quyền `audit.read`.</p>
							</Card>
						)}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}

function AuditMeta({ label, value }: { label: string; value: string }) {
	return (
		<div className="rounded-lg border border-border bg-muted/20 px-3 py-2">
			<p className="text-xs uppercase tracking-wide text-muted-foreground">{label}</p>
			<p className="mt-1 text-sm font-medium text-foreground">{value}</p>
		</div>
	);
}

function AuditJsonBlock({ title, value }: { title: string; value: unknown }) {
	return (
		<div>
			<Label>{title}</Label>
			<pre className="mt-2 max-h-64 overflow-auto rounded-lg border border-border bg-slate-950/95 p-4 text-xs text-slate-100">
				{value ? JSON.stringify(value, null, 2) : "Không có dữ liệu"}
			</pre>
		</div>
	);
}
