"use client";

import { Trash2, Edit2, UserCheck, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Driver } from "@/types/driver-types";

interface DriverTableProps {
	drivers: Driver[];
	onEdit: (driver: Driver) => void;
	onDelete: (id: number) => void;
	isLoading?: boolean;
}

export function DriverTable({ drivers, onEdit, onDelete, isLoading = false }: DriverTableProps) {
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

	if (isLoading) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<Loader2 className="w-12 h-12 text-muted-foreground mx-auto mb-4 animate-spin" />
				<p className="text-muted-foreground">Đang tải dữ liệu...</p>
			</div>
		);
	}

	if (drivers.length === 0) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<UserCheck className="w-12 h-12 text-muted-foreground mx-auto mb-4 opacity-50" />
				<p className="text-muted-foreground">Chưa có tài xế nào</p>
			</div>
		);
	}

	return (
		<div className="overflow-x-auto bg-card rounded-t-lg border border-b-0 border-border">
			<table className="w-full">
				<thead className="bg-muted border-b border-border">
					<tr>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Họ tên</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Số GPLX</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Số điện thoại</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Email</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Ngày tạo</th>
						<th className="px-6 py-4 text-right text-sm font-semibold text-foreground">Thao tác</th>
					</tr>
				</thead>
				<tbody className="divide-y divide-border">
					{drivers.map((driver) => (
						<tr key={driver.id} className="hover:bg-muted/50 transition-colors">
							<td className="px-6 py-4 text-sm text-foreground font-medium">{driver.name}</td>
							<td className="px-6 py-4 text-sm text-foreground">{driver.licenseNumber}</td>
							<td className="px-6 py-4 text-sm text-foreground">{driver.phoneNumber}</td>
							<td className="px-6 py-4 text-sm text-muted-foreground">{driver.email || "—"}</td>
							<td className="px-6 py-4 text-sm text-muted-foreground">{driver.createdAt ? formatDate(driver.createdAt) : "—"}</td>
							<td className="px-6 py-4 text-right">
								<div className="flex items-center justify-end gap-2">
									<Button onClick={() => onEdit(driver)} variant="outline" size="sm" className="gap-2">
										<Edit2 className="w-4 h-4" />
										Sửa
									</Button>
									<Button onClick={() => onDelete(driver.id)} variant="destructive" size="sm" className="gap-2">
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
