"use client";

import { Search, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface DriverFiltersProps {
	searchQuery: string;
	onSearchChange: (value: string) => void;
	onClearFilters: () => void;
	children?: React.ReactNode;
}

export function DriverFilters({ searchQuery, onSearchChange, onClearFilters, children }: DriverFiltersProps) {
	const hasActiveFilters = searchQuery !== "";

	return (
		<div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
			<div className="flex-1 max-w-md relative">
				<Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
				<Input type="text" placeholder="Tìm theo tên, số GPLX hoặc SĐT..." value={searchQuery} onChange={(e) => onSearchChange(e.target.value)} className="pl-10" />
			</div>
			<div className="flex items-center gap-2">
				{hasActiveFilters && (
					<Button onClick={onClearFilters} variant="outline" size="sm" className="gap-2">
						<X className="w-4 h-4" />
						Xóa bộ lọc
					</Button>
				)}
				{children}
			</div>
		</div>
	);
}
