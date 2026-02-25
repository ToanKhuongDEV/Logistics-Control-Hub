"use client";

import { DashboardLayout } from "@/components/dashboard-layout";
import { ProtectedRoute } from "@/components/protected-route";
import { routingApi, RoutingRun, RoutingHistoryPage } from "@/lib/routing-api";
import { depotApi } from "@/lib/depot-api";
import { Depot } from "@/types/depot-types";
import dynamic from "next/dynamic";
import { useState, useEffect, useCallback } from "react";
import { History, Warehouse, Route as RouteIcon, CheckCircle, XCircle, Clock, MapPin, DollarSign, ChevronLeft, ChevronRight } from "lucide-react";

const LeafletMap = dynamic(() => import("@/components/leaflet-map").then((mod) => mod.LeafletMap), {
	ssr: false,
	loading: () => <p className="h-full w-full flex items-center justify-center bg-muted/30 rounded-xl text-muted-foreground text-sm">Đang tải bản đồ...</p>,
});

function formatDate(dateStr: string | null | undefined): string {
	if (!dateStr) return "--";
	try {
		return new Intl.DateTimeFormat("vi-VN", {
			day: "2-digit",
			month: "2-digit",
			year: "numeric",
			hour: "2-digit",
			minute: "2-digit",
		}).format(new Date(dateStr));
	} catch {
		return dateStr;
	}
}

function StatusBadge({ status }: { status: string }) {
	if (status === "COMPLETED")
		return (
			<span className="flex items-center gap-1 px-2 py-0.5 bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 rounded-full text-xs font-medium">
				<CheckCircle className="w-3 h-3" /> Hoàn thành
			</span>
		);
	if (status === "FAILED")
		return (
			<span className="flex items-center gap-1 px-2 py-0.5 bg-red-500/15 text-red-600 dark:text-red-400 rounded-full text-xs font-medium">
				<XCircle className="w-3 h-3" /> Thất bại
			</span>
		);
	return (
		<span className="flex items-center gap-1 px-2 py-0.5 bg-yellow-500/15 text-yellow-600 dark:text-yellow-400 rounded-full text-xs font-medium">
			<Clock className="w-3 h-3" /> {status}
		</span>
	);
}

