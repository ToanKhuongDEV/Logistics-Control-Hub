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
	RefreshCw,
	Search,
	ShieldAlert,
	ShieldCheck,
	UserSearch,
	Waypoints,
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
			toast.error(error?.response?.data?.message || "Khong the tai nhat ky audit");
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
			return "Dang tai du lieu audit...";
		}
		if (totalElements === 0) {
			return "Chua co ban ghi phu hop voi bo loc hien tai.";
		}
		return `Dang hien thi ${logs.length} / ${totalElements} ban ghi`;
	}, [isLoading, logs.length, totalElements]);

	const activeFilterCount = useMemo(() => countActiveFilters(filters), [filters]);
	const currentPageSuccessCount = useMemo(() => logs.filter((log) => log.status === "SUCCESS").length, [logs]);
	const currentPageFailedCount = useMemo(() => logs.filter((log) => log.status === "FAILED").length, [logs]);

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

	const handlePageChange = async (nextPage: number) => {
		setPage(nextPage);
		await fetchAuditLogs(nextPage, filters);
	};

	const copyValue = async (value: string | null | undefined, label: string) => {
		if (!value) {
			toast.error(`Khong co ${label.toLowerCase()} de sao chep`);
			return;
		}

		try {
			await navigator.clipboard.writeText(value);
			toast.success(`Da sao chep ${label.toLowerCase()}`);
		} catch (error) {
			console.error("Copy failed:", error);
			toast.error(`Khong the sao chep ${label.toLowerCase()}`);
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
										Admin Audit Console
									</div>
									<div>
										<h1 className="text-3xl font-bold text-foreground md:text-4xl">Audit Logs</h1>
										<p className="mt-2 max-w-3xl text-sm leading-6 text-muted-foreground md:text-base">
											Theo doi thao tac nhay cam, doi chieu du lieu truoc va sau, va truy vet request giup admin dieu tra nhanh hon.
										</p>
									</div>
								</div>

								<div className="grid gap-3 sm:grid-cols-2 xl:w-[460px]">
									<SummaryCard
										icon={Waypoints}
										label="Tong ket qua"
										value={String(totalElements)}
										helper={resultSummary}
										tone="neutral"
									/>
									<SummaryCard
										icon={AlertTriangle}
										label="Ban ghi that bai"
										value={String(currentPageFailedCount)}
										helper="Tinh tren trang hien tai"
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
										label="Trang hien tai"
										value={String(logs.length)}
										helper="So dong dang hien thi"
										tone="neutral"
									/>
									<SummaryCard
										icon={CheckCircle2}
										label="Success"
										value={String(currentPageSuccessCount)}
										helper="Tac vu thanh cong"
										tone="success"
									/>
									<SummaryCard
										icon={AlertTriangle}
										label="Failed"
										value={String(currentPageFailedCount)}
										helper="Can uu tien kiem tra"
										tone="danger"
									/>
									<SummaryCard
										icon={UserSearch}
										label="Bo loc dang bat"
										value={String(activeFilterCount)}
										helper={activeFilterCount > 0 ? "Da khoanh vung ngu canh" : "Dang xem toan bo"}
										tone="neutral"
									/>
								</div>

								<Card className="overflow-hidden border-border/70 bg-card/95 shadow-sm">
									<div className="border-b border-border bg-muted/20 px-6 py-4">
										<div className="flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
											<div>
												<h2 className="text-lg font-semibold text-foreground">Bo loc dieu tra</h2>
												<p className="text-sm text-muted-foreground">Loc theo nguoi thao tac, loai su kien, depot phu trach va khoang thoi gian.</p>
											</div>
											<div className="inline-flex items-center gap-2 text-sm text-muted-foreground">
												<CalendarRange className="h-4 w-4" />
												{activeFilterCount > 0 ? `${activeFilterCount} bo loc dang ap dung` : "Chua ap dung bo loc"}
											</div>
										</div>
									</div>

									<div className="space-y-5 p-6">
										<div className="grid gap-4 lg:grid-cols-3 xl:grid-cols-8">
											<div className="xl:col-span-2">
												<Label htmlFor="audit-search">Tim nhanh</Label>
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
														<SelectValue placeholder="Tat ca action" />
													</SelectTrigger>
													<SelectContent>
														<SelectItem value="ALL">Tat ca action</SelectItem>
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
														<SelectValue placeholder="Tat ca resource" />
													</SelectTrigger>
													<SelectContent>
														<SelectItem value="ALL">Tat ca resource</SelectItem>
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
														<SelectValue placeholder="Tat ca trang thai" />
													</SelectTrigger>
													<SelectContent>
														<SelectItem value="ALL">Tat ca trang thai</SelectItem>
														{STATUS_OPTIONS.map((status) => (
															<SelectItem key={status} value={status}>
																{status}
															</SelectItem>
														))}
													</SelectContent>
												</Select>
											</div>
											<div>
												<Label htmlFor="audit-actor">Nguoi thao tac</Label>
												<Input
													id="audit-actor"
													className="mt-2"
													placeholder="Nhap username"
													value={filters.actorUsername}
													onChange={(event) => setFilters((current) => ({ ...current, actorUsername: event.target.value }))}
												/>
											</div>
											<div>
												<Label>Kho phu trach</Label>
												<Select value={filters.scopeDepotId} onValueChange={(value) => setFilters((current) => ({ ...current, scopeDepotId: value }))}>
													<SelectTrigger className="mt-2 w-full">
														<SelectValue placeholder="Tat ca kho" />
													</SelectTrigger>
													<SelectContent>
														<SelectItem value="ALL">Tat ca kho</SelectItem>
														{depots.map((depot) => (
															<SelectItem key={depot.id} value={String(depot.id)}>
																{depot.name}
															</SelectItem>
														))}
													</SelectContent>
												</Select>
											</div>
											<div>
												<Label htmlFor="audit-from">Tu thoi diem</Label>
												<Input
													id="audit-from"
													type="datetime-local"
													className="mt-2"
													value={filters.from}
													onChange={(event) => setFilters((current) => ({ ...current, from: event.target.value }))}
												/>
											</div>
											<div>
												<Label htmlFor="audit-to">Den thoi diem</Label>
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
												Ap dung bo loc
											</Button>
											<Button variant="outline" onClick={() => void resetFilters()} className="gap-2">
												<RefreshCw className="h-4 w-4" />
												Dat lai
											</Button>
											<Button variant="outline" onClick={() => void applyDatePreset(1)} className="gap-2">
												24h gan nhat
											</Button>
											<Button variant="outline" onClick={() => void applyDatePreset(7)} className="gap-2">
												7 ngay
											</Button>
											<Button variant="outline" onClick={() => void applyDatePreset(30)} className="gap-2">
												30 ngay
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
													<h2 className="text-lg font-semibold text-foreground">Danh sach ban ghi</h2>
													<p className="text-sm text-muted-foreground">Chon mot dong de mo panel dieu tra chi tiet.</p>
												</div>
												<div className="rounded-full border border-border bg-muted/30 px-3 py-1 text-xs font-medium text-muted-foreground">
													Page {Math.min(page + 1, Math.max(totalPages, 1))} / {Math.max(totalPages, 1)}
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
																		<p className="text-sm font-semibold text-foreground">{log.resourceName || log.resourceId || "Khong co ma doi tuong"}</p>
																		<p className="mt-1 text-sm text-muted-foreground">
																			{log.actorUsername || "An danh"} {log.actorRole ? `- ${log.actorRole}` : ""}
																		</p>
																	</div>
																</div>
																<div className="space-y-1 text-right text-xs text-muted-foreground">
																	<p>{formatDateTime(log.createdAt)}</p>
																	<p>{formatDepotScope(log.scopeDepotId)}</p>
																</div>
															</div>

															<p className="mt-3 line-clamp-2 text-sm leading-6 text-muted-foreground">
																{log.message || "Khong co thong diep bo sung cho ban ghi nay."}
															</p>

															<div className="mt-3 flex flex-wrap gap-2 text-xs text-muted-foreground">
																<span className="rounded-full bg-muted/50 px-2.5 py-1">Request {log.requestId || "N/A"}</span>
																<span className="rounded-full bg-muted/50 px-2.5 py-1">IP {log.ipAddress || "N/A"}</span>
															</div>
														</button>
													))
												) : (
													<div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center text-sm text-muted-foreground">
														{isLoading ? "Dang tai du lieu..." : "Khong co ban ghi audit phu hop."}
													</div>
												)}
											</div>
										</div>

										<div className="flex items-center justify-between border-t border-border px-6 py-4">
											<p className="text-sm text-muted-foreground">{resultSummary}</p>
											<div className="flex gap-2">
												<Button variant="outline" size="sm" onClick={() => void handlePageChange(page - 1)} disabled={!canGoPrev || isLoading} className="gap-2">
													<ChevronLeft className="h-4 w-4" />
													Truoc
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
													<h2 className="text-lg font-semibold text-foreground">Chi tiet ban ghi</h2>
													<p className="text-sm text-muted-foreground">Doc ngu canh su kien, request va du lieu thay doi tren cung mot man hinh.</p>
												</div>
												{selectedLog ? (
													<div className="flex gap-2">
														<Button variant="outline" size="sm" onClick={() => void copyValue(selectedLog.requestId, "Request ID")}>
															<Copy className="h-4 w-4" />
															Request ID
														</Button>
														<Button variant="outline" size="sm" onClick={() => void copyValue(selectedLog.resourceId, "Resource ID")}>
															<Copy className="h-4 w-4" />
															Resource ID
														</Button>
													</div>
												) : null}
											</div>
										</div>

										<div className="max-h-[860px] overflow-y-auto p-6">
											{selectedLog ? (
												<div className="space-y-6">
													<div className="rounded-2xl border border-border bg-[linear-gradient(135deg,rgba(15,23,42,0.98),rgba(30,41,59,0.95))] p-5 text-slate-100 shadow-sm">
														<div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
															<div className="space-y-3">
																<div className="flex flex-wrap items-center gap-2">
																	<ActionBadge action={selectedLog.action} />
																	<StatusBadge status={selectedLog.status} />
																	<ResourceBadge resourceType={selectedLog.resourceType} />
																</div>
																<div>
																	<p className="text-xs uppercase tracking-[0.22em] text-slate-400">Target Resource</p>
																	<h3 className="mt-2 text-2xl font-semibold text-white">{selectedLog.resourceName || selectedLog.resourceId || "Unknown Resource"}</h3>
																	<p className="mt-2 text-sm text-slate-300">{selectedLog.message || "Khong co thong diep bo sung."}</p>
																</div>
															</div>
															<div className="rounded-2xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-slate-200">
																<p className="text-xs uppercase tracking-[0.2em] text-slate-400">Occurred At</p>
																<p className="mt-2 font-medium text-white">{formatDateTime(selectedLog.createdAt)}</p>
															</div>
														</div>
													</div>

													<div className="grid gap-3 md:grid-cols-2">
														<AuditMeta label="Nguoi thao tac" value={selectedLog.actorUsername || "An danh"} />
														<AuditMeta label="Vai tro" value={selectedLog.actorRole || "N/A"} />
														<AuditMeta label="Action" value={selectedLog.action} />
														<AuditMeta label="Resource" value={selectedLog.resourceType} />
														<AuditMeta label="Ma doi tuong" value={selectedLog.resourceId || "N/A"} />
														<AuditMeta label="Kho pham vi" value={formatDepotScope(selectedLog.scopeDepotId)} />
														<AuditMeta label="Request ID" value={selectedLog.requestId || "N/A"} />
														<AuditMeta label="IP address" value={selectedLog.ipAddress || "N/A"} />
														<AuditMeta label="User agent" value={selectedLog.userAgent || "N/A"} className="md:col-span-2" />
													</div>

													<div className="rounded-2xl border border-border bg-muted/20 p-5">
														<div className="flex items-center justify-between gap-3">
															<div>
																<h3 className="font-semibold text-foreground">Tom tat thay doi</h3>
																<p className="text-sm text-muted-foreground">Rut gon cac key thay doi de admin quet nhanh truoc khi xem JSON.</p>
															</div>
															<span className="rounded-full border border-border bg-background px-3 py-1 text-xs font-medium text-muted-foreground">
																{highlightedChanges.length} muc
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
															<p className="mt-4 text-sm text-muted-foreground">Ban ghi nay khong co snapshot before/after hoac khong phat hien truong thay doi.</p>
														)}
													</div>

													<div className="grid gap-4 xl:grid-cols-3">
														<AuditJsonBlock title="Before Data" value={selectedLog.beforeData} />
														<AuditJsonBlock title="After Data" value={selectedLog.afterData} />
														<AuditJsonBlock title="Metadata" value={selectedLog.metadata} />
													</div>
												</div>
											) : (
												<div className="rounded-2xl border border-dashed border-border px-6 py-16 text-center">
													<p className="text-lg font-semibold text-foreground">Chon mot ban ghi audit</p>
													<p className="mt-2 text-sm text-muted-foreground">Panel nay se hien request context, snapshot du lieu va tom tat thay doi.</p>
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
								<h2 className="text-xl font-semibold text-foreground">Chi admin moi duoc xem audit logs</h2>
								<p className="mt-2 text-muted-foreground">Trang nay dung de dieu tra thao tac he thong va theo doi thay doi nhay cam, nen dang duoc gioi han o quyen `audit.read`.</p>
							</Card>
						)}
					</div>
				</div>
			</DashboardLayout>
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
	tone: "neutral" | "success" | "danger";
}) {
	return (
		<Card
			className={cn(
				"border-border/70 p-4 shadow-sm",
				tone === "success" && "border-emerald-500/20 bg-emerald-500/5",
				tone === "danger" && "border-rose-500/20 bg-rose-500/5",
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
					)}
				>
					<Icon className="h-5 w-5" />
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

function AuditJsonBlock({ title, value }: { title: string; value: unknown }) {
	return (
		<div className="overflow-hidden rounded-2xl border border-border bg-slate-950/95">
			<div className="border-b border-white/10 px-4 py-3">
				<Label className="text-slate-200">{title}</Label>
			</div>
			<pre className="max-h-80 overflow-auto p-4 text-xs leading-6 text-slate-100">{value ? JSON.stringify(value, null, 2) : "Khong co du lieu"}</pre>
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
			{status}
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

	return <span className={cn("inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold", toneClass)}>{action}</span>;
}

function ResourceBadge({ resourceType }: { resourceType: string }) {
	return <span className="inline-flex items-center rounded-full bg-slate-900/10 px-2.5 py-1 text-xs font-semibold text-slate-700">{resourceType}</span>;
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

	if (filters.search) chips.push(`Search: ${filters.search}`);
	if (filters.action !== "ALL") chips.push(`Action: ${filters.action}`);
	if (filters.resourceType !== "ALL") chips.push(`Resource: ${filters.resourceType}`);
	if (filters.actorUsername) chips.push(`Actor: ${filters.actorUsername}`);
	if (filters.status !== "ALL") chips.push(`Status: ${filters.status}`);
	if (filters.scopeDepotId !== "ALL") {
		const depot = depots.find((item) => String(item.id) === filters.scopeDepotId);
		chips.push(`Depot: ${depot?.name || `#${filters.scopeDepotId}`}`);
	}
	if (filters.from) chips.push(`From: ${formatDateTime(new Date(filters.from).toISOString())}`);
	if (filters.to) chips.push(`To: ${formatDateTime(new Date(filters.to).toISOString())}`);

	return chips;
}

function buildChangeSummary(log: AuditLog | null) {
	if (!log) {
		return [];
	}

	const beforeData = log.beforeData;
	const afterData = log.afterData;

	if (!isRecord(beforeData) || !isRecord(afterData)) {
		return [];
	}

	const keys = new Set([...Object.keys(beforeData), ...Object.keys(afterData)]);
	return [...keys]
		.filter((key) => JSON.stringify(beforeData[key]) !== JSON.stringify(afterData[key]))
		.map((key) => `${key}: ${shortValue(beforeData[key])} -> ${shortValue(afterData[key])}`)
		.slice(0, 12);
}

function isRecord(value: unknown): value is Record<string, unknown> {
	return typeof value === "object" && value !== null && !Array.isArray(value);
}

function shortValue(value: unknown) {
	if (value === null || value === undefined) {
		return "empty";
	}
	if (Array.isArray(value)) {
		return value.length === 0 ? "[]" : `[${value.length} items]`;
	}
	if (typeof value === "object") {
		return "{...}";
	}
	const text = String(value);
	return text.length > 28 ? `${text.slice(0, 25)}...` : text;
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
	return scopeDepotId ? `Kho #${scopeDepotId}` : "Global scope";
}
