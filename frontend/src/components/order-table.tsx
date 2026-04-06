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
	canManage?: boolean;
	selectedOrderIds: number[];
	onToggleOrderSelection: (orderId: number) => void;
}

export function OrderTable({
	orders,
	onEdit,
	onDelete,
	isLoading = false,
	canManage = true,
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
			<div className="rounded-lg border border-border bg-card p-12 text-center">
				<Loader2 className="mx-auto mb-4 h-12 w-12 animate-spin text-muted-foreground" />
				<p className="text-muted-foreground">Đang tải dữ liệu...</p>
			</div>
		);
	}

	if (orders.length === 0) {
		return (
			<div className="rounded-lg border border-border bg-card p-12 text-center">
				<Package className="mx-auto mb-4 h-12 w-12 text-muted-foreground opacity-50" />
				<p className="text-muted-foreground">Chưa có đơn hàng nào</p>
			</div>
		);
	}

	return (
		<>
			<div className="overflow-x-auto rounded-t-lg border border-b-0 border-border bg-card">
				<table className="w-full">
					<thead className="border-b border-border bg-muted">
						<tr>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Mã đơn hàng</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Khối lượng (kg)</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Điểm giao hàng</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Kho bãi</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Tài xế</th>
							<th className="min-w-[140px] px-6 py-4 text-left text-sm font-semibold text-foreground">Trạng thái</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Ngày tạo</th>
							{canManage && <th className="px-6 py-4 text-right text-sm font-semibold text-foreground">Thao tác</th>}
						</tr>
					</thead>
					<tbody className="divide-y divide-border">
						{orders.map((order) => {
							const getStatusConfig = (status: OrderStatus) => {
								if (status === OrderStatus.CREATED) return { label: "Đã tạo", color: "bg-blue-500/10 text-blue-600 border-blue-500/20" };
								if (status === OrderStatus.IN_TRANSIT) return { label: "Đang giao", color: "bg-purple-500/10 text-purple-600 border-purple-500/20" };
								if (status === OrderStatus.DELIVERED) return { label: "Đã giao", color: "bg-green-500/10 text-green-600 border-green-500/20" };
								if (status === OrderStatus.CANCELLED) return { label: "Đã hủy", color: "bg-red-500/10 text-red-600 border-red-500/20" };
								return { label: status, color: "bg-gray-500/10 text-gray-600 border-gray-500/20" };
							};

							const statusConfig = getStatusConfig(order.status);
							const isSelected = selectedOrderIds.includes(order.id);

							return (
								<tr key={order.id} className={`cursor-pointer transition-colors ${isSelected ? "bg-sky-500/10 hover:bg-sky-500/15" : "hover:bg-muted/50"}`} onClick={() => onToggleOrderSelection(order.id)}>
									<td className="px-6 py-4 text-sm font-medium text-foreground">{order.code}</td>
									<td className="px-6 py-4 text-sm text-foreground">{formatNumber(order.weightKg)}</td>
									<td className="px-6 py-4 text-sm text-foreground">{order.deliveryLocationName}</td>
									<td className="px-6 py-4 text-sm text-muted-foreground">{order.depotName || "-"}</td>
									<td className="px-6 py-4 text-sm text-muted-foreground">{order.driverName || "-"}</td>
									<td className="px-6 py-4">
										<span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-medium ${statusConfig.color}`}>{statusConfig.label}</span>
									</td>
									<td className="px-6 py-4 text-sm text-foreground">{formatDate(order.createdAt)}</td>
									{canManage && (
										<td className="px-6 py-4 text-right">
											<div className="flex items-center justify-end gap-2">
												<Button
													size="sm"
													variant="outline"
													onClick={(e) => {
														e.stopPropagation();
														setSelectedOrder(order);
													}}
													className="h-8 w-8 p-0 text-blue-500 hover:bg-blue-500/20 hover:text-blue-500"
												>
													<Eye className="h-4 w-4" />
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
													<Edit2 className="h-4 w-4" />
												</Button>
												<Button
													size="sm"
													variant="outline"
													onClick={(e) => {
														e.stopPropagation();
														if (window.confirm(`Bạn có chắc muốn xóa đơn hàng ${order.code}?`)) {
															onDelete(order.id);
														}
													}}
													className="h-8 w-8 p-0 text-red-500 hover:bg-red-500/20 hover:text-red-500"
												>
													<Trash2 className="h-4 w-4" />
												</Button>
											</div>
										</td>
									)}
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
