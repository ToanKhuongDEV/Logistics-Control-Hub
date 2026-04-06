"use client";

import { useEffect, useMemo, useState } from "react";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card } from "@/components/ui/card";
import { useAuth } from "@/contexts/auth-context";
import { Globe, Bell, BookOpen, Save, X, Loader2, KeyRound, Eye, EyeOff } from "lucide-react";
import { companyApi } from "@/lib/company-api";
import { authService, hasPermission } from "@/lib/auth";
import { toast } from "sonner";

type SettingsTab = "company" | "notifications" | "guide";

export default function SettingsPage() {
	const { user } = useAuth();
	const canManageCompany = hasPermission(user, "company.manage");
	const availableTabs = useMemo<SettingsTab[]>(() => (canManageCompany ? ["company", "notifications", "guide"] : ["notifications", "guide"]), [canManageCompany]);
	const [activeTab, setActiveTab] = useState<SettingsTab>(canManageCompany ? "company" : "guide");
	const [isEditingCompany, setIsEditingCompany] = useState(false);
	const [isLoading, setIsLoading] = useState(true);
	const [isSaving, setIsSaving] = useState(false);
	const [isChangingPassword, setIsChangingPassword] = useState(false);
	const [showChangePasswords, setShowChangePasswords] = useState({
		currentPassword: false,
		newPassword: false,
		confirmPassword: false,
	});

	const [companyData, setCompanyData] = useState({
		name: "",
		email: "",
		phone: "",
		address: "",
		website: "",
		taxId: "",
		description: "",
	});
	const [originalData, setOriginalData] = useState(companyData);
	const [changePasswordForm, setChangePasswordForm] = useState({
		currentPassword: "",
		newPassword: "",
		confirmPassword: "",
	});

	const quickGuide = [
		"Tạo hoặc cập nhật kho, xe và lái xe để hệ thống có đủ dữ liệu vận hành.",
		"Tạo đơn hàng với đầy đủ địa chỉ giao, tải trọng và thông tin cần thiết.",
		"Chạy tối ưu hóa để hệ thống phân bổ đơn hàng và sắp xếp lộ trình phù hợp.",
		"Theo dõi kết quả trên màn hình và cập nhật trạng thái đơn hàng trong quá trình giao.",
	];

	useEffect(() => {
		if (!availableTabs.includes(activeTab)) {
			setActiveTab(availableTabs[0]);
		}
	}, [activeTab, availableTabs]);

	useEffect(() => {
		if (!canManageCompany) {
			setIsLoading(false);
			return;
		}

		const fetchCompanyInfo = async () => {
			setIsLoading(true);
			try {
				const data = await companyApi.getCompanyInfo();
				if (data) {
					const formattedData = {
						name: data.name || "",
						email: data.email || "",
						phone: data.phone || "",
						address: data.address || "",
						website: data.website || "",
						taxId: data.taxId || "",
						description: data.description || "",
					};
					setCompanyData(formattedData);
					setOriginalData(formattedData);
				}
			} catch (error: any) {
				console.error("Error fetching company info:", error);
				toast.error("Không thể tải thông tin công ty");
			} finally {
				setIsLoading(false);
			}
		};

		void fetchCompanyInfo();
	}, [canManageCompany]);

	const handleCompanySave = async () => {
		setIsSaving(true);
		try {
			await companyApi.updateCompanyInfo({
				name: companyData.name,
				email: companyData.email,
				phone: companyData.phone,
				address: companyData.address,
				website: companyData.website,
				taxId: companyData.taxId,
				description: companyData.description,
			});
			setOriginalData(companyData);
			setIsEditingCompany(false);
			toast.success("Lưu thông tin công ty thành công");
		} catch (error: any) {
			console.error("Error saving company info:", error);
			toast.error("Không thể lưu thông tin công ty");
		} finally {
			setIsSaving(false);
		}
	};

	const handleChangePassword = async () => {
		if (changePasswordForm.newPassword !== changePasswordForm.confirmPassword) {
			toast.error("Mật khẩu xác nhận không khớp");
			return;
		}

		setIsChangingPassword(true);
		try {
			await authService.changePassword({
				currentPassword: changePasswordForm.currentPassword,
				newPassword: changePasswordForm.newPassword,
			});
			setChangePasswordForm({
				currentPassword: "",
				newPassword: "",
				confirmPassword: "",
			});
			toast.success("Đổi mật khẩu thành công");
		} catch (error: any) {
			console.error("Change password error:", error);
			toast.error(error.response?.data?.message || "Không thể đổi mật khẩu");
		} finally {
			setIsChangingPassword(false);
		}
	};

	const renderPasswordField = ({
		id,
		label,
		value,
		placeholder,
		isVisible,
		onToggle,
		onChange,
	}: {
		id: string;
		label: string;
		value: string;
		placeholder: string;
		isVisible: boolean;
		onToggle: () => void;
		onChange: (value: string) => void;
	}) => (
		<div>
			<Label htmlFor={id}>{label}</Label>
			<div className="relative mt-2">
				<Input id={id} type={isVisible ? "text" : "password"} value={value} onChange={(e) => onChange(e.target.value)} className="pr-11" placeholder={placeholder} />
				<button type="button" onClick={onToggle} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground" aria-label={isVisible ? "Ẩn mật khẩu" : "Hiện mật khẩu"}>
					{isVisible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
				</button>
			</div>
		</div>
	);

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex flex-col h-full">
					<div className="border-b border-border bg-card">
						<div className="px-8 py-6">
							<h1 className="text-3xl font-bold text-foreground">Cài đặt</h1>
							<p className="text-muted-foreground mt-2">Quản lý thông tin hệ thống, thông báo và bảo mật</p>
						</div>
					</div>

					<div className="p-8">
						<div className="mb-8 flex flex-wrap gap-2 border-b border-border pb-4">
							{availableTabs.includes("company") && (
								<button onClick={() => setActiveTab("company")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "company" ? "border-b-2 border-primary text-primary" : "text-muted-foreground hover:text-foreground"}`}>
									<div className="flex items-center gap-2">
										<Globe className="h-4 w-4" />
										Công ty
									</div>
								</button>
							)}
							<button onClick={() => setActiveTab("notifications")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "notifications" ? "border-b-2 border-primary text-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<Bell className="h-4 w-4" />
									Thông báo
								</div>
							</button>
							<button onClick={() => setActiveTab("guide")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "guide" ? "border-b-2 border-primary text-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<BookOpen className="h-4 w-4" />
									Hướng dẫn
								</div>
							</button>
						</div>

						{activeTab === "company" && canManageCompany && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<div className="mb-6 flex items-center justify-between">
										<h2 className="text-xl font-semibold text-foreground">Thông tin công ty</h2>
										{!isEditingCompany ? (
											<Button onClick={() => setIsEditingCompany(true)} variant="outline" disabled={isLoading}>
												{isLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : "Sửa"}
											</Button>
										) : (
											<Button
												onClick={() => {
													setCompanyData(originalData);
													setIsEditingCompany(false);
												}}
												variant="outline"
												className="gap-2"
											>
												<X className="h-4 w-4" />
												Hủy
											</Button>
										)}
									</div>

									{isLoading ? (
										<div className="flex items-center justify-center py-12">
											<Loader2 className="h-8 w-8 animate-spin text-primary" />
										</div>
									) : (
										<div className="space-y-4">
											<div>
												<Label htmlFor="companyName">Tên công ty</Label>
												<Input id="companyName" value={companyData.name} onChange={(e) => setCompanyData({ ...companyData, name: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="companyEmail">Email công ty</Label>
												<Input id="companyEmail" type="email" value={companyData.email} onChange={(e) => setCompanyData({ ...companyData, email: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="phone">Số điện thoại</Label>
												<Input id="phone" value={companyData.phone} onChange={(e) => setCompanyData({ ...companyData, phone: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="address">Địa chỉ</Label>
												<Input id="address" value={companyData.address} onChange={(e) => setCompanyData({ ...companyData, address: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="website">Website</Label>
												<Input id="website" value={companyData.website} onChange={(e) => setCompanyData({ ...companyData, website: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="taxId">Mã số thuế</Label>
												<Input id="taxId" value={companyData.taxId} onChange={(e) => setCompanyData({ ...companyData, taxId: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
											</div>

											{isEditingCompany && (
												<Button onClick={handleCompanySave} className="w-full gap-2 bg-primary hover:bg-primary/90" disabled={isSaving}>
													{isSaving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
													Lưu thay đổi
												</Button>
											)}
										</div>
									)}
								</Card>
							</div>
						)}

						{activeTab === "notifications" && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<h2 className="mb-6 text-xl font-semibold text-foreground">Cài đặt thông báo</h2>
									<div className="rounded-lg border border-border bg-card p-12 text-center">
										<p className="text-muted-foreground">Tính năng đang được phát triển</p>
									</div>
								</Card>
							</div>
						)}

						{activeTab === "guide" && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<h2 className="mb-2 text-xl font-semibold text-foreground">Hướng dẫn sử dụng</h2>
									<p className="mb-6 text-muted-foreground">Xem nhanh các bước vận hành và cách hệ thống tối ưu hóa lộ trình.</p>
									<div className="mb-6 rounded-xl border border-border bg-background p-5">
										<h3 className="mb-3 text-base font-semibold text-foreground">Các bước sử dụng</h3>
										<div className="space-y-3">
											{quickGuide.map((step, index) => (
												<div key={step} className="flex items-start gap-3">
													<div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">{index + 1}</div>
													<p className="text-sm leading-6 text-muted-foreground">{step}</p>
												</div>
											))}
										</div>
									</div>

									<Card className="border border-border p-6 shadow-none">
										<div className="mb-6 flex items-center gap-3">
											<div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
												<KeyRound className="h-5 w-5" />
											</div>
											<div>
												<h3 className="text-lg font-semibold text-foreground">Đổi mật khẩu</h3>
												<p className="text-sm text-muted-foreground">Cập nhật mật khẩu cho tài khoản đang đăng nhập.</p>
											</div>
										</div>
										<div className="grid gap-4 md:grid-cols-2">
											<div className="md:col-span-2">
												{renderPasswordField({
													id: "currentPassword",
													label: "Mật khẩu hiện tại",
													value: changePasswordForm.currentPassword,
													placeholder: "Nhập mật khẩu hiện tại",
													isVisible: showChangePasswords.currentPassword,
													onToggle: () => setShowChangePasswords({ ...showChangePasswords, currentPassword: !showChangePasswords.currentPassword }),
													onChange: (value) => setChangePasswordForm({ ...changePasswordForm, currentPassword: value }),
												})}
											</div>
											<div>
												{renderPasswordField({
													id: "updatedPassword",
													label: "Mật khẩu mới",
													value: changePasswordForm.newPassword,
													placeholder: "Ít nhất 8 ký tự",
													isVisible: showChangePasswords.newPassword,
													onToggle: () => setShowChangePasswords({ ...showChangePasswords, newPassword: !showChangePasswords.newPassword }),
													onChange: (value) => setChangePasswordForm({ ...changePasswordForm, newPassword: value }),
												})}
											</div>
											<div>
												{renderPasswordField({
													id: "confirmUpdatedPassword",
													label: "Xác nhận mật khẩu mới",
													value: changePasswordForm.confirmPassword,
													placeholder: "Nhập lại mật khẩu mới",
													isVisible: showChangePasswords.confirmPassword,
													onToggle: () => setShowChangePasswords({ ...showChangePasswords, confirmPassword: !showChangePasswords.confirmPassword }),
													onChange: (value) => setChangePasswordForm({ ...changePasswordForm, confirmPassword: value }),
												})}
											</div>
										</div>
										<Button onClick={handleChangePassword} className="mt-6 gap-2" disabled={isChangingPassword}>
											{isChangingPassword ? <Loader2 className="h-4 w-4 animate-spin" /> : <KeyRound className="h-4 w-4" />}
											Cập nhật mật khẩu
										</Button>
									</Card>
								</Card>
							</div>
						)}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
