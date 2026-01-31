"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";

interface PaginationProps {
	currentPage: number;
	totalPages: number;
	itemsPerPage: number;
	totalItems: number;
	entityName?: string;
	onPageChange: (page: number) => void;
}

export function Pagination({ currentPage, totalPages, itemsPerPage, totalItems, entityName = "mục", onPageChange }: PaginationProps) {
	const startItem = (currentPage - 1) * itemsPerPage + 1;
	const endItem = Math.min(currentPage * itemsPerPage, totalItems);

	// Generate page numbers to display
	const getPageNumbers = () => {
		const pages: (number | string)[] = [];
		const maxPagesToShow = 5;

		if (totalPages <= maxPagesToShow) {
			// Show all pages if total is less than max
			for (let i = 1; i <= totalPages; i++) {
				pages.push(i);
			}
		} else {
			// Always show first page
			pages.push(1);

			if (currentPage > 3) {
				pages.push("...");
			}

			// Show pages around current page
			const start = Math.max(2, currentPage - 1);
			const end = Math.min(totalPages - 1, currentPage + 1);

			for (let i = start; i <= end; i++) {
				pages.push(i);
			}

			if (currentPage < totalPages - 2) {
				pages.push("...");
			}

			// Always show last page
			pages.push(totalPages);
		}

		return pages;
	};

	return (
		<div className="flex items-center justify-between px-6 py-4 border border-border rounded-b-lg bg-card">
			<div className="text-sm text-muted-foreground">
				Hiển thị {startItem} - {endItem} của tổng số <span className="font-semibold text-foreground">{totalItems}</span> {entityName}
			</div>

			<div className="flex items-center gap-2">
				<Button variant="outline" size="sm" onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 1} className="text-sm text-muted-foreground hover:text-foreground disabled:opacity-50">
					<ChevronLeft className="w-4 h-4 mr-1" />
					Previous
				</Button>

				<div className="flex items-center gap-1">
					{getPageNumbers().map((page, index) =>
						typeof page === "number" ? (
							<Button key={index} variant={currentPage === page ? "default" : "outline"} size="sm" onClick={() => onPageChange(page)} className={currentPage === page ? "bg-primary hover:bg-primary/90 text-primary-foreground min-w-8" : "text-muted-foreground hover:text-foreground min-w-8"}>
								{page}
							</Button>
						) : (
							<span key={index} className="px-2 text-muted-foreground">
								{page}
							</span>
						),
					)}
				</div>

				<Button variant="outline" size="sm" onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPages} className="text-sm text-muted-foreground hover:text-foreground disabled:opacity-50">
					Next
					<ChevronRight className="w-4 h-4 ml-1" />
				</Button>
			</div>
		</div>
	);
}
