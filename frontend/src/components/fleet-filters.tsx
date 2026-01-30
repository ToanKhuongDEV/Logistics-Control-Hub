"use client";

import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { VehicleStatus } from "@/types/vehicle-types";

interface FleetFiltersProps {
	searchQuery: string;
	onSearchChange: (query: string) => void;
	status: VehicleStatus | "all";
	onStatusChange: (status: VehicleStatus | "all") => void;
}

export function FleetFilters({ searchQuery, onSearchChange, status, onStatusChange }: FleetFiltersProps) {
	return (
		<div className="flex flex-col sm:flex-row gap-4 items-stretch sm:items-center">
			{/* Search */}
			<div className="relative flex-1">
				<Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
				<Input placeholder="Tìm kiếm xe hoặc tài xế..." value={searchQuery} onChange={(e) => onSearchChange(e.target.value)} className="pl-10 bg-card border-border" />
			</div>

			{/* Status Filter */}
			<Select value={status} onValueChange={(value) => onStatusChange(value as VehicleStatus | "all")}>
				<SelectTrigger className="w-[180px] bg-card border-border">
					<SelectValue placeholder="Status" />
				</SelectTrigger>
				<SelectContent>
					<SelectItem value="all">Tất cả trạng thái</SelectItem>
					<SelectItem value={VehicleStatus.ACTIVE}>Đang hoạt động</SelectItem>
					<SelectItem value={VehicleStatus.MAINTENANCE}>Bảo trì</SelectItem>
					<SelectItem value={VehicleStatus.IDLE}>Nhàn rỗi</SelectItem>
				</SelectContent>
			</Select>
		</div>
	);
}
