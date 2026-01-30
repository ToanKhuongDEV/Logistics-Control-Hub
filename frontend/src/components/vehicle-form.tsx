"use client";

import React from "react";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { X } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { VehicleStatus, Vehicle, VehicleRequest } from "@/types/vehicle-types";

interface VehicleFormProps {
	vehicle?: Vehicle;
	onSubmit: (data: VehicleRequest) => void;
	onClose: () => void;
	isSubmitting?: boolean;
}

export function VehicleForm({ vehicle, onSubmit, onClose, isSubmitting = false }: VehicleFormProps) {
	const [formData, setFormData] = useState({
		code: vehicle?.code || "",
		maxWeightKg: vehicle?.maxWeightKg || "",
		maxVolumeM3: vehicle?.maxVolumeM3 || "",
		costPerKm: vehicle?.costPerKm || "",
		status: vehicle?.status || VehicleStatus.ACTIVE,
		type: vehicle?.type || "",
		driver: vehicle?.driver || "",
	});
	const [errors, setErrors] = useState<Record<string, string>>({});

	const validateForm = () => {
		const newErrors: Record<string, string> = {};

		if (!formData.code.trim()) {
			newErrors.code = "Mã xe là bắt buộc";
		}
		if (!formData.maxWeightKg || Number(formData.maxWeightKg) <= 0) {
			newErrors.maxWeightKg = "Tải trọng phải lớn hơn 0";
		}
		if (!formData.maxVolumeM3 || Number(formData.maxVolumeM3) <= 0) {
			newErrors.maxVolumeM3 = "Thể tích phải lớn hơn 0";
		}
		if (!formData.costPerKm || Number(formData.costPerKm) <= 0) {
			newErrors.costPerKm = "Chi phí km phải lớn hơn 0";
		}

		setErrors(newErrors);
		return Object.keys(newErrors).length === 0;
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		if (!validateForm()) return;

		onSubmit({
			code: formData.code,
			maxWeightKg: Number(formData.maxWeightKg),
			maxVolumeM3: Number(formData.maxVolumeM3),
			costPerKm: Number(formData.costPerKm),
			status: formData.status as VehicleStatus,
			type: formData.type || undefined,
			driver: formData.driver || undefined,
		});
	};

	return (
		<div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
			<div className="bg-card rounded-lg shadow-lg max-w-md w-full mx-4">
				<div className="flex items-center justify-between p-6 border-b border-border">
					<h2 className="text-lg font-semibold text-foreground">{vehicle ? "Sửa xe" : "Thêm xe mới"}</h2>
					<button onClick={onClose} className="text-muted-foreground hover:text-foreground transition-colors" disabled={isSubmitting}>
						<X className="w-5 h-5" />
					</button>
				</div>

				<form onSubmit={handleSubmit} className="p-6 space-y-4">
					<div className="space-y-2">
						<Label htmlFor="code" className="text-foreground">
							Mã xe <span className="text-red-500">*</span>
						</Label>
						<Input id="code" placeholder="VH001" value={formData.code} onChange={(e) => setFormData({ ...formData, code: e.target.value })} className="border-border" disabled={isSubmitting} />
						{errors.code && <p className="text-red-500 text-sm">{errors.code}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="maxWeightKg" className="text-foreground">
							Tải trọng tối đa (kg) <span className="text-red-500">*</span>
						</Label>
						<Input id="maxWeightKg" type="number" placeholder="1000" value={formData.maxWeightKg} onChange={(e) => setFormData({ ...formData, maxWeightKg: e.target.value })} className="border-border" disabled={isSubmitting} />
						{errors.maxWeightKg && <p className="text-red-500 text-sm">{errors.maxWeightKg}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="maxVolumeM3" className="text-foreground">
							Thể tích tối đa (m³) <span className="text-red-500">*</span>
						</Label>
						<Input id="maxVolumeM3" type="number" placeholder="15.5" step="0.01" value={formData.maxVolumeM3} onChange={(e) => setFormData({ ...formData, maxVolumeM3: e.target.value })} className="border-border" disabled={isSubmitting} />
						{errors.maxVolumeM3 && <p className="text-red-500 text-sm">{errors.maxVolumeM3}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="costPerKm" className="text-foreground">
							Chi phí/km (₫) <span className="text-red-500">*</span>
						</Label>
						<Input id="costPerKm" type="number" placeholder="5000" step="0.01" value={formData.costPerKm} onChange={(e) => setFormData({ ...formData, costPerKm: e.target.value })} className="border-border" disabled={isSubmitting} />
						{errors.costPerKm && <p className="text-red-500 text-sm">{errors.costPerKm}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="status" className="text-foreground">
							Trạng thái <span className="text-red-500">*</span>
						</Label>
						<Select value={formData.status} onValueChange={(value) => setFormData({ ...formData, status: value as VehicleStatus })} disabled={isSubmitting}>
							<SelectTrigger id="status" className="border-border">
								<SelectValue placeholder="Chọn trạng thái" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value={VehicleStatus.ACTIVE}>Đang hoạt động</SelectItem>
								<SelectItem value={VehicleStatus.MAINTENANCE}>Bảo trì</SelectItem>
								<SelectItem value={VehicleStatus.IDLE}>Nhàn rỗi</SelectItem>
							</SelectContent>
						</Select>
					</div>

					<div className="space-y-2">
						<Label htmlFor="type" className="text-foreground">
							Loại xe (Hãng/Model)
						</Label>
						<Input id="type" type="text" placeholder="VD: Hyundai Mighty, Isuzu FRR,..." value={formData.type} onChange={(e) => setFormData({ ...formData, type: e.target.value })} className="border-border" disabled={isSubmitting} />
					</div>

					<div className="space-y-2">
						<Label htmlFor="driver" className="text-foreground">
							Tài xế
						</Label>
						<Input id="driver" placeholder="Nguyễn Văn A" value={formData.driver} onChange={(e) => setFormData({ ...formData, driver: e.target.value })} className="border-border" disabled={isSubmitting} />
					</div>

					<div className="flex gap-3 pt-6">
						<Button type="button" variant="outline" onClick={onClose} className="flex-1 bg-transparent" disabled={isSubmitting}>
							Hủy
						</Button>
						<Button type="submit" className="flex-1 bg-primary hover:bg-primary/90 text-primary-foreground" disabled={isSubmitting}>
							{isSubmitting ? "Đang xử lý..." : vehicle ? "Cập nhật" : "Thêm"}
						</Button>
					</div>
				</form>
			</div>
		</div>
	);
}

export { VehicleStatus, type Vehicle, type VehicleRequest };
