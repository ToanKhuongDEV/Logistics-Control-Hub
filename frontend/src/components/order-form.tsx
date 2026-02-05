"use client";

import React from "react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { X } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { OrderStatus, Order, OrderRequest } from "@/types/order-types";

import { driverApi, Driver } from "@/lib/driver-api";

interface OrderFormProps {
	order?: Order;
	onSubmit: (data: OrderRequest) => void;
	onClose: () => void;
	isSubmitting?: boolean;
}

export function OrderForm({ order, onSubmit, onClose, isSubmitting = false }: OrderFormProps) {
	const [drivers, setDrivers] = useState<Driver[]>([]);

	const [formData, setFormData] = useState(() => {
		const addressParts = order?.deliveryLocationName?.split(", ") || [];

		return {
			code: order?.code || "",
			weightKg: order?.weightKg?.toString() || "",
			volumeM3: order?.volumeM3?.toString() || "",
			status: order?.status || OrderStatus.CREATED,
			locationStreet: addressParts[0] || "",
			locationCity: addressParts[1] || "",
			locationCountry: "Việt Nam",
			driverId: order?.driverId ?? null,
		};
	});

	// Fetch drivers logic
	React.useEffect(() => {
		const fetchDrivers = async () => {
			try {
				const data = await driverApi.getAll();
				setDrivers(data);
			} catch (error) {
				console.error("Failed to fetch drivers", error);
			}
		};
		fetchDrivers();
	}, []);
	const [errors, setErrors] = useState<Record<string, string>>({});

	const validateForm = () => {
		const newErrors: Record<string, string> = {};

		if (!formData.locationStreet.trim()) {
			newErrors.locationStreet = "Địa chỉ là bắt buộc";
		}
		if (!formData.locationCity.trim()) {
			newErrors.locationCity = "Thành phố là bắt buộc";
		}
		if (!formData.locationCountry.trim()) {
			newErrors.locationCountry = "Quốc gia là bắt buộc";
		}
		if (formData.weightKg && Number(formData.weightKg) <= 0) {
			newErrors.weightKg = "Khối lượng phải lớn hơn 0";
		}
		if (formData.volumeM3 && Number(formData.volumeM3) <= 0) {
			newErrors.volumeM3 = "Thể tích phải lớn hơn 0";
		}

		setErrors(newErrors);
		return Object.keys(newErrors).length === 0;
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		if (!validateForm()) return;

		onSubmit({
			code: formData.code,
			deliveryLocation: {
				street: formData.locationStreet,
				city: formData.locationCity,
				country: formData.locationCountry,
			},
			weightKg: formData.weightKg ? Number(formData.weightKg) : undefined,
			volumeM3: formData.volumeM3 ? Number(formData.volumeM3) : undefined,
			status: formData.status as OrderStatus,
			driverId: formData.driverId,
		});
	};

	return (
		<div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
			<div className="bg-card rounded-lg shadow-lg max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
				<div className="flex items-center justify-between p-6 border-b border-border sticky top-0 bg-card z-10">
					<h2 className="text-lg font-semibold text-foreground">{order ? "Sửa đơn hàng" : "Thêm đơn hàng mới"}</h2>
					<button onClick={onClose} className="text-muted-foreground hover:text-foreground transition-colors" disabled={isSubmitting}>
						<X className="w-5 h-5" />
					</button>
				</div>

				<form onSubmit={handleSubmit} className="p-6 space-y-4">
					<div className="space-y-2">
						<Label htmlFor="code" className="text-foreground">
							Mã đơn hàng
						</Label>
						<Input id="code" placeholder={order ? "" : "Tự động tạo"} value={order ? formData.code : ""} readOnly disabled className="border-border bg-muted text-muted-foreground" />
					</div>

					<div className="space-y-2">
						<Label htmlFor="weightKg" className="text-foreground">
							Khối lượng (kg)
						</Label>
						<Input id="weightKg" type="number" placeholder="100" value={formData.weightKg} onChange={(e) => setFormData({ ...formData, weightKg: e.target.value })} className="border-border" disabled={isSubmitting} />
						{errors.weightKg && <p className="text-red-500 text-sm">{errors.weightKg}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="volumeM3" className="text-foreground">
							Thể tích (m³)
						</Label>
						<Input id="volumeM3" type="number" placeholder="1.5" step="0.01" value={formData.volumeM3} onChange={(e) => setFormData({ ...formData, volumeM3: e.target.value })} className="border-border" disabled={isSubmitting} />
						{errors.volumeM3 && <p className="text-red-500 text-sm">{errors.volumeM3}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="status" className="text-foreground">
							Trạng thái <span className="text-red-500">*</span>
						</Label>
						<Select value={formData.status} onValueChange={(value) => setFormData({ ...formData, status: value as OrderStatus })} disabled={isSubmitting}>
							<SelectTrigger id="status" className="border-border">
								<SelectValue placeholder="Chọn trạng thái" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value={OrderStatus.CREATED}>Đã tạo</SelectItem>
								<SelectItem value={OrderStatus.ASSIGNED}>Đã phân công</SelectItem>
								<SelectItem value={OrderStatus.IN_TRANSIT}>Đang vận chuyển</SelectItem>
								<SelectItem value={OrderStatus.DELIVERED}>Đã giao</SelectItem>
								<SelectItem value={OrderStatus.CANCELLED}>Đã hủy</SelectItem>
							</SelectContent>
						</Select>
					</div>

					<div className="border-t pt-4 mt-4">
						<h3 className="text-sm font-semibold text-foreground mb-3">Thông tin địa điểm giao hàng</h3>

						<div className="space-y-4">
							<div className="space-y-2">
								<Label htmlFor="locationStreet" className="text-foreground">
									Địa chỉ <span className="text-red-500">*</span>
								</Label>
								<Input id="locationStreet" placeholder="VD: 123 Nguyễn Huệ" value={formData.locationStreet} onChange={(e) => setFormData({ ...formData, locationStreet: e.target.value })} className="border-border" disabled={isSubmitting} />
								{errors.locationStreet && <p className="text-red-500 text-sm">{errors.locationStreet}</p>}
							</div>

							<div className="space-y-2">
								<Label htmlFor="locationCity" className="text-foreground">
									Thành phố <span className="text-red-500">*</span>
								</Label>
								<Input id="locationCity" placeholder="VD: Hồ Chí Minh" value={formData.locationCity} onChange={(e) => setFormData({ ...formData, locationCity: e.target.value })} className="border-border" disabled={isSubmitting} />
								{errors.locationCity && <p className="text-red-500 text-sm">{errors.locationCity}</p>}
							</div>
						</div>
					</div>

					<div className="space-y-2">
						<Label htmlFor="driver" className="text-foreground">
							Tài xế (Tùy chọn)
						</Label>
						<Select value={formData.driverId?.toString() ?? "null"} onValueChange={(value) => setFormData({ ...formData, driverId: value === "null" ? null : Number(value) })} disabled={isSubmitting}>
							<SelectTrigger id="driver" className="border-border">
								<SelectValue placeholder="Chọn tài xế" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value="null">Không gán tài xế</SelectItem>
								{Array.isArray(drivers) &&
									drivers.map((driver) => (
										<SelectItem key={driver.id} value={driver.id.toString()}>
											{driver.name} - {driver.licenseNumber}
										</SelectItem>
									))}
							</SelectContent>
						</Select>
					</div>

					<div className="flex gap-3 pt-6">
						<Button type="button" variant="outline" onClick={onClose} className="flex-1 bg-transparent" disabled={isSubmitting}>
							Hủy
						</Button>
						<Button type="submit" className="flex-1 bg-primary hover:bg-primary/90 text-primary-foreground" disabled={isSubmitting}>
							{isSubmitting ? "Đang xử lý..." : order ? "Cập nhật" : "Thêm"}
						</Button>
					</div>
				</form>
			</div>
		</div>
	);
}

export { OrderStatus, type Order, type OrderRequest };
