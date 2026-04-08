"use client";

import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { OrderStatus } from "@/types/order-types";
import { Depot } from "@/types/depot-types";

interface OrderFiltersProps {
	searchQuery: string;
	onSearchChange: (query: string) => void;
	status: OrderStatus | "all";
	onStatusChange: (status: OrderStatus | "all") => void;
	depotId: string;
	onDepotChange: (depotId: string) => void;
	sortBy: string;
	onSortByChange: (sortBy: string) => void;
	depots: Depot[];
}

export function OrderFilters({
	searchQuery,
	onSearchChange,
	status,
	onStatusChange,
	depotId,
	onDepotChange,
	sortBy,
	onSortByChange,
	depots,
}: OrderFiltersProps) {
	return (
		<div className="flex flex-col gap-4">
			<div className="flex flex-col lg:flex-row gap-4 items-stretch lg:items-center">
				<div className="relative flex-1">
					<Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
					<Input placeholder="Tìm kiếm mã đơn hoặc tên kho..." value={searchQuery} onChange={(e) => onSearchChange(e.target.value)} className="pl-10 bg-card border-border" />
				</div>

				<Select value={status} onValueChange={(value) => onStatusChange(value as OrderStatus | "all")}>
					<SelectTrigger className="w-full lg:w-[220px] bg-card border-border">
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

				<Select value={depotId} onValueChange={onDepotChange}>
					<SelectTrigger className="w-full lg:w-[220px] bg-card border-border">
						<SelectValue placeholder="Kho" />
					</SelectTrigger>
					<SelectContent>
						<SelectItem value="all">Tất cả kho</SelectItem>
						{depots.map((depot) => (
							<SelectItem key={depot.id} value={depot.id.toString()}>
								{depot.name}
							</SelectItem>
						))}
					</SelectContent>
				</Select>

				<Select value={sortBy} onValueChange={onSortByChange}>
					<SelectTrigger className="w-full lg:w-[240px] bg-card border-border">
						<SelectValue placeholder="Sắp xếp" />
					</SelectTrigger>
					<SelectContent>
						<SelectItem value="createdAt,desc">Mới nhất</SelectItem>
						<SelectItem value="createdAt,asc">Cũ nhất</SelectItem>
						<SelectItem value="code,asc">Mã đơn A-Z</SelectItem>
						<SelectItem value="code,desc">Mã đơn Z-A</SelectItem>
						<SelectItem value="weightKg,asc">Khối lượng tăng dần</SelectItem>
						<SelectItem value="depotName,asc">Kho A-Z</SelectItem>
					</SelectContent>
				</Select>
			</div>
		</div>
	);
}
