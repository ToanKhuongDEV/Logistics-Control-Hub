import { cn } from "@/lib/utils";

interface Shipment {
	id: string;
	location: string;
	eta: string;
	status: "on-time" | "delayed" | "in-transit";
}

interface ShipmentListProps {
	shipments: Shipment[];
}

const statusConfig = {
	"on-time": {
		label: "Đúng giờ",
		className: "bg-emerald-500/20 text-emerald-400 border border-emerald-500/30",
	},
	delayed: {
		label: "Trễ hạn",
		className: "bg-red-500/20 text-red-400 border border-red-500/30",
	},
	"in-transit": {
		label: "Đang giao",
		className: "bg-accent/20 text-primary border border-primary/50",
	},
};

export function ShipmentList({ shipments }: ShipmentListProps) {
	return (
		<div className="bg-card border border-border rounded-xl p-6">
			<h3 className="text-lg font-semibold text-foreground mb-4">Đơn hàng đang giao</h3>
			<div className="space-y-3 max-h-96 overflow-y-auto">
				{shipments.map((shipment) => {
					const config = statusConfig[shipment.status];
					return (
						<div key={shipment.id} className="flex items-center justify-between p-3 bg-card/50 border border-border/50 rounded-lg hover:border-border transition-colors">
							<div className="flex-1 min-w-0">
								<p className="text-sm font-medium text-foreground truncate">{shipment.id}</p>
								<p className="text-xs text-muted-foreground">{shipment.location}</p>
							</div>
							<div className="flex items-center gap-2 ml-2 flex-shrink-0">
								<div className="text-right">
									<p className="text-xs font-medium text-foreground">ETA: {shipment.eta}</p>
								</div>
								<span className={cn("px-2 py-1 rounded-full text-xs font-semibold whitespace-nowrap", config.className)}>{config.label}</span>
							</div>
						</div>
					);
				})}
			</div>
		</div>
	);
}
