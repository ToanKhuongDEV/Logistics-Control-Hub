"use client";

import { Edit, Trash2, Loader2, CheckCircle, XCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Depot } from "@/types/depot-types";

interface DepotTableProps {
	depots: Depot[];
	onEdit: (depot: Depot) => void;
	onDelete: (id: number) => void;
	isLoading?: boolean;
}

export function DepotTable({ depots, onEdit, onDelete, isLoading }: DepotTableProps) {
	const formatDate = (dateString: string) => {
		return new Date(dateString).toLocaleDateString("vi-VN", {
			year: "numeric",
			month: "2-digit",
			day: "2-digit",
		});
	};

	const truncateText = (text: string, maxLength: number = 50) => {
		if (!text) return "-";
		return text.length > maxLength ? text.substring(0, maxLength) + "..." : text;
	};

	if (isLoading) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 flex flex-col items-center justify-center">
				<Loader2 className="w-8 h-8 text-primary animate-spin mb-4" />
				<p className="text-muted-foreground">Đang tải danh sách kho...</p>
			</div>
		);
	}

	if (depots.length === 0) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<p className="text-muted-foreground">Không tìm thấy kho nào</p>
			</div>
		);
	}

	return (
		<div className="overflow-x-auto bg-card rounded-t-lg border border-b-0 border-border">
			<table className="w-full">
				<thead className="bg-muted border-b border-border">
					<tr>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">ID</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Tên kho</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Địa chỉ</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Mô tả</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Trạng thái</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Ngày tạo</th>
						<th className="px-6 py-4 text-right text-sm font-semibold text-foreground">Thao tác</th>
					</tr>
				</thead>
				<tbody className="divide-y divide-border">
					{depots.map((depot) => (
						<tr key={depot.id} className="hover:bg-muted/50 transition-colors">
							<td className="px-6 py-4 text-sm text-foreground font-medium">#{depot.id}</td>
							<td className="px-6 py-4 text-sm text-foreground font-medium">{depot.name}</td>
							<td className="px-6 py-4 text-sm text-foreground max-w-xs">{truncateText(depot.address, 60)}</td>
							<td className="px-6 py-4 text-sm text-muted-foreground max-w-xs">{truncateText(depot.description, 40)}</td>
							<td className="px-6 py-4">
								{depot.isActive ? (
									<span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-green-50 text-green-700 border border-green-200">
										<CheckCircle className="w-3.5 h-3.5" />
										Hoạt động
									</span>
								) : (
									<span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-red-50 text-red-700 border border-red-200">
										<XCircle className="w-3.5 h-3.5" />
										Đóng cửa
									</span>
								)}
							</td>
							<td className="px-6 py-4 text-sm text-muted-foreground">{formatDate(depot.createdAt)}</td>
							<td className="px-6 py-4 text-right">
								<div className="flex items-center justify-end gap-2">
									<Button variant="outline" size="sm" onClick={() => onEdit(depot)} className="gap-2">
										<Edit className="w-4 h-4" />
										Sửa
									</Button>
									<Button variant="destructive" size="sm" onClick={() => onDelete(depot.id)} className="gap-2">
										<Trash2 className="w-4 h-4" />
										Xóa
									</Button>
								</div>
							</td>
						</tr>
					))}
				</tbody>
			</table>
		</div>
	);
}
