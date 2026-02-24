"use client";

import React from "react";
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { X } from "lucide-react";
import { Driver, DriverRequest } from "@/types/driver-types";

interface DriverFormProps {
	driver?: Driver | null;
	onSubmit: (data: DriverRequest) => Promise<void>;
	onClose: () => void;
	isSubmitting?: boolean;
}

export function DriverForm({ driver, onSubmit, onClose, isSubmitting = false }: DriverFormProps) {
	const [formData, setFormData] = useState<DriverRequest>({
		name: driver?.name || "",
		licenseNumber: driver?.licenseNumber || "",
		phoneNumber: driver?.phoneNumber || "",
		email: driver?.email || "",
	});
	const [errors, setErrors] = useState<Record<string, string>>({});

	useEffect(() => {
		if (driver) {
			setFormData({
				name: driver.name,
				licenseNumber: driver.licenseNumber,
				phoneNumber: driver.phoneNumber,
				email: driver.email || "",
			});
		}
	}, [driver]);

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setErrors({});

		const newErrors: Record<string, string> = {};

		if (!formData.name.trim()) {
			newErrors.name = "Vui lòng nhập họ tên tài xế";
		}

		if (!formData.licenseNumber.trim()) {
			newErrors.licenseNumber = "Vui lòng nhập số GPLX";
		} else if (!/^[A-Z]\d-\d{6}$/.test(formData.licenseNumber.trim())) {
			newErrors.licenseNumber = "Số GPLX phải có định dạng X0-000000 (ví dụ: A1-123456)";
		}

		if (!formData.phoneNumber.trim()) {
			newErrors.phoneNumber = "Vui lòng nhập số điện thoại";
		} else if (!/^\d{10}$/.test(formData.phoneNumber.trim())) {
			newErrors.phoneNumber = "Số điện thoại phải bao gồm đúng 10 chữ số";
		}

		if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
			newErrors.email = "Email không đúng định dạng";
		}

		if (Object.keys(newErrors).length > 0) {
			setErrors(newErrors);
			return;
		}

		try {
			await onSubmit(formData);
		} catch (error) {
			console.error("Form submission error:", error);
		}
	};

	return (
		<div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
			<div className="bg-card border border-border rounded-lg shadow-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
				<div className="flex items-center justify-between p-6 border-b border-border sticky top-0 bg-card z-10">
					<h2 className="text-xl font-semibold text-foreground">{driver ? "Cập nhật tài xế" : "Thêm tài xế mới"}</h2>
					<Button onClick={onClose} variant="ghost" size="sm" disabled={isSubmitting}>
						<X className="w-5 h-5" />
					</Button>
				</div>

				<form onSubmit={handleSubmit} className="p-6 space-y-4">
					<div className="space-y-2">
						<Label htmlFor="name" className="text-foreground">
							Họ tên <span className="text-destructive">*</span>
						</Label>
						<Input id="name" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} placeholder="Nguyễn Văn A" disabled={isSubmitting} className={errors.name ? "border-destructive" : ""} />
						{errors.name && <p className="text-sm text-destructive">{errors.name}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="licenseNumber" className="text-foreground">
							Số GPLX <span className="text-destructive">*</span>
						</Label>
						<Input id="licenseNumber" value={formData.licenseNumber} onChange={(e) => setFormData({ ...formData, licenseNumber: e.target.value.toUpperCase() })} placeholder="A1-123456" disabled={isSubmitting} className={errors.licenseNumber ? "border-destructive" : ""} />
						{errors.licenseNumber && <p className="text-sm text-destructive">{errors.licenseNumber}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="phoneNumber" className="text-foreground">
							Số điện thoại <span className="text-destructive">*</span>
						</Label>
						<Input id="phoneNumber" value={formData.phoneNumber} onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })} placeholder="0901234567" disabled={isSubmitting} className={errors.phoneNumber ? "border-destructive" : ""} />
						{errors.phoneNumber && <p className="text-sm text-destructive">{errors.phoneNumber}</p>}
					</div>

					<div className="space-y-2">
						<Label htmlFor="email" className="text-foreground">
							Email
						</Label>
						<Input id="email" type="email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} placeholder="example@email.com" disabled={isSubmitting} className={errors.email ? "border-destructive" : ""} />
						{errors.email && <p className="text-sm text-destructive">{errors.email}</p>}
					</div>

					<div className="flex justify-end gap-3 pt-4">
						<Button type="button" onClick={onClose} variant="outline" disabled={isSubmitting}>
							Hủy
						</Button>
						<Button type="submit" disabled={isSubmitting}>
							{isSubmitting ? "Đang xử lý..." : driver ? "Cập nhật" : "Thêm mới"}
						</Button>
					</div>
				</form>
			</div>
		</div>
	);
}
