import { cn } from "@/lib/utils";
import { Order } from "@/types/order-types";

interface ShipmentListProps {
	shipments: Order[];
	isLoading?: boolean;
}

export function ShipmentList({ shipments, isLoading }: ShipmentListProps) {
	return (
		<div className="bg-card border border-border rounded-xl p-6">
			<h3 className="text-lg font-semibold text-foreground mb-4">Đơn hàng đang giao</h3>

			{isLoading ? (
				<div className="space-y-3">
					{[1, 2, 3].map((i) => (
						<div key={i} className="h-16 w-full bg-muted animate-pulse rounded-lg" />
					))}
				</div>
			) : shipments.length === 0 ? (
				<div className="flex flex-col items-center justify-center py-8 text-muted-foreground">
					<p className="text-sm italic">Không có đơn hàng nào đang giao</p>
				</div>
			) : (
				<div className="space-y-3 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
					{shipments.map((order) => {
						return (
							<div key={order.id} className="flex items-center justify-between p-3 bg-card/50 border border-border/50 rounded-lg hover:border-border transition-colors">
								<div className="flex-1 min-w-0">
									<p className="text-sm font-bold text-foreground truncate">{order.code}</p>
									<p className="text-xs text-muted-foreground truncate">{order.deliveryLocationName}</p>
								</div>
								<div className="flex items-center gap-2 ml-2 shrink-0">
									<div className="text-right">
										<p className="text-[10px] text-muted-foreground leading-tight">Ngày tạo:</p>
										<p className="text-xs font-medium text-foreground">{new Date(order.createdAt).toLocaleDateString("vi-VN")}</p>
									</div>
									<span className="px-2 py-1 rounded-full text-[10px] font-bold uppercase bg-primary/20 text-primary border border-primary/30">Đang giao</span>
								</div>
							</div>
						);
					})}
				</div>
			)}
		</div>
	);
}
