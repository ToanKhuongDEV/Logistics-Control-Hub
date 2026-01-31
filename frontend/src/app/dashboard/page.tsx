"use client";

import { DashboardLayout } from "@/components/dashboard-layout";
import { KPICard } from "@/components/kpi-card";
import { LeafletMap } from "@/components/leaflet-map";
import { ShipmentList } from "@/components/shipment-list";
import { Truck, Package, TrendingUp } from "lucide-react";
import { ProtectedRoute } from "@/components/protected-route";

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
	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="p-8 space-y-6">
					{/* Header */}
					<div>
						<h1 className="text-3xl font-bold text-foreground">Tổng quan</h1>
					</div>

					{/* KPI Cards */}
					<div className="grid grid-cols-1 md:grid-cols-3 gap-4">
						<KPICard label="Xe đang hoạt động" value="1,245" icon={<Truck className="w-6 h-6" />} />
						<KPICard label="Đơn hàng đang giao" value="860" icon={<Package className="w-6 h-6" />} />
						<KPICard label="Tỷ lệ đúng hạn" value="94.5%" icon={<TrendingUp className="w-6 h-6" />} />
					</div>

					{/* Map and Shipments */}
					<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
						{/* Map */}
						<div className="lg:col-span-2">
							<LeafletMap />
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
