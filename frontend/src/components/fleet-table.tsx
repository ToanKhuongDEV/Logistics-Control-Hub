"use client";

import { Trash2, Edit2, Truck, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Vehicle, VehicleStatus } from "@/types/vehicle-types";

interface FleetTableProps {
	vehicles: Vehicle[];
	onEdit: (vehicle: Vehicle) => void;
	onDelete: (id: number) => void;
	isLoading?: boolean;
}

export function FleetTable({ vehicles, onEdit, onDelete, isLoading = false }: FleetTableProps) {
	const formatNumber = (num: number) => {
		return new Intl.NumberFormat("vi-VN").format(num);
	};

	if (isLoading) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<Loader2 className="w-12 h-12 text-muted-foreground mx-auto mb-4 animate-spin" />
				<p className="text-muted-foreground">Đang tải dữ liệu...</p>
			</div>
		);
	}

	if (vehicles.length === 0) {
		return (
			<div className="bg-card rounded-lg border border-border p-12 text-center">
				<Truck className="w-12 h-12 text-muted-foreground mx-auto mb-4 opacity-50" />
				<p className="text-muted-foreground">Chưa có xe nào trong đội</p>
			</div>
		);
	}

	return (
		<div className="overflow-x-auto bg-card rounded-t-lg border border-b-0 border-border">
			<table className="w-full">
				<thead className="bg-muted border-b border-border">
					<tr>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Mã xe</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Loại xe</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Tải trọng (kg)</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Thể tích (m³)</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Chi phí/km (₫)</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Trạng thái</th>
						<th className="px-6 py-4 text-left text-sm font-semibold text-foreground">Tài xế</th>
						<th className="px-6 py-4 text-right text-sm font-semibold text-foreground">Thao tác</th>
					</tr>
				</thead>
				<tbody className="divide-y divide-border">
					{vehicles.map((vehicle) => {
						const getStatusConfig = (status: VehicleStatus) => {
							if (status === VehicleStatus.ACTIVE) return { label: "Đang hoạt động", color: "bg-green-500/10 text-green-600 border-green-500/20" };
							if (status === VehicleStatus.MAINTENANCE) return { label: "Bảo trì", color: "bg-orange-500/10 text-orange-600 border-orange-500/20" };
							if (status === VehicleStatus.IDLE) return { label: "Nhàn rỗi", color: "bg-gray-500/10 text-gray-600 border-gray-500/20" };
							return { label: status, color: "bg-gray-500/10 text-gray-600 border-gray-500/20" };
						};

						const statusConfig = getStatusConfig(vehicle.status);

						return (
							<tr key={vehicle.id} className="hover:bg-muted/50 transition-colors">
								<td className="px-6 py-4 text-sm font-medium text-foreground">{vehicle.code}</td>
								<td className="px-6 py-4 text-sm text-foreground">{vehicle.type || "-"}</td>
								<td className="px-6 py-4 text-sm text-foreground">{formatNumber(vehicle.maxWeightKg)}</td>
								<td className="px-6 py-4 text-sm text-foreground">{Number(vehicle.maxVolumeM3).toFixed(2)}</td>
								<td className="px-6 py-4 text-sm text-foreground">{formatNumber(Number(vehicle.costPerKm))}</td>
								<td className="px-6 py-4">
									<span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border ${statusConfig.color}`}>{statusConfig.label}</span>
								</td>
								<td className="px-6 py-4 text-sm text-muted-foreground">{vehicle.driver || "-"}</td>
								<td className="px-6 py-4 text-right">
									<div className="flex items-center justify-end gap-2">
										<Button size="sm" variant="outline" onClick={() => onEdit(vehicle)} className="h-8 w-8 p-0 hover:bg-accent hover:text-accent-foreground">
											<Edit2 className="w-4 h-4" />
										</Button>
										<Button
											size="sm"
											variant="outline"
											onClick={() => {
												if (window.confirm(`Bạn có chắc muốn xóa xe ${vehicle.code}?`)) {
													onDelete(vehicle.id);
												}
											}}
											className="h-8 w-8 p-0 hover:bg-red-500/20 hover:text-red-500 text-red-500"
										>
											<Trash2 className="w-4 h-4" />
										</Button>
									</div>
								</td>
							</tr>
						);
					})}
				</tbody>
			</table>
		</div>
	);
}
