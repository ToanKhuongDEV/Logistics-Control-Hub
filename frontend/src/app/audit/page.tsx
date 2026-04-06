"use client";

import { useEffect, useMemo, useState } from "react";
import {
	AlertTriangle,
	CalendarRange,
	CheckCircle2,
	ChevronLeft,
	ChevronRight,
	Clock3,
	Copy,
	Eye,
	Siren,
	RefreshCw,
	Route,
	Search,
	ShieldAlert,
	ShieldCheck,
	UserSearch,
	Waypoints,
	X,
} from "lucide-react";
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
import { cn } from "@/lib/utils";
import { toast } from "sonner";

const ACTION_OPTIONS = ["LOGIN", "LOGOUT", "CHANGE_PASSWORD", "CREATE", "UPDATE", "DELETE", "BULK_UPDATE", "EXECUTE"] as const;
const RESOURCE_OPTIONS = ["AUTH", "USER", "COMPANY", "DEPOT", "DRIVER", "VEHICLE", "ORDER", "ROUTING_RUN"] as const;
const STATUS_OPTIONS = ["SUCCESS", "FAILED"] as const;

type Filters = {
	search: string;
	action: string;
	resourceType: string;
	actorUsername: string;
	scopeDepotId: string;
	status: string;
	from: string;
	to: string;
};

const INITIAL_FILTERS: Filters = {
	search: "",
	action: "ALL",
	resourceType: "ALL",
	actorUsername: "",
	scopeDepotId: "ALL",
	status: "ALL",
	from: "",
	to: "",
};

