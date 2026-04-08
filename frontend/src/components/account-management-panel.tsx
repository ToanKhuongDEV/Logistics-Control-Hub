"use client";

import { useEffect, useMemo, useState } from "react";
import { UserPlus, ShieldCheck, Loader2, Save, Warehouse, X, ChevronDown, ChevronUp } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { authService, User, UserRole } from "@/lib/auth";
import { depotApi } from "@/lib/depot-api";
import { Depot } from "@/types/depot-types";
import { toast } from "sonner";

const EMPTY_CREATE_FORM = {
	username: "",
	fullName: "",
	email: "",
	password: "",
	confirmPassword: "",
	role: "USER" as UserRole,
	assignedDepotIds: [] as number[],
};

const EMPTY_EDIT_FORM = {
	id: 0,
	fullName: "",
	email: "",
	role: "USER" as UserRole,
	assignedDepotIds: [] as number[],
};

export function AccountManagementPanel() {
	const [depots, setDepots] = useState<Depot[]>([]);
	const [accounts, setAccounts] = useState<User[]>([]);
	const [selectedAccount, setSelectedAccount] = useState<User | null>(null);
	const [createAccountForm, setCreateAccountForm] = useState(EMPTY_CREATE_FORM);
	const [editAccountForm, setEditAccountForm] = useState(EMPTY_EDIT_FORM);
	const [isCreatingAccount, setIsCreatingAccount] = useState(false);
	const [isUpdatingAccount, setIsUpdatingAccount] = useState(false);
	const [isLoadingAccounts, setIsLoadingAccounts] = useState(true);
	const [createDepotPicker, setCreateDepotPicker] = useState<string | undefined>(undefined);
	const [editDepotPicker, setEditDepotPicker] = useState<string | undefined>(undefined);
	const [isCreateExpanded, setIsCreateExpanded] = useState(false);

	useEffect(() => {
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
	}, []);

	const assignedDepotMap = useMemo(() => {
		const map = new Map<number, number>();
		accounts.forEach((account) => {
			account.assignedDepots?.forEach((depot) => map.set(depot.id, account.id));
		});
		return map;
	}, [accounts]);

	const createDepotOptions = useMemo(
		() => depots.filter((depot) => !assignedDepotMap.has(depot.id) || createAccountForm.assignedDepotIds.includes(depot.id)),
		[createAccountForm.assignedDepotIds, depots, assignedDepotMap]
	);

	const editDepotOptions = useMemo(
		() => depots.filter((depot) => !assignedDepotMap.has(depot.id) || assignedDepotMap.get(depot.id) === selectedAccount?.id || editAccountForm.assignedDepotIds.includes(depot.id)),
		[depots, assignedDepotMap, selectedAccount?.id, editAccountForm.assignedDepotIds]
	);

	const resetCreateForm = () => {
		setCreateAccountForm(EMPTY_CREATE_FORM);
		setCreateDepotPicker(undefined);
	};

	const refreshAccounts = async () => {
		const accountResult = await authService.getAccounts();
		setAccounts(accountResult);
		return accountResult;
	};

	const validateScopedRoleSelection = (role: UserRole, assignedDepotIds: number[]) => {
		if (role !== "ADMIN" && assignedDepotIds.length === 0) {
			toast.error("USER và Dispatcher phải được gán ít nhất một kho");
			return false;
		}
		return true;
	};

	const addDepotToCreateForm = (value: string) => {
		const depotId = Number(value);
		setCreateAccountForm((current) => ({
			...current,
			assignedDepotIds: current.assignedDepotIds.includes(depotId) ? current.assignedDepotIds : [...current.assignedDepotIds, depotId],
		}));
		setCreateDepotPicker(undefined);
	};

	const addDepotToEditForm = (value: string) => {
		const depotId = Number(value);
		setEditAccountForm((current) => ({
			...current,
			assignedDepotIds: current.assignedDepotIds.includes(depotId) ? current.assignedDepotIds : [...current.assignedDepotIds, depotId],
		}));
		setEditDepotPicker(undefined);
	};

	const removeCreateDepot = (depotId: number) => {
		setCreateAccountForm((current) => ({
			...current,
			assignedDepotIds: current.assignedDepotIds.filter((id) => id !== depotId),
		}));
	};

	const removeEditDepot = (depotId: number) => {
		setEditAccountForm((current) => ({
			...current,
			assignedDepotIds: current.assignedDepotIds.filter((id) => id !== depotId),
		}));
	};

	const handleCreateAccount = async () => {
		if (createAccountForm.password !== createAccountForm.confirmPassword) {
			toast.error("Mật khẩu xác nhận không khớp");
			return;
		}

		if (!validateScopedRoleSelection(createAccountForm.role, createAccountForm.assignedDepotIds)) {
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
				assignedDepotIds: createAccountForm.role === "ADMIN" ? [] : createAccountForm.assignedDepotIds,
			});
			resetCreateForm();
			setIsCreateExpanded(false);
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
		setEditDepotPicker(undefined);
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

		if (!validateScopedRoleSelection(editAccountForm.role, editAccountForm.assignedDepotIds)) {
			return;
		}

		setIsUpdatingAccount(true);
		try {
			const updated = await authService.updateAccount(selectedAccount.id, {
				fullName: editAccountForm.fullName,
				email: editAccountForm.email,
				role: editAccountForm.role,
				assignedDepotIds: editAccountForm.role === "ADMIN" ? [] : editAccountForm.assignedDepotIds,
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

	const renderDepotDropdown = ({
		value,
		onValueChange,
		options,
		selectedDepotIds,
		onRemove,
	}: {
		value: string | undefined;
		onValueChange: (value: string) => void;
		options: Depot[];
		selectedDepotIds: number[];
		onRemove: (depotId: number) => void;
	}) => (
		<div className="space-y-3 rounded-lg border border-border p-4">
			<div className="flex items-center gap-2 text-sm font-medium text-foreground">
				<Warehouse className="h-4 w-4 text-primary" />
				Kho phụ trách
			</div>
			<Select value={value} onValueChange={onValueChange}>
				<SelectTrigger className="w-full">
					<SelectValue placeholder="Chọn kho để gán" />
				</SelectTrigger>
				<SelectContent>
					{options.length > 0 ? (
						options.map((depot) => (
							<SelectItem key={depot.id} value={depot.id.toString()}>
								{depot.name} - {depot.city}
							</SelectItem>
						))
					) : (
						<SelectItem value="no-depot" disabled>
							Không còn kho khả dụng
						</SelectItem>
					)}
				</SelectContent>
			</Select>
			<div className="flex flex-wrap gap-2">
				{selectedDepotIds.length > 0 ? (
					selectedDepotIds.map((depotId) => {
						const depot = depots.find((item) => item.id === depotId);
						if (!depot) return null;
						return (
							<div key={depotId} className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-3 py-1 text-sm text-foreground">
								<span>{depot.name}</span>
								<button type="button" onClick={() => onRemove(depotId)} className="text-muted-foreground hover:text-foreground" aria-label={`Bỏ kho ${depot.name}`}>
									<X className="h-3.5 w-3.5" />
								</button>
							</div>
						);
					})
				) : (
					<p className="text-sm text-muted-foreground">Chưa có kho nào được gán.</p>
				)}
			</div>
		</div>
	);

	return (
		<div className="space-y-6">
			<Card className="p-6">
				<button type="button" onClick={() => setIsCreateExpanded((current) => !current)} className="flex w-full items-center justify-between gap-4 text-left">
					<div className="flex items-center gap-3">
						<div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
							<UserPlus className="h-5 w-5" />
						</div>
						<div>
							<h2 className="text-xl font-semibold text-foreground">Tạo tài khoản</h2>
							<p className="text-sm text-muted-foreground">Thu gọn để tiết kiệm không gian, mở ra khi cần thêm USER, Dispatcher hoặc Admin mới.</p>
						</div>
					</div>
					<div className="flex items-center gap-2 text-sm text-muted-foreground">
						<span>{isCreateExpanded ? "Thu gọn" : "Mở rộng"}</span>
						{isCreateExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
					</div>
				</button>

				{isCreateExpanded && (
					<div className="mt-6 border-t border-border pt-6">
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
								<Select
									value={createAccountForm.role}
									onValueChange={(value) =>
										setCreateAccountForm({
											...createAccountForm,
											role: value as UserRole,
											assignedDepotIds: value === "ADMIN" ? [] : createAccountForm.assignedDepotIds,
										})
									}
								>
									<SelectTrigger className="mt-2 w-full">
										<SelectValue placeholder="Chọn vai trò" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="USER">User</SelectItem>
										<SelectItem value="DISPATCHER">Dispatcher</SelectItem>
										<SelectItem value="ADMIN">Admin</SelectItem>
									</SelectContent>
								</Select>
							</div>
							{createAccountForm.role !== "ADMIN" && (
								<div className="md:col-span-2">
									{renderDepotDropdown({
										value: createDepotPicker,
										onValueChange: addDepotToCreateForm,
										options: createDepotOptions.filter((depot) => !createAccountForm.assignedDepotIds.includes(depot.id)),
										selectedDepotIds: createAccountForm.assignedDepotIds,
										onRemove: removeCreateDepot,
									})}
								</div>
							)}
						</div>

						<Button onClick={handleCreateAccount} className="mt-6 gap-2" disabled={isCreatingAccount}>
							{isCreatingAccount ? <Loader2 className="h-4 w-4 animate-spin" /> : <UserPlus className="h-4 w-4" />}
							Tạo tài khoản
						</Button>
					</div>
				)}
			</Card>

			<div className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
				<Card className="p-6">
					<div className="mb-6 flex items-center gap-3">
						<div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
							<ShieldCheck className="h-5 w-5" />
						</div>
						<div>
							<h2 className="text-xl font-semibold text-foreground">Cập nhật tài khoản</h2>
							<p className="text-sm text-muted-foreground">Chọn một người ở danh sách bên dưới để chỉnh vai trò và scope kho phụ trách.</p>
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
								<Select
									value={editAccountForm.role}
									onValueChange={(value) =>
										setEditAccountForm({
											...editAccountForm,
											role: value as UserRole,
											assignedDepotIds: value === "ADMIN" ? [] : editAccountForm.assignedDepotIds,
										})
									}
								>
									<SelectTrigger className="mt-2 w-full">
										<SelectValue placeholder="Chọn vai trò" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="USER">User</SelectItem>
										<SelectItem value="DISPATCHER">Dispatcher</SelectItem>
										<SelectItem value="ADMIN">Admin</SelectItem>
									</SelectContent>
								</Select>
							</div>
							{editAccountForm.role !== "ADMIN" &&
								renderDepotDropdown({
									value: editDepotPicker,
									onValueChange: addDepotToEditForm,
									options: editDepotOptions.filter((depot) => !editAccountForm.assignedDepotIds.includes(depot.id)),
									selectedDepotIds: editAccountForm.assignedDepotIds,
									onRemove: removeEditDepot,
								})}
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
										setEditDepotPicker(undefined);
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

				<Card className="p-6">
					<div className="mb-6 flex items-center justify-between">
						<div>
							<h2 className="text-xl font-semibold text-foreground">Danh sách nhân sự</h2>
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
		</div>
	);
}
