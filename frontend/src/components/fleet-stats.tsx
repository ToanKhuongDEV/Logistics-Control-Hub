"use client";

import { Truck, CheckCircle, Wrench, Clock, DollarSign, Weight, Box } from "lucide-react";

interface FleetStatsProps {
	totalVehicles: number;
	activeVehicles: number;
	maintenanceVehicles: number;
	idleVehicles: number;
	averageCostPerKm?: number;
	totalCapacityKg?: number;
	totalCapacityM3?: number;
}

export function FleetStats({ totalVehicles, activeVehicles, maintenanceVehicles, idleVehicles, averageCostPerKm, totalCapacityKg, totalCapacityM3 }: FleetStatsProps) {
	const formatNumber = (num: number) => {
		return new Intl.NumberFormat("vi-VN").format(num);
	};

	const stats = [
		{
			label: "TỔNG SỐ XE",
			value: totalVehicles,
			icon: Truck,
			color: "text-foreground",
			bgColor: "bg-card",
		},
		{
			label: "ĐANG HOẠT ĐỘNG",
			value: activeVehicles,
			icon: CheckCircle,
			color: "text-green-500",
			bgColor: "bg-card",
		},
		{
			label: "BẢO TRÌ",
			value: maintenanceVehicles,
			icon: Wrench,
			color: "text-orange-500",
			bgColor: "bg-card",
		},
		{
			label: "NHÀN RỖI",
			value: idleVehicles,
			icon: Clock,
			color: "text-muted-foreground",
			bgColor: "bg-card",
		},
	];

	// Add additional stats if available
	const additionalStats = [];

	if (averageCostPerKm !== undefined && averageCostPerKm !== null) {
		additionalStats.push({
			label: "CHI PHÍ TB/KM",
			value: `${formatNumber(Number(averageCostPerKm))} ₫`,
			icon: DollarSign,
			color: "text-blue-500",
			bgColor: "bg-card",
		});
	}

	if (totalCapacityKg !== undefined && totalCapacityKg !== null) {
		additionalStats.push({
			label: "TỔNG TẢI TRỌNG",
			value: `${formatNumber(totalCapacityKg)} kg`,
			icon: Weight,
			color: "text-purple-500",
			bgColor: "bg-card",
		});
	}

	if (totalCapacityM3 !== undefined && totalCapacityM3 !== null) {
		additionalStats.push({
			label: "TỔNG THỂ TÍCH",
			value: `${Number(totalCapacityM3).toFixed(2)} m³`,
			icon: Box,
			color: "text-cyan-500",
			bgColor: "bg-card",
		});
	}

	const allStats = [...stats, ...additionalStats];

	return (
		<div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-3">
			{allStats.map((stat, index) => {
				const Icon = stat.icon;
				return (
					<div key={index} className={`${stat.bgColor} border border-border rounded-lg p-3 transition-all hover:shadow-md`}>
						<div className="flex items-start justify-between mb-1">
							<p className="text-[10px] font-medium text-muted-foreground uppercase tracking-wide truncate" title={stat.label}>
								{stat.label}
							</p>
							<Icon className={`w-4 h-4 ${stat.color} opacity-60 shrink-0 ml-2`} />
						</div>
						<p className={`text-xl font-bold ${stat.color} truncate`} title={stat.value.toString()}>
							{stat.value}
						</p>
					</div>
				);
			})}
		</div>
	);
}
