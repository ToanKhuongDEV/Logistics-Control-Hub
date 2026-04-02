"use client";

import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { VehicleStatus } from "@/types/vehicle-types";
import { Depot } from "@/types/depot-types";

interface FleetFiltersProps {
	searchQuery: string;
	onSearchChange: (query: string) => void;
	status: VehicleStatus | "all";
	onStatusChange: (status: VehicleStatus | "all") => void;
	depotId: string;
	onDepotChange: (depotId: string) => void;
	depots: Depot[];
}

export function FleetFilters({ searchQuery, onSearchChange, status, onStatusChange, depotId, onDepotChange, depots }: FleetFiltersProps) {
	return (
		<div className="flex flex-col lg:flex-row gap-4 items-stretch lg:items-center">
			<div className="relative flex-1">
				<Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
				<Input placeholder="Tim kiem ma xe..." value={searchQuery} onChange={(e) => onSearchChange(e.target.value)} className="pl-10 bg-card border-border" />
			</div>

			<Select value={status} onValueChange={(value) => onStatusChange(value as VehicleStatus | "all")}>
				<SelectTrigger className="w-full lg:w-[200px] bg-card border-border">
					<SelectValue placeholder="Trang thai" />
				</SelectTrigger>
				<SelectContent>
					<SelectItem value="all">Tat ca trang thai</SelectItem>
					<SelectItem value={VehicleStatus.ACTIVE}>Dang hoat dong</SelectItem>
					<SelectItem value={VehicleStatus.MAINTENANCE}>Bao tri</SelectItem>
					<SelectItem value={VehicleStatus.IDLE}>Nhan roi</SelectItem>
				</SelectContent>
			</Select>

			<Select value={depotId} onValueChange={onDepotChange}>
				<SelectTrigger className="w-full lg:w-[220px] bg-card border-border">
					<SelectValue placeholder="Kho truc thuoc" />
				</SelectTrigger>
				<SelectContent>
					<SelectItem value="all">Tat ca kho</SelectItem>
					{depots.map((depot) => (
						<SelectItem key={depot.id} value={String(depot.id)}>
							{depot.name}
						</SelectItem>
					))}
				</SelectContent>
			</Select>
		</div>
	);
}
