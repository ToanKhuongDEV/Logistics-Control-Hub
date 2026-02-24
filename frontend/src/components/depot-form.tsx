"use client";

import { useState, useEffect } from "react";
import { X, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Depot, DepotRequest } from "@/types/depot-types";

interface DepotFormProps {
	depot?: Depot | null;
	onSubmit: (data: DepotRequest) => Promise<void>;
	onClose: () => void;
	isSubmitting?: boolean;
}

export function DepotForm({ depot, onSubmit, onClose, isSubmitting }: DepotFormProps) {
	const [formData, setFormData] = useState<DepotRequest>({
		name: "",
		locationRequest: {
			street: "",
			city: "",
			country: "Việt Nam",
		},
		description: "",
		isActive: true,
	});

	const [errors, setErrors] = useState<Record<string, string>>({});

	useEffect(() => {
		if (depot) {
			setFormData({
				name: depot.name,
				locationRequest: {
					street: depot.street || "",
					city: depot.city || "",
					country: depot.country || "Việt Nam",
				},
				description: depot.description || "",
				isActive: depot.isActive ?? true,
			});
		}
	}, [depot]);

	const validateForm = (): boolean => {
		const newErrors: Record<string, string> = {};

		if (!formData.name.trim()) {
			newErrors.name = "Tên kho là bắt buộc";
		} else if (formData.name.length > 255) {
			newErrors.name = "Tên kho không được vượt quá 255 ký tự";
		}

		if (!formData.locationRequest.street.trim()) {
			newErrors.street = "Địa chỉ là bắt buộc";
		}

		if (!formData.locationRequest.city.trim()) {
			newErrors.city = "Thành phố là bắt buộc";
		}

		if (formData.description && formData.description.length > 500) {
			newErrors.description = "Mô tả không được vượt quá 500 ký tự";
		}

		setErrors(newErrors);
		return Object.keys(newErrors).length === 0;
	};

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();

		if (!validateForm()) {
			return;
		}

		try {
			await onSubmit(formData);
		} catch (error) {
			console.error("Form submission error:", error);
		}
	};

	const handleChange = (field: string, value: string | number | boolean | undefined) => {
		if (field === "street" || field === "city" || field === "country") {
			setFormData((prev) => ({
				...prev,
				locationRequest: {
					...prev.locationRequest,
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

	return (
		<div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
			<div className="bg-card rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
				{/* Header */}
				<div className="flex items-center justify-between p-6 border-b border-border">
					<h2 className="text-2xl font-bold text-foreground">{depot ? "Chỉnh sửa kho" : "Thêm kho mới"}</h2>
					<Button variant="ghost" size="icon" onClick={onClose} disabled={isSubmitting}>
						<X className="w-5 h-5" />
					</Button>
				</div>

				{/* Form */}
				<form onSubmit={handleSubmit} className="p-6 space-y-6">
					{/* Name */}
					<div className="space-y-2">
						<Label htmlFor="name">
							Tên kho <span className="text-red-500">*</span>
						</Label>
						<Input id="name" value={formData.name} onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleChange("name", e.target.value)} placeholder="Nhập tên kho" className={errors.name ? "border-red-500" : ""} disabled={isSubmitting} />
						{errors.name && <p className="text-sm text-red-500">{errors.name}</p>}
					</div>

					{/* Address Information */}
					<div className="border-t pt-4 mt-4">
						<h3 className="text-sm font-semibold text-foreground mb-4">Thông tin địa điểm</h3>

						<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
							<div className="space-y-2 md:col-span-2">
								<Label htmlFor="street">
									Địa chỉ (Số nhà, đường) <span className="text-red-500">*</span>
								</Label>
								<Input id="street" value={formData.locationRequest.street} onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleChange("street", e.target.value)} placeholder="VD: 123 Nguyễn Huệ" className={errors.street ? "border-red-500" : ""} disabled={isSubmitting} />
								{errors.street && <p className="text-sm text-red-500">{errors.street}</p>}
							</div>

							<div className="space-y-2">
								<Label htmlFor="city">
									Thành phố <span className="text-red-500">*</span>
								</Label>
								<Input id="city" value={formData.locationRequest.city} onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleChange("city", e.target.value)} placeholder="VD: Hồ Chí Minh" className={errors.city ? "border-red-500" : ""} disabled={isSubmitting} />
								{errors.city && <p className="text-sm text-red-500">{errors.city}</p>}
							</div>

							<div className="space-y-2">
								<Label htmlFor="country">Quốc gia</Label>
								<Input id="country" value={formData.locationRequest.country} readOnly className="bg-muted text-muted-foreground" disabled />
							</div>
						</div>
					</div>

					{/* Description */}
					<div className="space-y-2">
						<Label htmlFor="description">Mô tả</Label>
						<Textarea
							id="description"
							value={formData.description}
							onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => handleChange("description", e.target.value)}
							placeholder="Nhập mô tả về kho (tùy chọn)"
							className={errors.description ? "border-red-500" : ""}
							rows={3}
							disabled={isSubmitting}
						/>
						{errors.description && <p className="text-sm text-red-500">{errors.description}</p>}
					</div>

					{/* Status Toggle - Only visible when editing */}
					{depot && (
						<div className="space-y-4 border-t pt-4">
							<div className="flex items-center justify-between">
								<div className="space-y-0.5">
									<Label htmlFor="isActive" className="text-base font-semibold">
										Trạng thái hoạt động
									</Label>
									<p className="text-sm text-muted-foreground">{formData.isActive ? "Kho đang hoạt động" : "Kho đã đóng cửa"}</p>
								</div>
								<Switch id="isActive" checked={formData.isActive ?? true} onCheckedChange={(checked: boolean) => handleChange("isActive", checked)} disabled={isSubmitting} />
							</div>
						</div>
					)}

					{/* Actions */}
					<div className="flex items-center justify-end gap-3 pt-4 border-t border-border">
						<Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
							Hủy
						</Button>
						<Button type="submit" disabled={isSubmitting} className="gap-2">
							{isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
							{depot ? "Cập nhật" : "Thêm mới"}
						</Button>
					</div>
				</form>
			</div>
		</div>
	);
}
