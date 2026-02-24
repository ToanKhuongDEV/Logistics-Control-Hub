"use client";

import React from "react";
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { X } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { OrderStatus, Order, OrderRequest } from "@/types/order-types";
import { depotApi } from "@/lib/depot-api";
import { Depot } from "@/types/depot-types";

interface OrderFormProps {
	order?: Order;
	onSubmit: (data: OrderRequest) => void;
	onClose: () => void;
	isSubmitting?: boolean;
}

export function OrderForm({ order, onSubmit, onClose, isSubmitting = false }: OrderFormProps) {
	const [depots, setDepots] = useState<Depot[]>([]);
	const [formData, setFormData] = useState<OrderRequest>(() => {
		return {
			code: order?.code || "",
			deliveryLocation: {
				street: order?.deliveryStreet || "",
				city: order?.deliveryCity || "",
				country: order?.deliveryCountry || "Việt Nam",
			},
			weightKg: order?.weightKg || undefined,
			volumeM3: order?.volumeM3 || undefined,
			depotId: order?.depotId || undefined,
			status: order?.status || OrderStatus.CREATED,
		};
	});
	const [errors, setErrors] = useState<Record<string, string>>({});

	useEffect(() => {
		const fetchDepots = async () => {
			try {
				const response = await depotApi.getDepots({ size: 100 });
				setDepots(response.data);
			} catch (error) {
				console.error("Error fetching depots:", error);
			}
		};
		fetchDepots();
	}, []);

	const validateForm = () => {
		const newErrors: Record<string, string> = {};

		if (!formData.deliveryLocation.street.trim()) {
			newErrors.street = "Địa chỉ là bắt buộc";
		}
		if (!formData.deliveryLocation.city.trim()) {
			newErrors.city = "Thành phố là bắt buộc";
		}
		if (!formData.deliveryLocation.country.trim()) {
			newErrors.country = "Quốc gia là bắt buộc";
		}
		if (formData.weightKg !== undefined && formData.weightKg <= 0) {
			newErrors.weightKg = "Khối lượng phải lớn hơn 0";
		}
		if (formData.volumeM3 !== undefined && formData.volumeM3 <= 0) {
			newErrors.volumeM3 = "Thể tích phải lớn hơn 0";
		}

		setErrors(newErrors);
		return Object.keys(newErrors).length === 0;
	};

	const handleChange = (field: string, value: any) => {
		if (field === "street" || field === "city" || field === "country") {
			setFormData((prev) => ({
				...prev,
				deliveryLocation: {
					...prev.deliveryLocation,
					[field]: value,
				},
			}));
		} else {
			setFormData((prev) => ({
				...prev,
				[field]: value,
			}));
		}
		if (errors[field]) {
			setErrors((prev) => ({ ...prev, [field]: "" }));
		}
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		if (!validateForm()) return;

		onSubmit(formData);
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
						<Label htmlFor="depotId" className="text-foreground">
							Kho bãi
						</Label>
						<Select value={formData.depotId?.toString()} onValueChange={(value) => handleChange("depotId", Number(value))} disabled={isSubmitting}>
							<SelectTrigger id="depotId" className="border-border">
								<SelectValue placeholder="Chọn kho bãi" />
							</SelectTrigger>
							<SelectContent>
								{depots.map((depot) => (
									<SelectItem key={depot.id} value={depot.id.toString()}>
										{depot.name}
									</SelectItem>
								))}
							</SelectContent>
						</Select>
					</div>

					<div className="space-y-2">
						<Label htmlFor="weightKg" className="text-foreground">
							Khối lượng (kg)
						</Label>
						<Input id="weightKg" type="number" placeholder="100" value={formData.weightKg || ""} onChange={(e) => handleChange("weightKg", e.target.value ? Number(e.target.value) : undefined)} className="border-border" disabled={isSubmitting} />
						{errors.weightKg && <p className="text-red-500 text-sm">{errors.weightKg}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="volumeM3" className="text-foreground">
							Thể tích (m³)
						</Label>
						<Input id="volumeM3" type="number" placeholder="1.5" step="0.01" value={formData.volumeM3 || ""} onChange={(e) => handleChange("volumeM3", e.target.value ? Number(e.target.value) : undefined)} className="border-border" disabled={isSubmitting} />
						{errors.volumeM3 && <p className="text-red-500 text-sm">{errors.volumeM3}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="status" className="text-foreground">
							Trạng thái <span className="text-red-500">*</span>
						</Label>
						<Select value={formData.status} onValueChange={(value) => handleChange("status", value as OrderStatus)} disabled={isSubmitting}>
							<SelectTrigger id="status" className="border-border">
								<SelectValue placeholder="Chọn trạng thái" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value={OrderStatus.CREATED}>Đã tạo</SelectItem>
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
								<Label htmlFor="street" className="text-foreground">
									Địa chỉ <span className="text-red-500">*</span>
								</Label>
								<Input id="street" placeholder="VD: 123 Nguyễn Huệ" value={formData.deliveryLocation.street} onChange={(e) => handleChange("street", e.target.value)} className="border-border" disabled={isSubmitting} />
								{errors.street && <p className="text-red-500 text-sm">{errors.street}</p>}
							</div>

							<div className="space-y-2">
								<Label htmlFor="city" className="text-foreground">
									Thành phố <span className="text-red-500">*</span>
								</Label>
								<Input id="city" placeholder="VD: Hồ Chí Minh" value={formData.deliveryLocation.city} onChange={(e) => handleChange("city", e.target.value)} className="border-border" disabled={isSubmitting} />
								{errors.city && <p className="text-red-500 text-sm">{errors.city}</p>}
							</div>

							<div className="space-y-2">
								<Label htmlFor="country" className="text-foreground">
									Quốc gia <span className="text-red-500">*</span>
								</Label>
								<Input id="country" placeholder="VD: Việt Nam" value={formData.deliveryLocation.country} onChange={(e) => handleChange("country", e.target.value)} className="border-border" disabled={isSubmitting} />
								{errors.country && <p className="text-red-500 text-sm">{errors.country}</p>}
							</div>
						</div>
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
