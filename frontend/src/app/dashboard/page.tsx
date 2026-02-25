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
import { orderApi } from "@/lib/order-api";
import { DashboardStatistics } from "@/types/dashboard-types";
import { Depot } from "@/types/depot-types";
import { Order, OrderStatus } from "@/types/order-types";
import { toast } from "sonner";

const LeafletMap = dynamic(() => import("@/components/leaflet-map").then((mod) => mod.LeafletMap), {
	ssr: false,
	loading: () => <p className="h-[400px] w-full flex items-center justify-center bg-gray-100 rounded-lg">Đang tải bản đồ...</p>,
});

export default function DashboardPage() {
	const [routes, setRoutes] = useState<Route[]>([]);
	const [isOptimizing, setIsOptimizing] = useState(false);
	const [statistics, setStatistics] = useState<DashboardStatistics | null>(null);
	const [isLoadingStats, setIsLoadingStats] = useState(true);
	const [depots, setDepots] = useState<Depot[]>([]);
	const [selectedDepotId, setSelectedDepotId] = useState<number | null>(null);
	const [isLoadingDepots, setIsLoadingDepots] = useState(true);
	const [inTransitOrders, setInTransitOrders] = useState<Order[]>([]);
	const [isLoadingOrders, setIsLoadingOrders] = useState(false);

	const handleOptimizeRoutes = async () => {
		if (selectedDepotId === null) {
			toast.error("Vui lòng chọn kho trước khi tối ưu tuyến đường.");
			return;
		}

		setIsOptimizing(true);

		try {
			const result = await routingApi.optimize(selectedDepotId);

			setRoutes(result.routes);

			const totalKm = result.totalDistanceKm ?? result.routes.reduce((sum, r) => sum + (r.totalDistanceKm ?? 0), 0);
			const totalCost = result.totalCost ?? result.routes.reduce((sum, r) => sum + (r.totalCost ?? 0), 0);
			toast.success(`Tối ưu thành công! Tạo ${result.routes.length} tuyến đường, tổng ${totalKm.toFixed(2)} km, chi phí ${totalCost.toFixed(0)} VND`);

			// Refresh stats and orders after optimization
			fetchStatisticsAndOrders();
		} catch (err: any) {
			console.error("Optimization error:", err);
			toast.error(err.response?.data?.message || "Không thể tối ưu tuyến đường. Vui lòng thử lại.");
		} finally {
			setIsOptimizing(false);
		}
	};

	const fetchStatisticsAndOrders = async () => {
		setIsLoadingStats(true);
		setIsLoadingOrders(true);
		try {
			const stats = await dashboardApi.getStatistics(selectedDepotId);
			setStatistics(stats);

			const ordersResult = await orderApi.getOrders({
				status: OrderStatus.IN_TRANSIT,
				depotId: selectedDepotId,
				size: 10,
			});
			setInTransitOrders(ordersResult.data);

			// Fetch latest routing run for this depot to show on map
			if (selectedDepotId) {
				const latestRun = await routingApi.getLatestRun(selectedDepotId);
				if (latestRun && latestRun.routes) {
					setRoutes(latestRun.routes);
				} else {
					setRoutes([]);
				}
			} else {
				setRoutes([]);
			}
		} catch (error) {
			console.error("Failed to fetch dashboard data:", error);
		} finally {
			setIsLoadingStats(false);
			setIsLoadingOrders(false);
		}
	};

	useEffect(() => {
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

		fetchDepots();
	}, []);

	useEffect(() => {
		fetchStatisticsAndOrders();
	}, [selectedDepotId]);

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
							<ShipmentList shipments={inTransitOrders} isLoading={isLoadingOrders} />
						</div>
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
