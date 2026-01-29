import React from "react";
interface KPICardProps {
	label: string;
	value: string | number;
	icon?: React.ReactNode;
}

export function KPICard({ label, value, icon }: KPICardProps) {
	return (
		<div className="border-2 border-primary bg-card/50 rounded-xl p-6 backdrop-blur">
			<div className="flex items-start justify-between gap-4">
				<div>
					<p className="text-sm text-muted-foreground mb-2">{label}</p>
					<p className="text-3xl font-bold text-foreground">{value}</p>
				</div>
				{icon && <div className="text-primary">{icon}</div>}
			</div>
		</div>
	);
}
