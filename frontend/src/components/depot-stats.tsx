"use client";

import { Warehouse, CheckCircle, XCircle } from "lucide-react";
import { DepotStatistics } from "@/types/depot-types";

interface DepotStatsProps {
	statistics: DepotStatistics | null;
}

export function DepotStats({ statistics }: DepotStatsProps) {
	const stats = [
		{
			label: "Tổng số kho",
			value: statistics?.total || 0,
			icon: Warehouse,
			color: "text-blue-600 dark:text-blue-300",
			bgColor: "bg-blue-50 dark:bg-blue-500/10",
		},
		{
			label: "Kho hoạt động",
			value: statistics?.active || 0,
			icon: CheckCircle,
			color: "text-green-600 dark:text-green-300",
			bgColor: "bg-green-50 dark:bg-green-500/10",
		},
		{
			label: "Kho đóng cửa",
			value: statistics?.inactive || 0,
			icon: XCircle,
			color: "text-red-600 dark:text-red-300",
			bgColor: "bg-red-50 dark:bg-red-500/10",
		},
	];

	return (
		<div className="grid grid-cols-1 md:grid-cols-3 gap-6">
			{stats.map((stat, index) => {
				const Icon = stat.icon;
				return (
					<div key={index} className="bg-card rounded-lg border border-border p-6 hover:shadow-md transition-shadow">
						<div className="flex items-center justify-between">
							<div>
								<p className="text-sm font-medium text-muted-foreground">{stat.label}</p>
								<p className="text-3xl font-bold text-foreground mt-2">{stat.value}</p>
							</div>
							<div className={`${stat.bgColor} ${stat.color} p-3 rounded-lg`}>
								<Icon className="w-6 h-6" />
							</div>
						</div>
					</div>
				);
			})}
		</div>
	);
}
