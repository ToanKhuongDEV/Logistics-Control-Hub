"use client";

import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { OrderStatus } from "@/types/order-types";

interface OrderFiltersProps {
	searchQuery: string;
	onSearchChange: (query: string) => void;
	status: OrderStatus | "all";
	onStatusChange: (status: OrderStatus | "all") => void;
}

export function OrderFilters({ searchQuery, onSearchChange, status, onStatusChange }: OrderFiltersProps) {
	return (
		<div className="flex flex-col sm:flex-row gap-4 items-stretch sm:items-center">
			{/* Search */}
			<div className="relative flex-1">
				<Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
				<Input placeholder="Tìm kiếm mã đơn hàng..." value={searchQuery} onChange={(e) => onSearchChange(e.target.value)} className="pl-10 bg-card border-border" />
			</div>

			{/* Status Filter */}
			<Select value={status} onValueChange={(value) => onStatusChange(value as OrderStatus | "all")}>
				<SelectTrigger className="w-[200px] bg-card border-border">
					<SelectValue placeholder="Trạng thái" />
				</SelectTrigger>
				<SelectContent>
					<SelectItem value="all">Tất cả trạng thái</SelectItem>
					<SelectItem value={OrderStatus.CREATED}>Đã tạo</SelectItem>
					<SelectItem value={OrderStatus.IN_TRANSIT}>Đang giao</SelectItem>
					<SelectItem value={OrderStatus.DELIVERED}>Đã giao</SelectItem>
					<SelectItem value={OrderStatus.CANCELLED}>Đã hủy</SelectItem>
				</SelectContent>
			</Select>
		</div>
	);
}
