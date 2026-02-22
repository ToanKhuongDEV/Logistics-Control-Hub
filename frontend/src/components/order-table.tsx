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
}

export function OrderTable({ orders, onEdit, onDelete, isLoading = false }: OrderTableProps) {
	const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
	const formatNumber = (num: number) => {
		return new Intl.NumberFormat("vi-VN").format(num);
	};

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
				<p className="text-muted-foreground">Đang tải dữ liệu...</p>
			</div>
		);
	}

	if (orders.length === 0) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<Package className="w-12 h-12 text-muted-foreground mx-auto mb-4 opacity-50" />
				<p className="text-muted-foreground">Chưa có đơn hàng nào</p>
			</div>
		);
	}

	return (
		<>
			<div className="overflow-x-auto bg-card rounded-t-lg border border-b-0 border-border">
				<table className="w-full">
					<thead className="bg-muted border-b border-border">
						<tr>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Mã đơn hàng</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Khối lượng (kg)</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Điểm giao hàng</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Tài xế</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground min-w-[140px]">Trạng thái</th>
							<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Ngày tạo</th>
							<th className="px-6 py-4 text-right text-sm font-semibold text-foreground">Thao tác</th>
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

							return (
								<tr key={order.id} className="hover:bg-muted/50 transition-colors">
									<td className="px-6 py-4 text-sm font-medium text-foreground">{order.code}</td>
									<td className="px-6 py-4 text-sm text-foreground">{formatNumber(order.weightKg)}</td>
									<td className="px-6 py-4 text-sm text-foreground">{order.deliveryLocationName}</td>
									<td className="px-6 py-4 text-sm text-muted-foreground">{order.driverName || "-"}</td>
									<td className="px-6 py-4">
										<span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border ${statusConfig.color}`}>{statusConfig.label}</span>
									</td>
									<td className="px-6 py-4 text-sm text-foreground">{formatDate(order.createdAt)}</td>
									<td className="px-6 py-4 text-right">
										<div className="flex items-center justify-end gap-2">
											<Button size="sm" variant="outline" onClick={() => setSelectedOrder(order)} className="h-8 w-8 p-0 hover:bg-blue-500/20 hover:text-blue-500 text-blue-500">
												<Eye className="w-4 h-4" />
											</Button>
											<Button size="sm" variant="outline" onClick={() => onEdit(order)} className="h-8 w-8 p-0 hover:bg-accent hover:text-accent-foreground">
												<Edit2 className="w-4 h-4" />
											</Button>
											<Button
												size="sm"
												variant="outline"
												onClick={() => {
													if (window.confirm(`Bạn có chắc muốn xóa đơn hàng ${order.code}?`)) {
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
