"use client";

import { UserCheck, Users, UserX } from "lucide-react";
import { DriverStatistics } from "@/types/driver-types";

interface DriverStatsProps {
	statistics: DriverStatistics | null;
}

export function DriverStats({ statistics }: DriverStatsProps) {
	if (!statistics) {
		return null;
	}

	const formatNumber = (num: number) => {
		return new Intl.NumberFormat("vi-VN").format(num);
	};

	const stats = [
		{
			title: "Tổng số tài xế",
			value: formatNumber(statistics.total),
			icon: Users,
			color: "bg-blue-500/10 text-blue-500",
		},
		{
			title: "Đã được phân công",
			value: formatNumber(statistics.assigned),
			icon: UserCheck,
			color: "bg-green-500/10 text-green-500",
		},
		{
			title: "Đang rảnh",
			value: formatNumber(statistics.available),
			icon: UserX,
			color: "bg-yellow-500/10 text-yellow-500",
		},
	];

	return (
		<div className="grid grid-cols-1 md:grid-cols-3 gap-6">
			{stats.map((stat) => {
				const Icon = stat.icon;
				return (
					<div key={stat.title} className="bg-card rounded-lg border border-border p-6">
						<div className="flex items-center justify-between mb-3">
							<p className="text-sm font-medium text-muted-foreground">{stat.title}</p>
							<div className={`p-2 rounded-lg ${stat.color}`}>
								<Icon className="w-5 h-5" />
							</div>
						</div>
						<p className="text-3xl font-bold text-foreground">{stat.value}</p>
					</div>
				);
			})}
		</div>
	);
}
