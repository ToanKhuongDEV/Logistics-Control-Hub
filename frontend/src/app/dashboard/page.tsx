"use client";

import { DashboardLayout } from "@/components/dashboard-layout";
import { KPICard } from "@/components/kpi-card";
import { ShipmentList } from "@/components/shipment-list";
import { Truck, Package, TrendingUp, Route as RouteIcon, Warehouse } from "lucide-react";
import { ProtectedRoute } from "@/components/protected-route";
import dynamic from "next/dynamic";
import { useState, useEffect } from "react";
import { routingApi, Route } from "@/lib/routing-api";
import { dashboardApi } from "@/lib/dashboard-api";
import { depotApi } from "@/lib/depot-api";
import { DashboardStatistics } from "@/types/dashboard-types";
import { Depot } from "@/types/depot-types";

const LeafletMap = dynamic(() => import("@/components/leaflet-map").then((mod) => mod.LeafletMap), {
	ssr: false,
	loading: () => <p className="h-[400px] w-full flex items-center justify-center bg-gray-100 rounded-lg">Đang tải bản đồ...</p>,
});

const shipments = [
	{
		id: "SHIP-1045",
		location: "Atlanta, GA - ETA: 14:30",
		eta: "14:30",
		status: "on-time" as const,
	},
	{
		id: "SHIP-1046",
		location: "Dallas, TX - ETA: 16:00",
		eta: "16:00",
		status: "delayed" as const,
	},
	{
		id: "SHIP-1047",
		location: "Chicago, IL - ETA: 10:45",
		eta: "10:45",
		status: "in-transit" as const,
	},
	{
		id: "SHIP-1048",
		location: "Atlanta, DL - ETA: 14:30",
		eta: "14:30",
		status: "on-time" as const,
	},
	{
		id: "SHIP-1049",
		location: "Atlanta, GA - ETA: 14:30",
		eta: "14:30",
		status: "on-time" as const,
	},
	{
		id: "SHIP-1050",
		location: "Dallas, TX - ETA: 16:00",
		eta: "16:00",
		status: "delayed" as const,
	},
	{
		id: "SHIP-1051",
		location: "Chicago, IL - ETA: 10:45",
		eta: "10:45",
		status: "in-transit" as const,
	},
	{
		id: "SHIP-1052",
		location: "Atlanta, GA - ETA: 14:30",
		eta: "14:30",
		status: "on-time" as const,
	},
];

