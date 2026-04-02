"use client";

import { Search, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Depot } from "@/types/depot-types";

interface DriverFiltersProps {
	searchQuery: string;
	onSearchChange: (value: string) => void;
	depotId: string;
	onDepotChange: (value: string) => void;
	depots: Depot[];
	onClearFilters: () => void;
	children?: React.ReactNode;
}

export function DriverFilters({ searchQuery, onSearchChange, depotId, onDepotChange, depots, onClearFilters, children }: DriverFiltersProps) {
	const hasActiveFilters = searchQuery !== "" || depotId !== "all";

	return (
		<div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
			<div className="flex w-full flex-col gap-4 lg:flex-row lg:items-center">
				<div className="relative flex-1 max-w-md">
					<Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
					<Input type="text" placeholder="Tim theo ten, so GPLX hoac SDT..." value={searchQuery} onChange={(e) => onSearchChange(e.target.value)} className="pl-10" />
				</div>
				<Select value={depotId} onValueChange={onDepotChange}>
					<SelectTrigger className="w-full lg:w-[240px]">
						<SelectValue placeholder="Loc theo kho" />
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
			<div className="flex items-center gap-2">
				{hasActiveFilters && (
					<Button onClick={onClearFilters} variant="outline" size="sm" className="gap-2">
						<X className="w-4 h-4" />
						Xoa bo loc
					</Button>
				)}
				{children}
			</div>
		</div>
	);
}
