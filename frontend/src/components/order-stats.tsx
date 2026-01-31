"use client";

import { Package, Clock, Truck } from "lucide-react";

interface OrderStatsProps {
	totalOrders: number;
	pendingOrders: number;
	inTransitOrders: number;
}

export function OrderStats({ totalOrders, pendingOrders, inTransitOrders }: OrderStatsProps) {
	const stats = [
		{
			label: "TỔNG ĐƠN HÀNG CHƯA GIAO",
			value: totalOrders,
			icon: Package,
			color: "text-primary",
		},
		{
			label: "CHỜ XỬ LÝ",
			value: pendingOrders,
			icon: Clock,
			color: "text-yellow-500",
		},
		{
			label: "ĐANG VẬN CHUYỂN",
			value: inTransitOrders,
			icon: Truck,
			color: "text-blue-500",
		},
	];

	return (
		<div className="grid grid-cols-1 md:grid-cols-3 gap-3">
			{stats.map((stat, index) => {
				const Icon = stat.icon;
				return (
					<div key={index} className="bg-card border border-border rounded-lg p-3 transition-all hover:shadow-md">
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