export default function DashboardPage() {
	const [routes, setRoutes] = useState<Route[]>([]);
	const [isOptimizing, setIsOptimizing] = useState(false);
	const [optimizationResult, setOptimizationResult] = useState<string | null>(null);
	const [error, setError] = useState<string | null>(null);
	const [statistics, setStatistics] = useState<DashboardStatistics | null>(null);
	const [isLoadingStats, setIsLoadingStats] = useState(true);
	const [depots, setDepots] = useState<Depot[]>([]);
	const [selectedDepotId, setSelectedDepotId] = useState<number | null>(null);
	const [isLoadingDepots, setIsLoadingDepots] = useState(true);

	const handleOptimizeRoutes = async () => {
		if (selectedDepotId === null) {
			setError("Vui lòng chọn kho trước khi tối ưu tuyến đường.");
			return;
		}

		setIsOptimizing(true);
		setError(null);
		setOptimizationResult(null);

		try {
			const result = await routingApi.optimize(selectedDepotId);

			setRoutes(result.routes);

			const totalKm = result.totalDistanceKm ?? result.routes.reduce((sum, r) => sum + (r.totalDistanceKm ?? 0), 0);
			const totalCost = result.totalCost ?? result.routes.reduce((sum, r) => sum + (r.totalCost ?? 0), 0);
			setOptimizationResult(`Tối ưu thành công! Tạo ${result.routes.length} tuyến đường, tổng ${totalKm.toFixed(2)} km, chi phí ${totalCost.toFixed(0)} VND`);
		} catch (err: any) {
			console.error("Optimization error:", err);
			setError(err.response?.data?.message || "Không thể tối ưu tuyến đường. Vui lòng thử lại.");
		} finally {
			setIsOptimizing(false);
		}
	};

	useEffect(() => {
		const fetchStatistics = async () => {
			try {
				const stats = await dashboardApi.getStatistics();
				setStatistics(stats);
			} catch (error) {
				console.error("Failed to fetch dashboard statistics:", error);
			} finally {
				setIsLoadingStats(false);
			}
		};

		const fetchDepots = async () => {
			try {
				const result = await depotApi.getDepots({ size: 100 });
				setDepots(result.data.filter((d) => d.isActive));
			} catch (error) {
				console.error("Failed to fetch depots:", error);
			} finally {
				setIsLoadingDepots(false);
			}
		};

		fetchStatistics();
		fetchDepots();
	}, []);

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="p-8 space-y-6">
					{/* Header */}
					<div className="flex justify-between items-center flex-wrap gap-4">
						<h1 className="text-3xl font-bold text-foreground">Tổng quan</h1>

						{/* Depot selector + Optimize Button */}
						<div className="flex items-center gap-3">
							<div className="flex items-center gap-2">
								<Warehouse className="w-4 h-4 text-muted-foreground" />
								<select
									id="depot-select"
									value={selectedDepotId ?? ""}
									onChange={(e) => setSelectedDepotId(e.target.value ? Number(e.target.value) : null)}
									disabled={isLoadingDepots}
									className="border border-input bg-background text-foreground rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50 disabled:cursor-not-allowed min-w-[200px]"
								>
									<option value="">-- Chọn kho --</option>
									{depots.map((depot) => (
										<option key={depot.id} value={depot.id}>
											{depot.name}
										</option>
									))}
								</select>
							</div>

							<button onClick={handleOptimizeRoutes} disabled={isOptimizing || selectedDepotId === null} className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
								<RouteIcon className="w-5 h-5" />
								{isOptimizing ? "Đang tối ưu..." : "Tối ưu tuyến đường"}
							</button>
						</div>
					</div>

					{/* Notification Messages */}
					{optimizationResult && (
						<div className="p-4 bg-green-50 border border-green-200 text-green-800 rounded-lg">
							<p className="text-sm font-medium">{optimizationResult}</p>
						</div>
					)}

					{error && (
						<div className="p-4 bg-red-50 border border-red-200 text-red-800 rounded-lg">
							<p className="text-sm font-medium">{error}</p>
						</div>
					)}

					{/* KPI Cards */}
					<div className="grid grid-cols-1 md:grid-cols-3 gap-4">
						{isLoadingStats ? (
							<>
								<div className="h-24 bg-gray-100 animate-pulse rounded-lg" />
								<div className="h-24 bg-gray-100 animate-pulse rounded-lg" />
								<div className="h-24 bg-gray-100 animate-pulse rounded-lg" />
							</>
						) : statistics ? (
							<>
								<KPICard label="Xe đang hoạt động" value={statistics.activeVehicles.toString()} icon={<Truck className="w-6 h-6" />} />
								<KPICard label="Đơn hàng đang giao" value={statistics.ordersInTransit.toString()} icon={<Package className="w-6 h-6" />} />
								<KPICard label="Tỷ lệ giao thành công" value={statistics.ordersInTransit + statistics.ordersDelivered > 0 ? `${((statistics.ordersDelivered / (statistics.ordersInTransit + statistics.ordersDelivered)) * 100).toFixed(1)}%` : "0%"} icon={<TrendingUp className="w-6 h-6" />} />
							</>
						) : (
							<>
								<KPICard label="Xe đang hoạt động" value="--" icon={<Truck className="w-6 h-6" />} />
								<KPICard label="Đơn hàng đang giao" value="--" icon={<Package className="w-6 h-6" />} />
								<KPICard label="Tỷ lệ hoàn thành" value="--" icon={<TrendingUp className="w-6 h-6" />} />
							</>
						)}
					</div>

					{/* Map and Shipments */}
					<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
						{/* Map */}
						<div className="lg:col-span-2">
							<LeafletMap routes={routes} />
						</div>

						{/* Shipment List */}
						<div>
							<ShipmentList shipments={shipments} />
						</div>
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
