"use client";

import { useEffect, useMemo, useState } from "react";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useAuth } from "@/contexts/auth-context";
import { Globe, Bell, BookOpen, Users, Save, X, Loader2, KeyRound, UserPlus, Eye, EyeOff, Warehouse, ShieldCheck } from "lucide-react";
import { companyApi } from "@/lib/company-api";
import { authService, User, UserRole } from "@/lib/auth";
import { depotApi } from "@/lib/depot-api";
import { Depot } from "@/types/depot-types";
import { toast } from "sonner";

type SettingsTab = "company" | "notifications" | "guide" | "users";

const EMPTY_CREATE_FORM = {
	username: "",
	fullName: "",
	email: "",
	password: "",
	confirmPassword: "",
	role: "DISPATCHER" as UserRole,
	assignedDepotIds: [] as number[],
};

const EMPTY_EDIT_FORM = {
	id: 0,
	fullName: "",
	email: "",
	role: "DISPATCHER" as UserRole,
	assignedDepotIds: [] as number[],
};

export default function SettingsPage() {
	const { user } = useAuth();
	const isAdmin = user?.role === "ADMIN";
	const availableTabs = useMemo<SettingsTab[]>(() => (isAdmin ? ["company", "notifications", "guide", "users"] : ["notifications", "guide"]), [isAdmin]);
	const [activeTab, setActiveTab] = useState<SettingsTab>(isAdmin ? "company" : "guide");
	const [isEditingCompany, setIsEditingCompany] = useState(false);
	const [isLoading, setIsLoading] = useState(true);
	const [isSaving, setIsSaving] = useState(false);
	const [isCreatingAccount, setIsCreatingAccount] = useState(false);
	const [isUpdatingAccount, setIsUpdatingAccount] = useState(false);
	const [isChangingPassword, setIsChangingPassword] = useState(false);
	const [isLoadingAccounts, setIsLoadingAccounts] = useState(false);
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
	const [depots, setDepots] = useState<Depot[]>([]);
	const [accounts, setAccounts] = useState<User[]>([]);
	const [selectedAccount, setSelectedAccount] = useState<User | null>(null);
	const [createAccountForm, setCreateAccountForm] = useState(EMPTY_CREATE_FORM);
	const [editAccountForm, setEditAccountForm] = useState(EMPTY_EDIT_FORM);
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
		if (!isAdmin) {
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
	}, [isAdmin]);

	useEffect(() => {
		if (!isAdmin) {
			return;
		}

		const fetchAdminData = async () => {
			setIsLoadingAccounts(true);
			try {
				const [depotResult, accountResult] = await Promise.all([depotApi.getDepots({ page: 0, size: 100 }), authService.getAccounts()]);
				setDepots(depotResult.data.filter((depot) => depot.isActive));
				setAccounts(accountResult);
			} catch (error: any) {
				console.error("Error fetching account management data:", error);
				toast.error(error?.response?.data?.message || "Không thể tải dữ liệu tài khoản");
			} finally {
				setIsLoadingAccounts(false);
			}
		};

		void fetchAdminData();
	}, [isAdmin]);

	const resetCreateForm = () => setCreateAccountForm(EMPTY_CREATE_FORM);

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

	const handleCreateDepotToggle = (depotId: number) => {
		setCreateAccountForm((current) => ({
			...current,
			assignedDepotIds: current.assignedDepotIds.includes(depotId) ? current.assignedDepotIds.filter((id) => id !== depotId) : [...current.assignedDepotIds, depotId],
		}));
	};

	const handleEditDepotToggle = (depotId: number) => {
		setEditAccountForm((current) => ({
			...current,
			assignedDepotIds: current.assignedDepotIds.includes(depotId) ? current.assignedDepotIds.filter((id) => id !== depotId) : [...current.assignedDepotIds, depotId],
		}));
	};

	const validateDispatcherDepotSelection = (role: UserRole, assignedDepotIds: number[]) => {
		if (role === "DISPATCHER" && assignedDepotIds.length === 0) {
			toast.error("Dispatcher phải được gán ít nhất một kho");
			return false;
		}
		return true;
	};

	const refreshAccounts = async () => {
		const accountResult = await authService.getAccounts();
		setAccounts(accountResult);
		return accountResult;
	};

	const handleCreateAccount = async () => {
		if (createAccountForm.password !== createAccountForm.confirmPassword) {
			toast.error("Mật khẩu xác nhận không khớp");
			return;
		}

		if (!validateDispatcherDepotSelection(createAccountForm.role, createAccountForm.assignedDepotIds)) {
			return;
		}

		setIsCreatingAccount(true);
		try {
			await authService.createAccount({
				username: createAccountForm.username,
				fullName: createAccountForm.fullName,
				email: createAccountForm.email,
				password: createAccountForm.password,
				role: createAccountForm.role,
				assignedDepotIds: createAccountForm.role === "DISPATCHER" ? createAccountForm.assignedDepotIds : [],
			});
			resetCreateForm();
			await refreshAccounts();
			toast.success("Đã tạo tài khoản mới");
		} catch (error: any) {
			console.error("Create account error:", error);
			toast.error(error.response?.data?.message || "Không thể tạo tài khoản");
		} finally {
			setIsCreatingAccount(false);
		}
	};

	const handleSelectAccount = (account: User) => {
		setSelectedAccount(account);
		setEditAccountForm({
			id: account.id,
			fullName: account.fullName || "",
			email: account.email || "",
			role: account.role,
			assignedDepotIds: account.assignedDepots?.map((depot) => depot.id) || [],
		});
	};

	const handleUpdateAccount = async () => {
		if (!selectedAccount) {
			return;
		}

		if (!validateDispatcherDepotSelection(editAccountForm.role, editAccountForm.assignedDepotIds)) {
			return;
		}

		setIsUpdatingAccount(true);
		try {
			const updated = await authService.updateAccount(selectedAccount.id, {
				fullName: editAccountForm.fullName,
				email: editAccountForm.email,
				role: editAccountForm.role,
				assignedDepotIds: editAccountForm.role === "DISPATCHER" ? editAccountForm.assignedDepotIds : [],
			});

			const nextAccounts = await refreshAccounts();
			setSelectedAccount(updated);
			handleSelectAccount(nextAccounts.find((account) => account.id === updated.id) || updated);
			toast.success("Đã cập nhật tài khoản");
		} catch (error: any) {
			console.error("Update account error:", error);
			toast.error(error.response?.data?.message || "Không thể cập nhật tài khoản");
		} finally {
			setIsUpdatingAccount(false);
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

	const renderDepotChecklist = (selectedDepotIds: number[], onToggle: (depotId: number) => void, disabled: boolean) => (
		<div className="rounded-lg border border-border p-4 space-y-3">
			<div className="flex items-center gap-2 text-sm font-medium text-foreground">
				<Warehouse className="h-4 w-4 text-primary" />
				Kho phụ trách
			</div>
			<div className="grid gap-2 md:grid-cols-2">
				{depots.map((depot) => (
					<label key={depot.id} className={`flex items-start gap-3 rounded-md border border-border px-3 py-2 ${disabled ? "opacity-60" : "cursor-pointer hover:bg-muted/40"}`}>
						<input type="checkbox" checked={selectedDepotIds.includes(depot.id)} onChange={() => onToggle(depot.id)} disabled={disabled} className="mt-1 h-4 w-4 rounded border-border" />
						<span className="text-sm">
							<span className="block font-medium text-foreground">{depot.name}</span>
							<span className="block text-muted-foreground">{depot.city}</span>
						</span>
					</label>
				))}
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
							<p className="text-muted-foreground mt-2">Quản lý thông tin hệ thống, tài khoản và bảo mật</p>
						</div>
					</div>

					<div className="p-8">
						<div className="flex gap-2 mb-8 border-b border-border pb-4 flex-wrap">
							{availableTabs.includes("company") && (
								<button onClick={() => setActiveTab("company")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "company" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
									<div className="flex items-center gap-2">
										<Globe className="w-4 h-4" />
										Công ty
									</div>
								</button>
							)}
							<button onClick={() => setActiveTab("notifications")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "notifications" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<Bell className="w-4 h-4" />
									Thông báo
								</div>
							</button>
							<button onClick={() => setActiveTab("guide")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "guide" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<BookOpen className="w-4 h-4" />
									Hướng dẫn
								</div>
							</button>
							{availableTabs.includes("users") && (
								<button onClick={() => setActiveTab("users")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "users" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
									<div className="flex items-center gap-2">
										<Users className="w-4 h-4" />
										Người dùng
									</div>
								</button>
							)}
						</div>

						{activeTab === "company" && isAdmin && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<div className="flex items-center justify-between mb-6">
										<h2 className="text-xl font-semibold text-foreground">Thông tin công ty</h2>
										{!isEditingCompany ? (
											<Button onClick={() => setIsEditingCompany(true)} variant="outline" disabled={isLoading}>
												{isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : "Sửa"}
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
												<X className="w-4 h-4" />
												Hủy
											</Button>
										)}
									</div>

									{isLoading ? (
										<div className="flex items-center justify-center py-12">
											<Loader2 className="w-8 h-8 animate-spin text-primary" />
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
												<Button onClick={handleCompanySave} className="w-full bg-primary hover:bg-primary/90 gap-2" disabled={isSaving}>
													{isSaving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
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
									<h2 className="text-xl font-semibold text-foreground mb-6">Cài đặt thông báo</h2>
									<div className="bg-card rounded-lg border border-border p-12 text-center">
										<p className="text-muted-foreground">Tính năng đang được phát triển</p>
									</div>
								</Card>
							</div>
						)}

						{activeTab === "guide" && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<h2 className="text-xl font-semibold text-foreground mb-2">Hướng dẫn sử dụng</h2>
									<p className="text-muted-foreground mb-6">Xem nhanh các bước vận hành và cách hệ thống tối ưu hóa lộ trình.</p>
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

									<Card className="p-6 border border-border shadow-none">
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

						{activeTab === "users" && isAdmin && (
							<div className="space-y-6">
								<div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
									<Card className="p-6">
										<div className="mb-6 flex items-center gap-3">
											<div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
												<UserPlus className="h-5 w-5" />
											</div>
											<div>
												<h2 className="text-xl font-semibold text-foreground">Tạo tài khoản</h2>
												<p className="text-sm text-muted-foreground">Admin có thể tạo dispatcher và gán kho phụ trách ngay khi khởi tạo.</p>
											</div>
										</div>

										<div className="grid gap-4 md:grid-cols-2">
											<div>
												<Label htmlFor="newUsername">Tên đăng nhập</Label>
												<Input id="newUsername" value={createAccountForm.username} onChange={(e) => setCreateAccountForm({ ...createAccountForm, username: e.target.value })} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="newFullName">Họ và tên</Label>
												<Input id="newFullName" value={createAccountForm.fullName} onChange={(e) => setCreateAccountForm({ ...createAccountForm, fullName: e.target.value })} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="newEmail">Email</Label>
												<Input id="newEmail" type="email" value={createAccountForm.email} onChange={(e) => setCreateAccountForm({ ...createAccountForm, email: e.target.value })} className="mt-2" />
											</div>
											<div>
												<Label htmlFor="newPassword">Mật khẩu tạm thời</Label>
												<Input id="newPassword" type="password" value={createAccountForm.password} onChange={(e) => setCreateAccountForm({ ...createAccountForm, password: e.target.value })} className="mt-2" />
											</div>
											<div className="md:col-span-2">
												<Label htmlFor="confirmNewPassword">Xác nhận mật khẩu</Label>
												<Input id="confirmNewPassword" type="password" value={createAccountForm.confirmPassword} onChange={(e) => setCreateAccountForm({ ...createAccountForm, confirmPassword: e.target.value })} className="mt-2" />
											</div>
											<div className="md:col-span-2">
												<Label>Vai trò</Label>
												<Select value={createAccountForm.role} onValueChange={(value) => setCreateAccountForm({ ...createAccountForm, role: value as UserRole, assignedDepotIds: value === "ADMIN" ? [] : createAccountForm.assignedDepotIds })}>
													<SelectTrigger className="mt-2 w-full">
														<SelectValue placeholder="Chọn vai trò" />
													</SelectTrigger>
													<SelectContent>
														<SelectItem value="DISPATCHER">Dispatcher</SelectItem>
														<SelectItem value="ADMIN">Admin</SelectItem>
													</SelectContent>
												</Select>
											</div>
											{createAccountForm.role === "DISPATCHER" && <div className="md:col-span-2">{renderDepotChecklist(createAccountForm.assignedDepotIds, handleCreateDepotToggle, false)}</div>}
										</div>

										<Button onClick={handleCreateAccount} className="mt-6 gap-2" disabled={isCreatingAccount}>
											{isCreatingAccount ? <Loader2 className="h-4 w-4 animate-spin" /> : <UserPlus className="h-4 w-4" />}
											Tạo tài khoản
										</Button>
									</Card>

									<Card className="p-6">
										<div className="mb-6 flex items-center gap-3">
											<div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
												<ShieldCheck className="h-5 w-5" />
											</div>
											<div>
												<h2 className="text-xl font-semibold text-foreground">Cập nhật tài khoản</h2>
												<p className="text-sm text-muted-foreground">Chọn một tài khoản ở danh sách bên dưới để sửa vai trò và kho phụ trách.</p>
											</div>
										</div>

										{selectedAccount ? (
											<div className="space-y-4">
												<div>
													<Label>Tên đăng nhập</Label>
													<Input value={selectedAccount.username} disabled className="mt-2" />
												</div>
												<div>
													<Label>Họ và tên</Label>
													<Input value={editAccountForm.fullName} onChange={(e) => setEditAccountForm({ ...editAccountForm, fullName: e.target.value })} className="mt-2" />
												</div>
												<div>
													<Label>Email</Label>
													<Input type="email" value={editAccountForm.email} onChange={(e) => setEditAccountForm({ ...editAccountForm, email: e.target.value })} className="mt-2" />
												</div>
												<div>
													<Label>Vai trò</Label>
													<Select value={editAccountForm.role} onValueChange={(value) => setEditAccountForm({ ...editAccountForm, role: value as UserRole, assignedDepotIds: value === "ADMIN" ? [] : editAccountForm.assignedDepotIds })}>
														<SelectTrigger className="mt-2 w-full">
															<SelectValue placeholder="Chọn vai trò" />
														</SelectTrigger>
														<SelectContent>
															<SelectItem value="DISPATCHER">Dispatcher</SelectItem>
															<SelectItem value="ADMIN">Admin</SelectItem>
														</SelectContent>
													</Select>
												</div>
												{editAccountForm.role === "DISPATCHER" && renderDepotChecklist(editAccountForm.assignedDepotIds, handleEditDepotToggle, false)}
												<div className="flex gap-3">
													<Button onClick={handleUpdateAccount} className="gap-2" disabled={isUpdatingAccount}>
														{isUpdatingAccount ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
														Lưu tài khoản
													</Button>
													<Button
														variant="outline"
														onClick={() => {
															setSelectedAccount(null);
															setEditAccountForm(EMPTY_EDIT_FORM);
														}}
													>
														Bỏ chọn
													</Button>
												</div>
											</div>
										) : (
											<div className="rounded-lg border border-dashed border-border p-8 text-center text-sm text-muted-foreground">Chưa chọn tài khoản nào.</div>
										)}
									</Card>
								</div>

								<Card className="p-6">
									<div className="flex items-center justify-between mb-6">
										<div>
											<h2 className="text-xl font-semibold text-foreground">Danh sách tài khoản</h2>
											<p className="text-sm text-muted-foreground">Nhấn vào một dòng để đưa thông tin lên khung cập nhật.</p>
										</div>
										{isLoadingAccounts && <Loader2 className="h-5 w-5 animate-spin text-primary" />}
									</div>

									<div className="overflow-x-auto">
										<table className="w-full">
											<thead className="border-b border-border text-left text-sm text-muted-foreground">
												<tr>
													<th className="pb-3 font-medium">Tài khoản</th>
													<th className="pb-3 font-medium">Vai trò</th>
													<th className="pb-3 font-medium">Kho phụ trách</th>
												</tr>
											</thead>
											<tbody className="divide-y divide-border">
												{accounts.map((account) => (
													<tr key={account.id} onClick={() => handleSelectAccount(account)} className={`cursor-pointer transition-colors hover:bg-muted/40 ${selectedAccount?.id === account.id ? "bg-muted/60" : ""}`}>
														<td className="py-4">
															<div className="font-medium text-foreground">{account.fullName}</div>
															<div className="text-sm text-muted-foreground">{account.username} · {account.email}</div>
														</td>
														<td className="py-4 text-sm text-foreground">{account.role}</td>
														<td className="py-4 text-sm text-muted-foreground">{account.assignedDepots && account.assignedDepots.length > 0 ? account.assignedDepots.map((depot) => depot.name).join(", ") : "-"}</td>
													</tr>
												))}
											</tbody>
										</table>
									</div>
								</Card>
							</div>
						)}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