export default function AuditPage() {
	const { user } = useAuth();
	const canReadAudit = hasPermission(user, "audit.read");
	const [depots, setDepots] = useState<Depot[]>([]);
	const [logs, setLogs] = useState<AuditLog[]>([]);
	const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);
	const [isJsonViewerOpen, setIsJsonViewerOpen] = useState(false);
	const [filters, setFilters] = useState<Filters>(INITIAL_FILTERS);
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
				from: toIsoDateTime(nextFilters.from),
				to: toIsoDateTime(nextFilters.to),
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
		return `Đang hiển thị ${logs.length} / ${totalElements} bản ghi`;
	}, [isLoading, logs.length, totalElements]);

	const activeFilterCount = useMemo(() => countActiveFilters(filters), [filters]);
	const currentPageSuccessCount = useMemo(() => logs.filter((log) => log.status === "SUCCESS").length, [logs]);
	const currentPageFailedCount = useMemo(() => logs.filter((log) => log.status === "FAILED").length, [logs]);
	const currentPageRoutingCount = useMemo(() => logs.filter((log) => log.resourceType === "ROUTING_RUN").length, [logs]);
	const currentPageIncidentCount = useMemo(() => logs.filter((log) => isPriorityLog(log)).length, [logs]);
	const highlightedChanges = useMemo(() => buildChangeSummary(selectedLog), [selectedLog]);

	const applyFilters = async () => {
		setPage(0);
		await fetchAuditLogs(0, filters);
	};

	const applyDatePreset = async (days: number) => {
		const now = new Date();
		const from = new Date(now.getTime() - days * 24 * 60 * 60 * 1000);
		const nextFilters = {
			...filters,
			from: toDateTimeLocalValue(from),
			to: toDateTimeLocalValue(now),
		};

		setFilters(nextFilters);
		setPage(0);
		await fetchAuditLogs(0, nextFilters);
	};

	const resetFilters = async () => {
		setFilters(INITIAL_FILTERS);
		setPage(0);
		await fetchAuditLogs(0, INITIAL_FILTERS);
	};

	const applyQuickFocus = async (focus: "failed" | "routing" | "incidents") => {
		const nextFilters =
			focus === "failed"
				? { ...filters, status: "FAILED", resourceType: "ALL" }
				: focus === "routing"
					? { ...filters, resourceType: "ROUTING_RUN", status: "ALL" }
					: { ...filters, status: "FAILED", resourceType: "ROUTING_RUN" };

		setFilters(nextFilters);
		setPage(0);
		await fetchAuditLogs(0, nextFilters);
	};

	const handlePageChange = async (nextPage: number) => {
		setPage(nextPage);
		await fetchAuditLogs(nextPage, filters);
	};

	const copyValue = async (value: string | null | undefined, label: string) => {
		if (!value) {
			toast.error(`Không có ${label.toLowerCase()} để sao chép`);
			return;
		}

		try {
			await navigator.clipboard.writeText(value);
			toast.success(`Đã sao chép ${label.toLowerCase()}`);
		} catch (error) {
			console.error("Copy failed:", error);
			toast.error(`Không thể sao chép ${label.toLowerCase()}`);
		}
	};

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex h-full flex-col bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.10),_transparent_28%),radial-gradient(circle_at_top_right,_rgba(59,130,246,0.08),_transparent_24%)]">
					<div className="border-b border-border bg-card/90 backdrop-blur">
						<div className="px-6 py-6 md:px-8">
							<div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
								<div className="space-y-3">
									<div className="inline-flex items-center gap-2 rounded-full border border-emerald-500/20 bg-emerald-500/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.24em] text-emerald-700">
										<ShieldCheck className="h-3.5 w-3.5" />
										Bảng điều khiển audit
									</div>
									<div>
										<h1 className="text-3xl font-bold text-foreground md:text-4xl">Nhật ký audit</h1>
										<p className="mt-2 max-w-3xl text-sm leading-6 text-muted-foreground md:text-base">
											Theo dõi thao tác nhạy cảm, đối chiếu dữ liệu trước và sau, và truy vết request giúp admin điều tra nhanh hơn.
										</p>
									</div>
								</div>

								<div className="grid gap-3 sm:grid-cols-2 xl:w-[460px]">
									<SummaryCard
										icon={Waypoints}
										label="Tổng kết quả"
										value={String(totalElements)}
										helper={resultSummary}
										tone="neutral"
									/>
									<SummaryCard
										icon={AlertTriangle}
										label="Bản ghi thất bại"
										value={String(currentPageFailedCount)}
										helper="Tính trên trang hiện tại"
										tone="danger"
									/>
								</div>
							</div>
						</div>
					</div>

					<div className="space-y-6 p-6 md:p-8">
						{canReadAudit ? (
							<>
								<div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
									<SummaryCard
										icon={Clock3}
										label="Trang hiện tại"
										value={String(logs.length)}
										helper="Số dòng đang hiển thị"
										tone="neutral"
									/>
									<SummaryCard
										icon={CheckCircle2}
										label="Thành công"
										value={String(currentPageSuccessCount)}
										helper="Tác vụ thành công"
										tone="success"
									/>
									<SummaryCard
										icon={AlertTriangle}
										label="Thất bại"
										value={String(currentPageFailedCount)}
										helper="Cần ưu tiên kiểm tra"
										tone="danger"
									/>
									<SummaryCard
										icon={Route}
										label="Nhật ký routing"
										value={String(currentPageRoutingCount)}
										helper="Sự kiện tối ưu tuyến"
										tone="routing"
									/>
								</div>

								<div className="grid gap-4 xl:grid-cols-[1.1fr_0.9fr_0.9fr]">
									<PrioritySpotlightCard
										icon={Siren}
										title="Cảnh báo ưu tiên"
										value={`${currentPageIncidentCount} bản ghi`}
										description="Gom các sự kiện thất bại và routing để admin quét trước."
										tone="danger"
										onClick={() => void applyQuickFocus("incidents")}
										buttonLabel="Tập trung sự cố"
									/>
									<PrioritySpotlightCard
										icon={AlertTriangle}
										title="Thất bại"
										value={`${currentPageFailedCount} bản ghi`}
										description="Validation fail, permission fail, routing fail và các nghiệp vụ cần điều tra."
										tone="danger"
										onClick={() => void applyQuickFocus("failed")}
										buttonLabel="Chỉ xem fail"
									/>
									<PrioritySpotlightCard
										icon={Route}
										title="Tối ưu tuyến"
										value={`${currentPageRoutingCount} bản ghi`}
										description="Nhóm log tối ưu tuyến, để theo dõi run thành công và các ca fail."
										tone="routing"
										onClick={() => void applyQuickFocus("routing")}
										buttonLabel="Chỉ xem routing"
									/>
								</div>

								<Card className="overflow-hidden border-border/70 bg-card/95 shadow-sm">
									<div className="border-b border-border bg-muted/20 px-6 py-4">
										<div className="flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
											<div>
												<h2 className="text-lg font-semibold text-foreground">Bộ lọc điều tra</h2>
												<p className="text-sm text-muted-foreground">Lọc theo người thao tác, loại sự kiện, depot phụ trách và khoảng thời gian.</p>
											</div>
											<div className="inline-flex items-center gap-2 text-sm text-muted-foreground">
												<CalendarRange className="h-4 w-4" />
												{activeFilterCount > 0 ? `${activeFilterCount} bộ lọc đang áp dụng` : "Chưa áp dụng bộ lọc"}
											</div>
										</div>
									</div>

									<div className="space-y-5 p-6">
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
												<Label>Hành động</Label>
												<Select value={filters.action} onValueChange={(value) => setFilters((current) => ({ ...current, action: value }))}>
													<SelectTrigger className="mt-2 w-full">
														<SelectValue placeholder="Tất cả action" />
													</SelectTrigger>
													<SelectContent>
														<SelectItem value="ALL">Tất cả action</SelectItem>
														{ACTION_OPTIONS.map((action) => (
															<SelectItem key={action} value={action}>
																{formatActionLabel(action)}
															</SelectItem>
														))}
													</SelectContent>
												</Select>
											</div>
											<div>
												<Label>Tài nguyên</Label>
												<Select value={filters.resourceType} onValueChange={(value) => setFilters((current) => ({ ...current, resourceType: value }))}>
													<SelectTrigger className="mt-2 w-full">
														<SelectValue placeholder="Tất cả resource" />
													</SelectTrigger>
													<SelectContent>
														<SelectItem value="ALL">Tất cả resource</SelectItem>
														{RESOURCE_OPTIONS.map((resource) => (
															<SelectItem key={resource} value={resource}>
																{formatResourceLabel(resource)}
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
																{formatStatusLabel(status)}
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

										<div className="grid gap-4 md:grid-cols-2">
											<div>
												<Label htmlFor="audit-from">Từ thời điểm</Label>
												<Input
													id="audit-from"
													type="datetime-local"
													className="mt-2"
													value={filters.from}
													onChange={(event) => setFilters((current) => ({ ...current, from: event.target.value }))}
												/>
											</div>
											<div>
												<Label htmlFor="audit-to">Đến thời điểm</Label>
												<Input
													id="audit-to"
													type="datetime-local"
													className="mt-2"
													value={filters.to}
													onChange={(event) => setFilters((current) => ({ ...current, to: event.target.value }))}
												/>
											</div>
										</div>

										{activeFilterCount > 0 ? (
											<div className="flex flex-wrap gap-2">
												{buildActiveFilterChips(filters, depots).map((chip) => (
													<span key={chip} className="rounded-full border border-border bg-muted/40 px-3 py-1 text-xs font-medium text-foreground">
														{chip}
													</span>
												))}
											</div>
										) : null}

										<div className="flex flex-wrap gap-3">
											<Button onClick={() => void applyFilters()} className="gap-2">
												<Search className="h-4 w-4" />
												Áp dụng bộ lọc
											</Button>
											<Button variant="outline" onClick={() => void resetFilters()} className="gap-2">
												<RefreshCw className="h-4 w-4" />
												Đặt lại
											</Button>
											<Button variant="outline" onClick={() => void applyDatePreset(1)} className="gap-2">
												24h gần nhất
											</Button>
											<Button variant="outline" onClick={() => void applyDatePreset(7)} className="gap-2">
												7 ngày
											</Button>
											<Button variant="outline" onClick={() => void applyDatePreset(30)} className="gap-2">
												30 ngày
											</Button>
											<Button variant="outline" onClick={() => void applyQuickFocus("failed")} className="gap-2">
												Thất bại
											</Button>
											<Button variant="outline" onClick={() => void applyQuickFocus("routing")} className="gap-2">
												Tối ưu tuyến
											</Button>
											<p className="self-center text-sm text-muted-foreground">{resultSummary}</p>
										</div>
									</div>
								</Card>

								<div className="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
									<Card className="overflow-hidden border-border/70 bg-card/95 shadow-sm">
										<div className="border-b border-border px-6 py-4">
											<div className="flex items-center justify-between gap-4">
												<div>
													<h2 className="text-lg font-semibold text-foreground">Danh sách bản ghi</h2>
													<p className="text-sm text-muted-foreground">Chọn một dòng để mở panel điều tra chi tiết.</p>
												</div>
												<div className="rounded-full border border-border bg-muted/30 px-3 py-1 text-xs font-medium text-muted-foreground">
													Trang {Math.min(page + 1, Math.max(totalPages, 1))} / {Math.max(totalPages, 1)}
												</div>
											</div>
										</div>

										<div className="max-h-[860px] overflow-y-auto p-3">
											<div className="space-y-3">
												{logs.length > 0 ? (
													logs.map((log) => (
														<button
															key={log.id}
															type="button"
															onClick={() => setSelectedLog(log)}
															className={cn(
																"w-full rounded-2xl border p-4 text-left transition-all",
																selectedLog?.id === log.id
																	? "border-primary/50 bg-primary/5 shadow-sm"
																	: isPriorityLog(log)
																		? "border-rose-300/60 bg-rose-50/70 hover:border-rose-400 hover:bg-rose-50"
																		: log.resourceType === "ROUTING_RUN"
																			? "border-sky-300/60 bg-sky-50/60 hover:border-sky-400 hover:bg-sky-50"
																			: "border-border bg-background hover:border-primary/30 hover:bg-muted/20",
															)}
														>
															<div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
																<div className="space-y-3">
																	<div className="flex flex-wrap items-center gap-2">
																		<ActionBadge action={log.action} />
																		<StatusBadge status={log.status} />
																		<ResourceBadge resourceType={log.resourceType} />
																	</div>
																	<div>
																		<p className="text-sm font-semibold text-foreground">{log.resourceName || log.resourceId || "Không có mã đối tượng"}</p>
																		<p className="mt-1 text-sm text-muted-foreground">
																			{log.actorUsername || "Ẩn danh"} {log.actorRole ? `- ${log.actorRole}` : ""}
																		</p>
																	</div>
																</div>
																<div className="space-y-1 text-right text-xs text-muted-foreground">
																	<p>{formatDateTime(log.createdAt)}</p>
																	<p>{formatDepotScope(log.scopeDepotId)}</p>
																</div>
															</div>

															<p className="mt-3 line-clamp-2 text-sm leading-6 text-muted-foreground">
																{log.message || "Không có thông điệp bổ sung cho bản ghi này."}
															</p>

															<div className="mt-3 flex flex-wrap gap-2 text-xs text-muted-foreground">
																{isPriorityLog(log) ? <span className="rounded-full bg-rose-600 px-2.5 py-1 font-semibold text-white">Cảnh báo ưu tiên</span> : null}
																{log.resourceType === "ROUTING_RUN" ? <span className="rounded-full bg-sky-600 px-2.5 py-1 font-semibold text-white">Dấu vết routing</span> : null}
																<span className="rounded-full bg-muted/50 px-2.5 py-1">Mã request {log.requestId || "N/A"}</span>
																<span className="rounded-full bg-muted/50 px-2.5 py-1">IP {log.ipAddress || "N/A"}</span>
															</div>
														</button>
													))
												) : (
													<div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center text-sm text-muted-foreground">
														{isLoading ? "Đang tải dữ liệu..." : "Không có bản ghi audit phù hợp."}
													</div>
												)}
											</div>
										</div>

										<div className="flex items-center justify-between border-t border-border px-6 py-4">
											<p className="text-sm text-muted-foreground">{resultSummary}</p>
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

									<Card className="overflow-hidden border-border/70 bg-card/95 shadow-sm">
										<div className="border-b border-border bg-muted/20 px-6 py-4">
											<div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
												<div>
													<h2 className="text-lg font-semibold text-foreground">Chi tiết bản ghi</h2>
													<p className="text-sm text-muted-foreground">Đọc ngữ cảnh sự kiện, request và dữ liệu thay đổi trên cùng một màn hình.</p>
												</div>
												{selectedLog ? (
													<div className="flex gap-2">
														<Button variant="outline" size="sm" onClick={() => void copyValue(selectedLog.requestId, "mã request")}>
															<Copy className="h-4 w-4" />
															Mã request
														</Button>
														<Button variant="outline" size="sm" onClick={() => void copyValue(selectedLog.resourceId, "mã đối tượng")}>
															<Copy className="h-4 w-4" />
															Mã đối tượng
														</Button>
													</div>
												) : null}
											</div>
										</div>

										<div className="max-h-[860px] overflow-y-auto p-6">
											{selectedLog ? (
												<div className="space-y-6">
													<div className={cn(
														"rounded-2xl border p-5 text-slate-100 shadow-sm",
														selectedLog.status === "FAILED"
															? "border-rose-400/40 bg-[linear-gradient(135deg,rgba(127,29,29,0.98),rgba(68,12,12,0.95))]"
															: selectedLog.resourceType === "ROUTING_RUN"
																? "border-sky-400/40 bg-[linear-gradient(135deg,rgba(8,47,73,0.98),rgba(12,74,110,0.95))]"
																: "border-border bg-[linear-gradient(135deg,rgba(15,23,42,0.98),rgba(30,41,59,0.95))]",
													)}>
														<div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
															<div className="space-y-3">
																<div className="flex flex-wrap items-center gap-2">
																	<ActionBadge action={selectedLog.action} />
																	<StatusBadge status={selectedLog.status} />
																	<ResourceBadge resourceType={selectedLog.resourceType} />
																</div>
																<div>
																	<p className="text-xs uppercase tracking-[0.22em] text-slate-400">Đối tượng mục tiêu</p>
																	<h3 className="mt-2 text-2xl font-semibold text-white">{selectedLog.resourceName || selectedLog.resourceId || "Không rõ đối tượng"}</h3>
																	<p className="mt-2 text-sm text-slate-300">{selectedLog.message || "Không có thông điệp bổ sung."}</p>
																</div>
															</div>
															<div className="rounded-2xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-slate-200">
																<p className="text-xs uppercase tracking-[0.2em] text-slate-400">Thời điểm xảy ra</p>
																<p className="mt-2 font-medium text-white">{formatDateTime(selectedLog.createdAt)}</p>
															</div>
														</div>
													</div>

													<div className="grid gap-3 md:grid-cols-2">
														<AuditMeta label="Người thao tác" value={selectedLog.actorUsername || "Ẩn danh"} />
														<AuditMeta label="Vai trò" value={selectedLog.actorRole || "N/A"} />
														<AuditMeta label="Hành động" value={formatActionLabel(selectedLog.action)} />
														<AuditMeta label="Tài nguyên" value={formatResourceLabel(selectedLog.resourceType)} />
														<AuditMeta label="Mã đối tượng" value={selectedLog.resourceId || "N/A"} />
														<AuditMeta label="Kho phạm vi" value={formatDepotScope(selectedLog.scopeDepotId)} />
														<AuditMeta label="Mã request" value={selectedLog.requestId || "N/A"} />
														<AuditMeta label="Địa chỉ IP" value={selectedLog.ipAddress || "N/A"} />
														<AuditMeta label="Trình duyệt / thiết bị" value={selectedLog.userAgent || "N/A"} className="md:col-span-2" />
													</div>

													<div className="rounded-2xl border border-border bg-muted/20 p-5">
														<div className="flex items-center justify-between gap-3">
															<div>
																<h3 className="font-semibold text-foreground">Tóm tắt thay đổi</h3>
																<p className="text-sm text-muted-foreground">Rút gọn các key thay đổi để admin quét nhanh trước khi xem JSON.</p>
															</div>
															<span className="rounded-full border border-border bg-background px-3 py-1 text-xs font-medium text-muted-foreground">
																{highlightedChanges.length} mục
															</span>
														</div>

														{highlightedChanges.length > 0 ? (
															<div className="mt-4 flex flex-wrap gap-2">
																{highlightedChanges.map((item) => (
																	<span key={item} className="rounded-full bg-background px-3 py-1 text-xs font-medium text-foreground shadow-sm">
																		{item}
																	</span>
																))}
															</div>
														) : (
															<p className="mt-4 text-sm text-muted-foreground">Bản ghi này không có snapshot before/after hoặc không phát hiện trường thay đổi.</p>
														)}
													</div>


													<div className="grid gap-4 xl:grid-cols-3">
														<AuditJsonBlock title="Before Data" value={selectedLog.beforeData} onOpen={() => setIsJsonViewerOpen(true)} />
														<AuditJsonBlock title="After Data" value={selectedLog.afterData} onOpen={() => setIsJsonViewerOpen(true)} />
														<AuditJsonBlock title="Metadata" value={selectedLog.metadata} onOpen={() => setIsJsonViewerOpen(true)} />
													</div>
												</div>
											) : (
												<div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center">
													<p className="text-lg font-semibold text-foreground">Chọn một bản ghi audit</p>
													<p className="mt-2 text-sm text-muted-foreground">Panel này sẽ hiển thị request context, snapshot dữ liệu và tóm tắt thay đổi.</p>
												</div>
											)}
										</div>
									</Card>
								</div>
							</>
						) : (
							<Card className="max-w-2xl border-border/70 bg-card/95 p-10 text-center shadow-sm">
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
			{selectedLog && isJsonViewerOpen ? (
				<AuditJsonViewerModal
					log={selectedLog}
					onClose={() => setIsJsonViewerOpen(false)}
				/>
			) : null}
		</ProtectedRoute>
	);
}

function SummaryCard({
	icon: Icon,
	label,
	value,
	helper,
	tone,
}: {
	icon: typeof ShieldCheck;
	label: string;
	value: string;
	helper: string;
	tone: "neutral" | "success" | "danger" | "routing";
}) {
	return (
		<Card
			className={cn(
				"border-border/70 p-4 shadow-sm",
				tone === "success" && "border-emerald-500/20 bg-emerald-500/5",
				tone === "danger" && "border-rose-500/20 bg-rose-500/5",
				tone === "routing" && "border-sky-500/20 bg-sky-500/5",
			)}
		>
			<div className="flex items-start justify-between gap-4">
				<div>
					<p className="text-sm text-muted-foreground">{label}</p>
					<p className="mt-2 text-3xl font-semibold text-foreground">{value}</p>
					<p className="mt-2 text-xs text-muted-foreground">{helper}</p>
				</div>
				<div
					className={cn(
						"rounded-2xl p-3",
						tone === "neutral" && "bg-slate-900 text-white",
						tone === "success" && "bg-emerald-600 text-white",
						tone === "danger" && "bg-rose-600 text-white",
						tone === "routing" && "bg-sky-600 text-white",
					)}
				>
					<Icon className="h-5 w-5" />
				</div>
			</div>
		</Card>
	);
}

function PrioritySpotlightCard({
	icon: Icon,
	title,
	value,
	description,
	buttonLabel,
	tone,
	onClick,
}: {
	icon: typeof ShieldCheck;
	title: string;
	value: string;
	description: string;
	buttonLabel: string;
	tone: "danger" | "routing";
	onClick: () => void;
}) {
	return (
		<Card
			className={cn(
				"overflow-hidden border-border/70 p-0 shadow-sm",
				tone === "danger"
					? "border-rose-300/50 bg-[linear-gradient(135deg,rgba(255,241,242,0.98),rgba(255,228,230,0.95))]"
					: "border-sky-300/50 bg-[linear-gradient(135deg,rgba(240,249,255,0.98),rgba(224,242,254,0.95))]",
			)}
		>
			<div className="flex h-full flex-col gap-4 p-5">
				<div className="flex items-start justify-between gap-4">
					<div>
						<p className="text-sm font-semibold text-foreground">{title}</p>
						<p className="mt-2 text-2xl font-bold text-foreground">{value}</p>
						<p className="mt-2 text-sm leading-6 text-muted-foreground">{description}</p>
					</div>
					<div className={cn("rounded-2xl p-3 text-white", tone === "danger" ? "bg-rose-600" : "bg-sky-600")}>
						<Icon className="h-5 w-5" />
					</div>
				</div>
				<div>
					<Button variant="secondary" onClick={onClick} className="w-full justify-center">
						{buttonLabel}
					</Button>
				</div>
			</div>
		</Card>
	);
}

function AuditMeta({ label, value, className }: { label: string; value: string; className?: string }) {
	return (
		<div className={cn("rounded-2xl border border-border bg-muted/20 px-4 py-3", className)}>
			<p className="text-[11px] uppercase tracking-[0.22em] text-muted-foreground">{label}</p>
			<p className="mt-2 text-sm font-medium leading-6 text-foreground">{value}</p>
		</div>
	);
}

function AuditJsonBlock({ title, value, onOpen }: { title: string; value: unknown; onOpen: () => void }) {
	return (
		<div className="overflow-hidden rounded-2xl border border-border bg-slate-950/95">
			<div className="flex items-center justify-between border-b border-white/10 px-4 py-3">
				<Label className="text-slate-200">{title}</Label>
				<Button variant="ghost" size="icon-sm" className="text-slate-200 hover:bg-white/10 hover:text-white" onClick={onOpen}>
					<Eye className="h-4 w-4" />
				</Button>
			</div>
			<pre className="max-h-80 overflow-auto p-4 text-xs leading-6 text-slate-100">{value ? JSON.stringify(value, null, 2) : "Không có dữ liệu"}</pre>
		</div>
	);
}

function AuditJsonViewerModal({ log, onClose }: { log: AuditLog; onClose: () => void }) {
	return (
		<div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 p-4 backdrop-blur-sm">
			<div className="flex max-h-[90vh] w-full max-w-7xl flex-col overflow-hidden rounded-3xl border border-white/10 bg-slate-950 shadow-2xl">
				<div className="flex items-center justify-between border-b border-white/10 px-6 py-4">
					<div>
						<p className="text-sm font-medium text-slate-400">Trình xem dữ liệu audit</p>
						<h3 className="mt-1 text-xl font-semibold text-white">
							{log.resourceName || log.resourceId || "Không rõ đối tượng"}
						</h3>
					</div>
					<Button variant="ghost" size="icon-sm" className="text-slate-200 hover:bg-white/10 hover:text-white" onClick={onClose}>
						<X className="h-5 w-5" />
					</Button>
				</div>

				<div className="grid flex-1 gap-4 overflow-auto p-6 xl:grid-cols-3">
					<AuditJsonPanel title="Dữ liệu trước thay đổi" value={log.beforeData} />
					<AuditJsonPanel title="Dữ liệu sau thay đổi" value={log.afterData} />
					<AuditJsonPanel title="Metadata" value={log.metadata} />
				</div>
			</div>
		</div>
	);
}

function AuditJsonPanel({ title, value }: { title: string; value: unknown }) {
	return (
		<div className="overflow-hidden rounded-2xl border border-white/10 bg-slate-900/80">
			<div className="border-b border-white/10 px-4 py-3">
				<p className="text-sm font-semibold text-slate-100">{title}</p>
			</div>
			<pre className="max-h-[65vh] overflow-auto p-4 text-xs leading-6 text-slate-100">
				{value ? JSON.stringify(value, null, 2) : "Không có dữ liệu"}
			</pre>
		</div>
	);
}

function StatusBadge({ status }: { status: string }) {
	return (
		<span
			className={cn(
				"inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold",
				status === "SUCCESS" && "bg-emerald-500/15 text-emerald-700",
				status === "FAILED" && "bg-rose-500/15 text-rose-700",
				status !== "SUCCESS" && status !== "FAILED" && "bg-slate-500/15 text-slate-700",
			)}
		>
			{formatStatusLabel(status)}
		</span>
	);
}

function ActionBadge({ action }: { action: string }) {
	const toneClass =
		action === "LOGIN"
			? "bg-sky-500/15 text-sky-700"
			: action === "LOGOUT"
				? "bg-violet-500/15 text-violet-700"
				: action === "CHANGE_PASSWORD"
					? "bg-amber-500/15 text-amber-700"
					: action === "CREATE"
						? "bg-emerald-500/15 text-emerald-700"
						: action === "UPDATE"
							? "bg-indigo-500/15 text-indigo-700"
							: "bg-slate-500/15 text-slate-700";

	return <span className={cn("inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold", toneClass)}>{formatActionLabel(action)}</span>;
}

function ResourceBadge({ resourceType }: { resourceType: string }) {
	return (
		<span
			className={cn(
				"inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold",
				resourceType === "ROUTING_RUN" ? "bg-sky-500/15 text-sky-700" : "bg-slate-900/10 text-slate-700",
			)}
		>
			{formatResourceLabel(resourceType)}
		</span>
	);
}

function isPriorityLog(log: AuditLog) {
	return log.status === "FAILED" || log.resourceType === "ROUTING_RUN";
}

function countActiveFilters(filters: Filters) {
	return [
		filters.search,
		filters.action !== "ALL" ? filters.action : "",
		filters.resourceType !== "ALL" ? filters.resourceType : "",
		filters.actorUsername,
		filters.scopeDepotId !== "ALL" ? filters.scopeDepotId : "",
		filters.status !== "ALL" ? filters.status : "",
		filters.from,
		filters.to,
	].filter(Boolean).length;
}

function buildActiveFilterChips(filters: Filters, depots: Depot[]) {
	const chips: string[] = [];

	if (filters.search) chips.push(`Tìm kiếm: ${filters.search}`);
	if (filters.action !== "ALL") chips.push(`Hành động: ${formatActionLabel(filters.action)}`);
	if (filters.resourceType !== "ALL") chips.push(`Tài nguyên: ${formatResourceLabel(filters.resourceType)}`);
	if (filters.actorUsername) chips.push(`Người thao tác: ${filters.actorUsername}`);
	if (filters.status !== "ALL") chips.push(`Trạng thái: ${formatStatusLabel(filters.status)}`);
	if (filters.scopeDepotId !== "ALL") {
		const depot = depots.find((item) => String(item.id) === filters.scopeDepotId);
		chips.push(`Kho: ${depot?.name || `#${filters.scopeDepotId}`}`);
	}
	if (filters.from) chips.push(`Từ: ${formatDateTime(new Date(filters.from).toISOString())}`);
	if (filters.to) chips.push(`Đến: ${formatDateTime(new Date(filters.to).toISOString())}`);

	return chips;
}

function buildChangeSummary(log: AuditLog | null) {
	if (!log) {
		return [];
	}

	const beforeData = log.beforeData;
	const afterData = log.afterData;

	if (beforeData == null && afterData == null) {
		return [];
	}

	if (Array.isArray(beforeData) || Array.isArray(afterData)) {
		return buildArrayChangeSummary(beforeData, afterData);
	}

	if (isRecord(beforeData) && isRecord(afterData)) {
		const keys = new Set([...Object.keys(beforeData), ...Object.keys(afterData)]);
		return [...keys]
			.filter((key) => JSON.stringify(beforeData[key]) !== JSON.stringify(afterData[key]))
			.map((key) => `${formatFieldLabel(key)}: ${shortValue(beforeData[key])} -> ${shortValue(afterData[key])}`)
			.slice(0, 12);
	}

	if (beforeData == null && isRecord(afterData)) {
		return Object.keys(afterData)
			.slice(0, 12)
			.map((key) => `${formatFieldLabel(key)}: ${shortValue(afterData[key])}`);
	}

	if (afterData == null && isRecord(beforeData)) {
		return Object.keys(beforeData)
			.slice(0, 12)
			.map((key) => `${formatFieldLabel(key)}: ${shortValue(beforeData[key])}`);
	}

	if (JSON.stringify(beforeData) !== JSON.stringify(afterData)) {
		return [`Giá trị thay đổi: ${shortValue(beforeData)} -> ${shortValue(afterData)}`];
	}

	return [];
}

function isRecord(value: unknown): value is Record<string, unknown> {
	return typeof value === "object" && value !== null && !Array.isArray(value);
}

function shortValue(value: unknown) {
	if (value === null || value === undefined) {
		return "trống";
	}
	if (Array.isArray(value)) {
		return value.length === 0 ? "[]" : `[${value.length} mục]`;
	}
	if (typeof value === "object") {
		return "{...}";
	}
	const text = String(value);
	return text.length > 28 ? `${text.slice(0, 25)}...` : text;
}

function buildArrayChangeSummary(beforeData: unknown, afterData: unknown) {
	const beforeArray = Array.isArray(beforeData) ? beforeData : [];
	const afterArray = Array.isArray(afterData) ? afterData : [];

	if (beforeArray.length === 0 && afterArray.length > 0) {
		return [`Thêm mới ${afterArray.length} mục`, ...summarizeArrayItems(afterArray)].slice(0, 12);
	}

	if (afterArray.length === 0 && beforeArray.length > 0) {
		return [`Xóa ${beforeArray.length} mục`, ...summarizeArrayItems(beforeArray)].slice(0, 12);
	}

	if (beforeArray.length !== afterArray.length) {
		return [`Số lượng mục: ${beforeArray.length} -> ${afterArray.length}`, ...summarizeArrayItems(afterArray.length > 0 ? afterArray : beforeArray)].slice(0, 12);
	}

	const changedIndexes = beforeArray
		.map((item, index) => ({ item, index }))
		.filter(({ item, index }) => JSON.stringify(item) !== JSON.stringify(afterArray[index]))
		.slice(0, 6);

	if (changedIndexes.length > 0) {
		return changedIndexes.map(({ item, index }) => {
			const nextItem = afterArray[index];
			if (isRecord(item) && isRecord(nextItem)) {
				const label = getArrayItemLabel(nextItem) || getArrayItemLabel(item) || `Mục ${index + 1}`;
				return `${label}: đã thay đổi`;
			}
			return `Mục ${index + 1}: ${shortValue(item)} -> ${shortValue(nextItem)}`;
		});
	}

	if (JSON.stringify(beforeArray) !== JSON.stringify(afterArray)) {
		return [`Dữ liệu danh sách đã thay đổi (${afterArray.length} mục)`];
	}

	return [];
}

function summarizeArrayItems(items: unknown[]) {
	return items.slice(0, 6).map((item, index) => {
		if (isRecord(item)) {
			return `${getArrayItemLabel(item) || `Mục ${index + 1}`}: ${summarizeObjectKeys(item)}`;
		}
		return `Mục ${index + 1}: ${shortValue(item)}`;
	});
}

function getArrayItemLabel(item: Record<string, unknown>) {
	const labelCandidate = item.code ?? item.name ?? item.username ?? item.id;
	return labelCandidate ? String(labelCandidate) : null;
}

function summarizeObjectKeys(item: Record<string, unknown>) {
	return Object.keys(item)
		.slice(0, 3)
		.map((key) => formatFieldLabel(key))
		.join(", ");
}

function formatFieldLabel(key: string) {
	return key
		.replace(/([a-z])([A-Z])/g, "$1 $2")
		.replace(/_/g, " ")
		.replace(/\bid\b/gi, "ID")
		.replace(/^./, (char) => char.toUpperCase());
}

function toIsoDateTime(value: string) {
	if (!value) {
		return undefined;
	}

	const date = new Date(value);
	return Number.isNaN(date.getTime()) ? undefined : date.toISOString();
}

function toDateTimeLocalValue(date: Date) {
	const pad = (value: number) => String(value).padStart(2, "0");
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function formatDateTime(value: string) {
	try {
		return new Intl.DateTimeFormat("vi-VN", {
			day: "2-digit",
			month: "2-digit",
			year: "numeric",
			hour: "2-digit",
			minute: "2-digit",
			second: "2-digit",
		}).format(new Date(value));
	} catch {
		return value;
	}
}

function formatDepotScope(scopeDepotId: number | null) {
	return scopeDepotId ? `Kho #${scopeDepotId}` : "Toàn hệ thống";
}

function formatActionLabel(action: string) {
	switch (action) {
		case "LOGIN":
			return "Đăng nhập";
		case "LOGOUT":
			return "Đăng xuất";
		case "CHANGE_PASSWORD":
			return "Đổi mật khẩu";
		case "CREATE":
			return "Tạo mới";
		case "UPDATE":
			return "Cập nhật";
		case "DELETE":
			return "Xóa";
		case "BULK_UPDATE":
			return "Cập nhật hàng loạt";
		case "EXECUTE":
			return "Thực thi";
		default:
			return action;
	}
}

function formatResourceLabel(resourceType: string) {
	switch (resourceType) {
		case "AUTH":
			return "Xác thực";
		case "USER":
			return "Tài khoản";
		case "COMPANY":
			return "Công ty";
		case "DEPOT":
			return "Kho";
		case "DRIVER":
			return "Lái xe";
		case "VEHICLE":
			return "Phương tiện";
		case "ORDER":
			return "Đơn hàng";
		case "ROUTING_RUN":
			return "Tối ưu tuyến";
		default:
			return resourceType;
	}
}

function formatStatusLabel(status: string) {
	switch (status) {
		case "SUCCESS":
			return "Thành công";
		case "FAILED":
			return "Thất bại";
		default:
			return status;
	}
}
