"use client";

import { useState } from "react";
import { Trash2, Edit2, Package, Loader2, Eye } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Order, OrderStatus } from "@/types/order-types";
import { OrderDetailModal } from "./order-detail-modal";

interface OrderTableProps {
	orders: Order[];
	onEdit: (order: Order) => void;
	onDelete: (id: number) => void;
	isLoading?: boolean;
	selectedOrderIds: number[];
	onToggleOrderSelection: (orderId: number) => void;
}

export function OrderTable({
	orders,
	onEdit,
	onDelete,
	isLoading = false,
	selectedOrderIds,
	onToggleOrderSelection,
}: OrderTableProps) {
	const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

	const formatNumber = (num: number) => new Intl.NumberFormat("vi-VN").format(num);

	const formatDate = (dateString: string) => {
		const date = new Date(dateString);
		return new Intl.DateTimeFormat("vi-VN", {
			year: "numeric",
			month: "2-digit",
			day: "2-digit",
			hour: "2-digit",
			minute: "2-digit",
		}).format(date);
	};

	if (isLoading) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<Loader2 className="w-12 h-12 text-muted-foreground mx-auto mb-4 animate-spin" />
				<p className="text-muted-foreground">Dang tai du lieu...</p>
			</div>
		);
	}

	if (orders.length === 0) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<Package className="w-12 h-12 text-muted-foreground mx-auto mb-4 opacity-50" />
				<p className="text-muted-foreground">Chua co don hang nao</p>
			</div>
		);
	}

	return (
		<>
			<div className="overflow-x-auto bg-card rounded-t-lg border border-b-0 border-border">
				<table className="w-full">
					<thead className="bg-muted border-b border-border">
						<tr>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Ma don hang</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Khoi luong (kg)</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Diem giao hang</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Kho bai</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Tai xe</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground min-w-[140px]">Trang thai</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Ngay tao</th>
							<th className="px-6 py-4 text-right text-sm font-semibold text-foreground">Thao tac</th>
						</tr>
					</thead>
					<tbody className="divide-y divide-border">
						{orders.map((order) => {
							const getStatusConfig = (status: OrderStatus) => {
								if (status === OrderStatus.CREATED) return { label: "Da tao", color: "bg-blue-500/10 text-blue-600 border-blue-500/20" };
								if (status === OrderStatus.IN_TRANSIT) return { label: "Dang giao", color: "bg-purple-500/10 text-purple-600 border-purple-500/20" };
								if (status === OrderStatus.DELIVERED) return { label: "Da giao", color: "bg-green-500/10 text-green-600 border-green-500/20" };
								if (status === OrderStatus.CANCELLED) return { label: "Da huy", color: "bg-red-500/10 text-red-600 border-red-500/20" };
								return { label: status, color: "bg-gray-500/10 text-gray-600 border-gray-500/20" };
							};

							const statusConfig = getStatusConfig(order.status);
							const isSelected = selectedOrderIds.includes(order.id);

							return (
								<tr
									key={order.id}
									className={`transition-colors cursor-pointer ${isSelected ? "bg-sky-500/10 hover:bg-sky-500/15" : "hover:bg-muted/50"}`}
									onClick={() => onToggleOrderSelection(order.id)}
								>
									<td className="px-6 py-4 text-sm font-medium text-foreground">{order.code}</td>
									<td className="px-6 py-4 text-sm text-foreground">{formatNumber(order.weightKg)}</td>
									<td className="px-6 py-4 text-sm text-foreground">{order.deliveryLocationName}</td>
									<td className="px-6 py-4 text-sm text-muted-foreground">{order.depotName || "-"}</td>
									<td className="px-6 py-4 text-sm text-muted-foreground">{order.driverName || "-"}</td>
									<td className="px-6 py-4">
										<span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border ${statusConfig.color}`}>{statusConfig.label}</span>
									</td>
									<td className="px-6 py-4 text-sm text-foreground">{formatDate(order.createdAt)}</td>
									<td className="px-6 py-4 text-right">
										<div className="flex items-center justify-end gap-2">
											<Button
												size="sm"
												variant="outline"
												onClick={(e) => {
													e.stopPropagation();
													setSelectedOrder(order);
												}}
												className="h-8 w-8 p-0 hover:bg-blue-500/20 hover:text-blue-500 text-blue-500"
											>
												<Eye className="w-4 h-4" />
											</Button>
											<Button
												size="sm"
												variant="outline"
												onClick={(e) => {
													e.stopPropagation();
													onEdit(order);
												}}
												className="h-8 w-8 p-0 hover:bg-accent hover:text-accent-foreground"
											>
												<Edit2 className="w-4 h-4" />
											</Button>
											<Button
												size="sm"
												variant="outline"
												onClick={(e) => {
													e.stopPropagation();
													if (window.confirm(`Ban co chac muon xoa don hang ${order.code}?`)) {
														onDelete(order.id);
													}
												}}
												className="h-8 w-8 p-0 hover:bg-red-500/20 hover:text-red-500 text-red-500"
											>
												<Trash2 className="w-4 h-4" />
											</Button>
										</div>
									</td>
								</tr>
							);
						})}
					</tbody>
				</table>
			</div>

			{selectedOrder && <OrderDetailModal order={selectedOrder} onClose={() => setSelectedOrder(null)} />}
		</>
	);
}