export default function HistoryPage() {
	const [depots, setDepots] = useState<Depot[]>([]);
	const [selectedDepotId, setSelectedDepotId] = useState<number | null>(null);
	const [isLoadingDepots, setIsLoadingDepots] = useState(true);

	const [historyPage, setHistoryPage] = useState<RoutingHistoryPage | null>(null);
	const [isLoadingHistory, setIsLoadingHistory] = useState(false);
	const [currentPage, setCurrentPage] = useState(0);

	const [selectedRun, setSelectedRun] = useState<RoutingRun | null>(null);
	const [isLoadingRun, setIsLoadingRun] = useState(false);

	// Fetch depots on mount
	useEffect(() => {
		const fetchDepots = async () => {
			try {
				const result = await depotApi.getDepots({ size: 100 });
				setDepots(result.data.filter((d) => d.isActive));
			} catch (e) {
				console.error(e);
			} finally {
				setIsLoadingDepots(false);
			}
		};
		fetchDepots();
	}, []);

	// Fetch history when depot or page changes
	const fetchHistory = useCallback(async () => {
		if (selectedDepotId === null) return;
		setIsLoadingHistory(true);
		setSelectedRun(null);
		try {
			const data = await routingApi.getHistoryByDepot(selectedDepotId, currentPage, 10);
			setHistoryPage(data);
		} catch (e) {
			console.error(e);
			setHistoryPage(null);
		} finally {
			setIsLoadingHistory(false);
		}
	}, [selectedDepotId, currentPage]);

	useEffect(() => {
		fetchHistory();
	}, [fetchHistory]);

	// Reset to page 0 when depot changes
	const handleDepotChange = (id: number | null) => {
		setSelectedDepotId(id);
		setCurrentPage(0);
		setSelectedRun(null);
		setHistoryPage(null);
	};

	// Fetch full run details (including routes/stops) on click
	const handleSelectRun = async (run: RoutingRun) => {
		if (selectedRun?.id === run.id) return;
		setIsLoadingRun(true);
		try {
			const full = await routingApi.getRoutingRunById(run.id);
			setSelectedRun(full);
		} catch (e) {
			console.error(e);
			setSelectedRun(run);
		} finally {
			setIsLoadingRun(false);
		}
	};

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="p-8 space-y-6 min-h-screen">
					{/* Header */}
					<div className="flex justify-between items-center flex-wrap gap-4">
						<div className="flex items-center gap-3">
							<div className="p-2 bg-primary/10 rounded-lg">
								<History className="w-6 h-6 text-primary" />
							</div>
							<div>
								<h1 className="text-3xl font-bold text-foreground">Lịch sử tối ưu</h1>
								<p className="text-sm text-muted-foreground">Xem lại các lần tối ưu tuyến đường trước đây</p>
							</div>
						</div>

						{/* Depot Selector */}
						<div className="flex items-center gap-2">
							<Warehouse className="w-4 h-4 text-muted-foreground" />
							<select
								id="history-depot-select"
								value={selectedDepotId ?? ""}
								onChange={(e) => handleDepotChange(e.target.value ? Number(e.target.value) : null)}
								disabled={isLoadingDepots}
								className="border border-input bg-background text-foreground rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50 disabled:cursor-not-allowed min-w-[220px]"
							>
								<option value="">-- Chọn kho --</option>
								{depots.map((depot) => (
									<option key={depot.id} value={depot.id}>
										{depot.name}
									</option>
								))}
							</select>
						</div>
					</div>

					{/* Body: list + map */}
					{selectedDepotId === null ? (
						<div className="flex flex-col items-center justify-center h-80 gap-3 text-muted-foreground">
							<Warehouse className="w-12 h-12 opacity-30" />
							<p className="text-lg font-medium">Chọn kho để xem lịch sử</p>
						</div>
					) : (
						<div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
							{/* LEFT: Run List */}
							<div className="lg:col-span-4 flex flex-col gap-3">
								<h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider">
									Danh sách các lần chạy
									{historyPage && <span className="ml-2 font-normal normal-case">({historyPage.totalElements} lần)</span>}
								</h2>

								{isLoadingHistory ? (
									<div className="space-y-3">
										{Array.from({ length: 5 }).map((_, i) => (
											<div key={i} className="h-20 bg-muted/40 animate-pulse rounded-xl" />
										))}
									</div>
								) : !historyPage || historyPage.content.length === 0 ? (
									<div className="flex flex-col items-center justify-center h-48 gap-2 text-muted-foreground border border-dashed border-border rounded-xl">
										<RouteIcon className="w-8 h-8 opacity-30" />
										<p className="text-sm">Chưa có lần tối ưu nào</p>
									</div>
								) : (
									<>
										<div className="space-y-2">
											{historyPage.content.map((run) => {
												const isActive = selectedRun?.id === run.id;
												return (
													<button
														id={`history-run-${run.id}`}
														key={run.id}
														onClick={() => handleSelectRun(run)}
														className={`w-full text-left rounded-xl border p-4 transition-all duration-200 hover:border-primary/50 hover:shadow-sm ${isActive ? "border-primary bg-primary/5 shadow-md ring-1 ring-primary/20" : "border-border bg-card hover:bg-accent/30"}`}
													>
														<div className="flex justify-between items-start mb-2">
															<span className="text-sm font-semibold text-foreground">Lần #{run.id}</span>
															<StatusBadge status={run.status} />
														</div>
														<p className="text-xs text-muted-foreground mb-2">{formatDate(run.createdAt || run.startTime)}</p>
														<div className="flex gap-3 text-xs text-muted-foreground">
															<span className="flex items-center gap-1">
																<MapPin className="w-3 h-3" />
																{run.totalDistanceKm != null ? `${Number(run.totalDistanceKm).toFixed(1)} km` : "--"}
															</span>
															<span className="flex items-center gap-1">
																<RouteIcon className="w-3 h-3" />
																{run.routes ? `${run.routes.length} tuyến` : "--"}
															</span>
															<span className="flex items-center gap-1">
																<DollarSign className="w-3 h-3" />
																{run.totalCost != null ? `${Number(run.totalCost).toLocaleString("vi-VN")} ₫` : "--"}
															</span>
														</div>
													</button>
												);
											})}
										</div>

										{/* Pagination */}
										{historyPage.totalPages > 1 && (
											<div className="flex items-center justify-between pt-2">
												<button onClick={() => setCurrentPage((p) => Math.max(0, p - 1))} disabled={currentPage === 0} className="flex items-center gap-1 px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-accent disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
													<ChevronLeft className="w-4 h-4" /> Trước
												</button>
												<span className="text-xs text-muted-foreground">
													{currentPage + 1} / {historyPage.totalPages}
												</span>
												<button
													onClick={() => setCurrentPage((p) => Math.min(historyPage.totalPages - 1, p + 1))}
													disabled={currentPage >= historyPage.totalPages - 1}
													className="flex items-center gap-1 px-3 py-1.5 text-sm border border-border rounded-lg hover:bg-accent disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
												>
													Sau <ChevronRight className="w-4 h-4" />
												</button>
											</div>
										)}
									</>
								)}
							</div>

							{/* RIGHT: Map + Summary */}
							<div className="lg:col-span-8 flex flex-col gap-4">
								{/* Summary Cards */}
								{selectedRun && (
									<div className="grid grid-cols-3 gap-3">
										<div className="bg-card border border-border rounded-xl p-4 flex flex-col gap-1">
											<span className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Tổng quãng đường</span>
											<span className="text-2xl font-bold text-foreground">{selectedRun.totalDistanceKm != null ? `${Number(selectedRun.totalDistanceKm).toFixed(2)}` : "--"}</span>
											<span className="text-xs text-muted-foreground">km</span>
										</div>
										<div className="bg-card border border-border rounded-xl p-4 flex flex-col gap-1">
											<span className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Số tuyến đường</span>
											<span className="text-2xl font-bold text-foreground">{selectedRun.routes ? selectedRun.routes.length : "--"}</span>
											<span className="text-xs text-muted-foreground">tuyến</span>
										</div>
										<div className="bg-card border border-border rounded-xl p-4 flex flex-col gap-1">
											<span className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Tổng chi phí</span>
											<span className="text-2xl font-bold text-foreground">{selectedRun.totalCost != null ? Number(selectedRun.totalCost).toLocaleString("vi-VN") : "--"}</span>
											<span className="text-xs text-muted-foreground">VND</span>
										</div>
									</div>
								)}

								{/* Map */}
								<div className="relative flex-1 min-h-[440px]">
									{isLoadingRun && (
										<div className="absolute inset-0 z-10 flex items-center justify-center bg-background/60 rounded-xl">
											<div className="flex flex-col items-center gap-2 text-muted-foreground">
												<div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
												<span className="text-sm">Đang tải tuyến đường...</span>
											</div>
										</div>
									)}

									{!selectedRun && !isLoadingRun ? (
										<div className="h-full min-h-[440px] flex flex-col items-center justify-center gap-3 text-muted-foreground bg-muted/20 rounded-xl border border-dashed border-border">
											<MapPin className="w-10 h-10 opacity-30" />
											<p className="text-base font-medium">Chọn một lần chạy để xem bản đồ</p>
											<p className="text-sm opacity-60">Tuyến đường sẽ hiển thị ở đây</p>
										</div>
									) : (
										<div className="h-full min-h-[440px] rounded-xl overflow-hidden border border-border">
											<LeafletMap routes={selectedRun?.routes ?? []} />
										</div>
									)}
								</div>

								{/* Run Info */}
								{selectedRun && (
									<div className="bg-card border border-border rounded-xl p-4 text-xs text-muted-foreground">
										<span className="font-medium text-foreground">Cấu hình: </span>
										{selectedRun.configuration || "--"}
									</div>
								)}
							</div>
						</div>
					)}
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
