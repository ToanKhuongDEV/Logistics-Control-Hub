"use client";

import { Search, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface DepotFiltersProps {
	searchQuery: string;
	onSearchChange: (value: string) => void;
	onClearFilters: () => void;
	children?: React.ReactNode;
}

export function DepotFilters({ searchQuery, onSearchChange, onClearFilters, children }: DepotFiltersProps) {
	return (
		<div className="bg-card rounded-lg border border-border p-6">
			<div className="flex flex-col md:flex-row gap-4">
				{/* Search Input */}
				<div className="flex-1">
					<div className="relative">
						<Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
						<Input type="text" placeholder="Tìm kiếm theo tên hoặc địa chỉ..." value={searchQuery} onChange={(e) => onSearchChange(e.target.value)} className="pl-10" />
					</div>
				</div>

				{/* Clear Filters Button */}
				{searchQuery && (
					<Button variant="outline" onClick={onClearFilters} className="gap-2">
						<X className="w-4 h-4" />
						Xóa bộ lọc
					</Button>
				)}

				{/* Action Button Slot */}
				{children}
			</div>
		</div>
	);
}
