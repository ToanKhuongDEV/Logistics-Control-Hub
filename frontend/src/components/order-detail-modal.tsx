"use client";

import { X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Order, OrderStatus } from "@/types/order-types";

interface OrderDetailModalProps {
	order: Order;
	onClose: () => void;
}

export function OrderDetailModal({ order, onClose }: OrderDetailModalProps) {
	const getStatusConfig = (status: OrderStatus) => {
		if (status === OrderStatus.CREATED) return { label: "Đã tạo", color: "bg-blue-500/10 text-blue-600 border-blue-500/20" };
		if (status === OrderStatus.IN_TRANSIT) return { label: "Đang giao", color: "bg-purple-500/10 text-purple-600 border-purple-500/20" };
		if (status === OrderStatus.DELIVERED) return { label: "Đã giao", color: "bg-green-500/10 text-green-600 border-green-500/20" };
		if (status === OrderStatus.CANCELLED) return { label: "Đã hủy", color: "bg-red-500/10 text-red-600 border-red-500/20" };
		return { label: status, color: "bg-gray-500/10 text-gray-600 border-gray-500/20" };
	};

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

	const statusConfig = getStatusConfig(order.status);

	return (
		<div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={onClose}>
			<div className="bg-card rounded-lg shadow-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
				<div className="flex items-center justify-between p-6 border-b border-border sticky top-0 bg-card z-10">
					<h2 className="text-xl font-semibold text-foreground">Chi tiết đơn hàng</h2>
					<button onClick={onClose} className="text-muted-foreground hover:text-foreground transition-colors">
						<X className="w-5 h-5" />
					</button>
				</div>

				<div className="p-6 space-y-6">
					{/* Order Code */}
					<div className="flex items-center justify-between pb-4 border-b border-border">
						<div>
							<p className="text-sm text-muted-foreground mb-1">Mã đơn hàng</p>
							<p className="text-2xl font-bold text-foreground">{order.code}</p>
						</div>
						<span className={`inline-flex items-center px-3 py-1.5 rounded-full text-sm font-medium border ${statusConfig.color}`}>{statusConfig.label}</span>
					</div>

					{/* Order Information */}
					<div className="grid grid-cols-1 md:grid-cols-2 gap-6">
						<div className="space-y-4">
							<h3 className="text-sm font-semibold text-foreground uppercase tracking-wide">Thông tin hàng hóa</h3>

							<div>
								<p className="text-sm text-muted-foreground mb-1">Khối lượng</p>
								<p className="text-lg font-medium text-foreground">{formatNumber(order.weightKg)} kg</p>
							</div>

							<div>
								<p className="text-sm text-muted-foreground mb-1">Thể tích</p>
								<p className="text-lg font-medium text-foreground">{Number(order.volumeM3).toFixed(2)} m³</p>
							</div>
						</div>

						<div className="space-y-4">
							<h3 className="text-sm font-semibold text-foreground uppercase tracking-wide">Thông tin vận chuyển</h3>

							<div>
								<p className="text-sm text-muted-foreground mb-1">Tài xế</p>
								<p className="text-lg font-medium text-foreground">{order.driverName || "-"}</p>
							</div>

							<div>
								<p className="text-sm text-muted-foreground mb-1">Ngày tạo</p>
								<p className="text-lg font-medium text-foreground">{formatDate(order.createdAt)}</p>
							</div>
						</div>
					</div>

					{/* Delivery Location */}
					<div className="pt-4 border-t border-border">
						<h3 className="text-sm font-semibold text-foreground uppercase tracking-wide mb-3">Địa điểm giao hàng</h3>
						<div className="bg-muted/50 rounded-lg p-4">
							<p className="text-base text-foreground">{order.deliveryLocationName}</p>
						</div>
					</div>
				</div>

				<div className="flex gap-3 p-6 border-t border-border bg-muted/30">
					<Button type="button" variant="outline" onClick={onClose} className="flex-1 bg-transparent">
						Đóng
					</Button>
				</div>
			</div>
		</div>
	);
}
