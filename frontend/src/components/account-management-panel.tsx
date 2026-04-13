"use client";

import { useEffect, useMemo, useState } from "react";
import { ChevronDown, ChevronUp, Eye, Loader2, Save, Search, ShieldCheck, Trash2, UserPlus, Warehouse, X } from "lucide-react";
import { Pagination } from "@/components/pagination";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useAuth } from "@/contexts/auth-context";
import { authService, UpdateAccountRequest, User, UserRole } from "@/lib/auth";
import { depotApi } from "@/lib/depot-api";
import { Depot } from "@/types/depot-types";
import { toast } from "sonner";

const ITEMS_PER_PAGE = 10;

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
	const { user: currentUser } = useAuth();
	const [depots, setDepots] = useState<Depot[]>([]);
	const [accounts, setAccounts] = useState<User[]>([]);
	const [accountDirectory, setAccountDirectory] = useState<User[]>([]);
	const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null);
	const [selectedAccount, setSelectedAccount] = useState<User | null>(null);
	const [createAccountForm, setCreateAccountForm] = useState(EMPTY_CREATE_FORM);
	const [editAccountForm, setEditAccountForm] = useState(EMPTY_EDIT_FORM);
	const [searchQuery, setSearchQuery] = useState("");
	const [debouncedSearchQuery, setDebouncedSearchQuery] = useState("");
	const [roleFilter, setRoleFilter] = useState<"all" | UserRole>("all");
	const [depotFilter, setDepotFilter] = useState("all");
	const [currentPage, setCurrentPage] = useState(1);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [isCreatingAccount, setIsCreatingAccount] = useState(false);
	const [isUpdatingAccount, setIsUpdatingAccount] = useState(false);
	const [isDeletingAccount, setIsDeletingAccount] = useState(false);
	const [isLoadingAccounts, setIsLoadingAccounts] = useState(true);
	const [isLoadingDetail, setIsLoadingDetail] = useState(false);
	const [createDepotPicker, setCreateDepotPicker] = useState<string | undefined>(undefined);
	const [editDepotPicker, setEditDepotPicker] = useState<string | undefined>(undefined);
	const [isCreateExpanded, setIsCreateExpanded] = useState(false);

	useEffect(() => {
		const timer = window.setTimeout(() => {
			setDebouncedSearchQuery(searchQuery.trim());
		}, 350);

		return () => window.clearTimeout(timer);
	}, [searchQuery]);

	const loadDepots = async () => {
		try {
			const response = await depotApi.getDepots({ page: 0, size: 100 });
			setDepots(response.data.filter((depot) => depot.isActive));
		} catch (error: any) {
			console.error("Error fetching depots:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách kho");
		}
	};

	const loadAccountDirectory = async () => {
		try {
			const response = await authService.getAccounts({ page: 0, size: 500 });
			setAccountDirectory(response.data);
		} catch (error) {
			console.error("Error fetching account directory:", error);
		}
	};

	const loadAccounts = async () => {
		setIsLoadingAccounts(true);
		try {
			const response = await authService.getAccounts({
				page: currentPage - 1,
				size: ITEMS_PER_PAGE,
				search: debouncedSearchQuery || undefined,
				role: roleFilter !== "all" ? roleFilter : undefined,
				depotId: depotFilter !== "all" ? Number(depotFilter) : undefined,
			});

			setAccounts(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
		} catch (error: any) {
			console.error("Error fetching accounts:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách nhân viên");
		} finally {
			setIsLoadingAccounts(false);
		}
	};

	const loadAccountDetail = async (id: number) => {
		setIsLoadingDetail(true);
		try {
			const account = await authService.getAccountById(id);
			setSelectedAccount(account);
			setEditAccountForm({
				id: account.id,
				fullName: account.fullName || "",
				email: account.email || "",
				role: account.role,
				assignedDepotIds: account.assignedDepots?.map((depot) => depot.id) || [],
			});
			setEditDepotPicker(undefined);
		} catch (error: any) {
			console.error("Error fetching account detail:", error);
			toast.error(error?.response?.data?.message || "Không thể tải chi tiết nhân viên");
			setSelectedAccount(null);
		} finally {
			setIsLoadingDetail(false);
		}
	};

	useEffect(() => {
		void loadDepots();
		void loadAccountDirectory();
	}, []);

	useEffect(() => {
		void loadAccounts();
	}, [currentPage, debouncedSearchQuery, roleFilter, depotFilter]);

	useEffect(() => {
		if (selectedAccountId == null) {
			setSelectedAccount(null);
			setEditAccountForm(EMPTY_EDIT_FORM);
			return;
		}

		void loadAccountDetail(selectedAccountId);
	}, [selectedAccountId]);

	const assignedDepotMap = useMemo(() => {
		const map = new Map<number, number>();
		accountDirectory.forEach((account) => {
			account.assignedDepots?.forEach((depot) => map.set(depot.id, account.id));
		});
		return map;
	}, [accountDirectory]);

	const isEditingAnotherAdmin = useMemo(() => !!selectedAccount && selectedAccount.role === "ADMIN" && currentUser != null && currentUser.id !== selectedAccount.id, [currentUser, selectedAccount]);

	const createDepotOptions = useMemo(() => depots.filter((depot) => !assignedDepotMap.has(depot.id) || createAccountForm.assignedDepotIds.includes(depot.id)), [createAccountForm.assignedDepotIds, depots, assignedDepotMap]);

	const editDepotOptions = useMemo(() => depots.filter((depot) => !assignedDepotMap.has(depot.id) || assignedDepotMap.get(depot.id) === selectedAccount?.id || editAccountForm.assignedDepotIds.includes(depot.id)), [depots, assignedDepotMap, selectedAccount?.id, editAccountForm.assignedDepotIds]);

	const resetCreateForm = () => {
		setCreateAccountForm(EMPTY_CREATE_FORM);
		setCreateDepotPicker(undefined);
	};

	const refreshAfterMutation = async (selectedId?: number | null) => {
		await Promise.all([loadAccounts(), loadAccountDirectory()]);
		if (selectedId) {
			setSelectedAccountId(selectedId);
			await loadAccountDetail(selectedId);
		}
	};

	const validateScopedRoleSelection = (role: UserRole, assignedDepotIds: number[]) => {
		if (role === "DISPATCHER" && assignedDepotIds.length === 0) {
			toast.error("Dispatcher phải được gán ít nhất một kho");
			return false;
		}
		return true;
	};

	const haveSameDepotIds = (left: number[], right: number[]) => {
		if (left.length !== right.length) {
			return false;
		}

		const sortedLeft = [...left].sort((a, b) => a - b);
		const sortedRight = [...right].sort((a, b) => a - b);
		return sortedLeft.every((value, index) => value === sortedRight[index]);
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
			const created = await authService.createAccount({
				username: createAccountForm.username.trim(),
				fullName: createAccountForm.fullName.trim(),
				email: createAccountForm.email.trim(),
				password: createAccountForm.password,
				role: createAccountForm.role,
				assignedDepotIds: createAccountForm.role === "DISPATCHER" ? createAccountForm.assignedDepotIds : [],
			});
			resetCreateForm();
			setIsCreateExpanded(false);
			setCurrentPage(1);
			await refreshAfterMutation(created.id);
			toast.success("Đã tạo tài khoản mới");
		} catch (error: any) {
			console.error("Create account error:", error);
			toast.error(error?.response?.data?.message || "Không thể tạo tài khoản");
		} finally {
			setIsCreatingAccount(false);
		}
	};

	const handleUpdateAccount = async () => {
		if (!selectedAccount) {
			return;
		}

		const nextFullName = editAccountForm.fullName.trim();
		const nextEmail = editAccountForm.email.trim();
		const currentAssignedDepotIds = selectedAccount.assignedDepots?.map((depot) => depot.id) || [];
		const payload: UpdateAccountRequest = {};

		if (!validateScopedRoleSelection(editAccountForm.role, editAccountForm.assignedDepotIds)) {
			return;
		}

		if (nextFullName !== (selectedAccount.fullName || "")) {
			payload.fullName = nextFullName;
		}

		if (nextEmail.toLowerCase() !== (selectedAccount.email || "").trim().toLowerCase()) {
			payload.email = nextEmail;
		}

		if (editAccountForm.role !== selectedAccount.role) {
			payload.role = editAccountForm.role;
		}

		if (!haveSameDepotIds(editAccountForm.assignedDepotIds, currentAssignedDepotIds)) {
			payload.assignedDepotIds = editAccountForm.role === "DISPATCHER" ? editAccountForm.assignedDepotIds : [];
		}

		if (payload.role === "ADMIN" && payload.assignedDepotIds === undefined) {
			payload.assignedDepotIds = [];
		}

		if (Object.keys(payload).length === 0) {
			toast.success("Không có thay đổi để cập nhật");
			return;
		}

		setIsUpdatingAccount(true);
		try {
			await authService.updateAccount(selectedAccount.id, payload);

			await refreshAfterMutation(selectedAccount.id);
			toast.success("Đã cập nhật tài khoản");
		} catch (error: any) {
			console.error("Update account error:", error);
			toast.error(error?.response?.data?.message || "Không thể cập nhật tài khoản");
		} finally {
			setIsUpdatingAccount(false);
		}
	};

	const handleDeleteAccount = async () => {
		if (!selectedAccount) {
			return;
		}

		if (!window.confirm(`Bạn có chắc chắn muốn xóa tài khoản ${selectedAccount.username}?`)) {
			return;
		}

		setIsDeletingAccount(true);
		try {
			await authService.deleteAccount(selectedAccount.id);
			setSelectedAccountId(null);
			setSelectedAccount(null);
			setEditAccountForm(EMPTY_EDIT_FORM);

			if (accounts.length === 1 && currentPage > 1) {
				setCurrentPage((page) => page - 1);
			}

			await Promise.all([loadAccounts(), loadAccountDirectory()]);
			toast.success("Đã xóa tài khoản");
		} catch (error: any) {
			console.error("Delete account error:", error);
			toast.error(error?.response?.data?.message || "Không thể xóa tài khoản");
		} finally {
			setIsDeletingAccount(false);
		}
	};

	const handleClearFilters = () => {
		setSearchQuery("");
		setRoleFilter("all");
		setDepotFilter("all");
		setCurrentPage(1);
	};

	const renderDepotDropdown = ({ value, onValueChange, options, selectedDepotIds, onRemove }: { value: string | undefined; onValueChange: (value: string) => void; options: Depot[]; selectedDepotIds: number[]; onRemove: (depotId: number) => void }) => (
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
							<p className="text-sm text-muted-foreground">Thêm nhân viên mới, chọn vai trò và gán kho ngay trong cùng một form.</p>
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
											assignedDepotIds: value === "DISPATCHER" ? createAccountForm.assignedDepotIds : [],
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
							{createAccountForm.role === "DISPATCHER" && (
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

			<Card className="p-6">
				<div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
					<div>
						<h2 className="text-xl font-semibold text-foreground">Tìm kiếm nhân viên</h2>
						<p className="text-sm text-muted-foreground">Tìm theo tên, username, email và lọc theo vai trò hoặc kho.</p>
					</div>
					<Button variant="outline" onClick={handleClearFilters}>
						Xóa bộ lọc
					</Button>
				</div>

				<div className="mt-6 grid gap-4 lg:grid-cols-[1.2fr_0.7fr_0.8fr]">
					<div>
						<Label htmlFor="accountSearch">Tìm kiếm</Label>
						<div className="relative mt-2">
							<Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
							<Input
								id="accountSearch"
								value={searchQuery}
								onChange={(e) => {
									setSearchQuery(e.target.value);
									setCurrentPage(1);
								}}
								placeholder="Nhập tên, username hoặc email"
								className="pl-9"
							/>
						</div>
					</div>
					<div>
						<Label>Vai trò</Label>
						<Select
							value={roleFilter}
							onValueChange={(value) => {
								setRoleFilter(value as "all" | UserRole);
								setCurrentPage(1);
							}}
						>
							<SelectTrigger className="mt-2 w-full">
								<SelectValue placeholder="Tất cả vai trò" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value="all">Tất cả vai trò</SelectItem>
								<SelectItem value="USER">User</SelectItem>
								<SelectItem value="DISPATCHER">Dispatcher</SelectItem>
								<SelectItem value="ADMIN">Admin</SelectItem>
							</SelectContent>
						</Select>
					</div>
					<div>
						<Label>Kho</Label>
						<Select
							value={depotFilter}
							onValueChange={(value) => {
								setDepotFilter(value);
								setCurrentPage(1);
							}}
						>
							<SelectTrigger className="mt-2 w-full">
								<SelectValue placeholder="Tất cả kho" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value="all">Tất cả kho</SelectItem>
								{depots.map((depot) => (
									<SelectItem key={depot.id} value={depot.id.toString()}>
										{depot.name}
									</SelectItem>
								))}
							</SelectContent>
						</Select>
					</div>
				</div>
			</Card>

			<div className="grid gap-6 xl:grid-cols-[2fr_1fr]">
				<Card className="overflow-hidden">
					<div className="flex items-center justify-between border-b border-border px-6 py-5">
						<div>
							<h2 className="text-xl font-semibold text-foreground">Danh sách nhân sự</h2>
							<p className="text-sm text-muted-foreground">Chọn một dòng để xem chi tiết và cập nhật nhân viên.</p>
						</div>
						{isLoadingAccounts && <Loader2 className="h-5 w-5 animate-spin text-primary" />}
					</div>

					<div className="overflow-x-auto">
						<table className="w-full">
							<thead className="border-b border-border text-left text-sm text-muted-foreground">
								<tr>
									<th className="px-6 pb-3 pt-4 font-medium">Tài khoản</th>
									<th className="pb-3 pt-4 font-medium">Vai trò</th>
									<th className="pl-4 pb-3 pt-4 font-medium">Kho phụ trách</th>
									<th className="pb-3 pt-4 pr-6 font-medium text-right">Chi tiết</th>
								</tr>
							</thead>
							<tbody className="divide-y divide-border">
								{accounts.length > 0 ? (
									accounts.map((account) => (
										<tr key={account.id} onClick={() => setSelectedAccountId(account.id)} className={`cursor-pointer transition-colors hover:bg-muted/40 ${selectedAccountId === account.id ? "bg-muted/60" : ""}`}>
											<td className="px-6 py-4">
												<div className="font-medium text-foreground">{account.fullName}</div>
												<div className="text-sm text-muted-foreground">
													{account.username} - {account.email}
												</div>
											</td>
											<td className="py-4 text-sm text-foreground">{account.role}</td>
											<td className="pl-4 py-4 text-sm text-muted-foreground">
												{account.assignedDepots && account.assignedDepots.length > 0 ? (
													<div className="space-y-1">
														{account.assignedDepots.map((depot) => (
															<div key={depot.id}>{depot.name}</div>
														))}
													</div>
												) : (
													"-"
												)}
											</td>
											<td className="py-4 pr-6 text-right">
												<Button
													type="button"
													size="sm"
													variant="ghost"
													className="gap-2"
													onClick={(event) => {
														event.stopPropagation();
														setSelectedAccountId(account.id);
													}}
												>
													<Eye className="h-4 w-4" />
													Xem
												</Button>
											</td>
										</tr>
									))
								) : (
									<tr>
										<td colSpan={4} className="px-6 py-10 text-center text-sm text-muted-foreground">
											Không có nhân viên phù hợp với bộ lọc hiện tại.
										</td>
									</tr>
								)}
							</tbody>
						</table>
					</div>

					{totalElements > 0 && <Pagination currentPage={currentPage} totalPages={totalPages} itemsPerPage={ITEMS_PER_PAGE} totalItems={totalElements} onPageChange={setCurrentPage} entityName="nhân viên" />}
				</Card>

				<Card className="p-6">
					<div className="mb-6 flex items-center gap-3">
						<div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
							<ShieldCheck className="h-5 w-5" />
						</div>
						<div>
							<h2 className="text-xl font-semibold text-foreground">Chi tiết và cập nhật</h2>
							<p className="text-sm text-muted-foreground">Xem thông tin nhân viên, sửa vai trò, gán kho hoặc xóa tài khoản.</p>
						</div>
					</div>

					{isLoadingDetail ? (
						<div className="flex items-center justify-center rounded-lg border border-dashed border-border p-8 text-sm text-muted-foreground">
							<Loader2 className="mr-2 h-4 w-4 animate-spin" />
							Đang tải chi tiết nhân viên...
						</div>
					) : selectedAccount ? (
						<div className="space-y-5">
							<div className="rounded-lg border border-border bg-muted/20 p-4">
								<div className="text-lg font-semibold text-foreground">{selectedAccount.fullName}</div>
								<div className="mt-1 text-sm text-muted-foreground">{selectedAccount.username}</div>
								<div className="mt-3 grid gap-3 sm:grid-cols-2">
									<div>
										<p className="text-xs uppercase tracking-wide text-muted-foreground">Email</p>
										<p className="text-sm text-foreground">{selectedAccount.email}</p>
									</div>
									<div>
										<p className="text-xs uppercase tracking-wide text-muted-foreground">Vai trò hiện tại</p>
										<p className="text-sm text-foreground">{selectedAccount.role}</p>
									</div>
									<div className="sm:col-span-2">
										<p className="text-xs uppercase tracking-wide text-muted-foreground">Kho đang phụ trách</p>
										<div className="text-sm text-foreground">
											{selectedAccount.assignedDepots && selectedAccount.assignedDepots.length > 0 ? (
												<div className="space-y-1">
													{selectedAccount.assignedDepots.map((depot) => (
														<div key={depot.id}>{depot.name}</div>
													))}
												</div>
											) : (
												"Chưa được gán kho"
											)}
										</div>
									</div>
								</div>
							</div>

							<div>
								<Label>Tên đăng nhập</Label>
								<Input value={selectedAccount.username} disabled className="mt-2" />
							</div>
							<div>
								<Label>Họ và tên</Label>
								<Input value={editAccountForm.fullName} onChange={(e) => setEditAccountForm({ ...editAccountForm, fullName: e.target.value })} className="mt-2" disabled={isEditingAnotherAdmin} />
							</div>
							<div>
								<Label>Email</Label>
								<Input type="email" value={editAccountForm.email} onChange={(e) => setEditAccountForm({ ...editAccountForm, email: e.target.value })} className="mt-2" disabled={isEditingAnotherAdmin} />
							</div>
							<div>
								<Label>Vai trò</Label>
								<Select
									value={editAccountForm.role}
									disabled={isEditingAnotherAdmin}
									onValueChange={(value) =>
										setEditAccountForm({
											...editAccountForm,
											role: value as UserRole,
											assignedDepotIds: value === "DISPATCHER" ? editAccountForm.assignedDepotIds : [],
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
							{isEditingAnotherAdmin && <div className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">Bạn chỉ có thể xem thông tin. Admin không được sửa hoặc xóa tài khoản admin khác.</div>}
							{editAccountForm.role === "DISPATCHER" &&
								renderDepotDropdown({
									value: editDepotPicker,
									onValueChange: addDepotToEditForm,
									options: editDepotOptions.filter((depot) => !editAccountForm.assignedDepotIds.includes(depot.id)),
									selectedDepotIds: editAccountForm.assignedDepotIds,
									onRemove: removeEditDepot,
								})}

							<div className="flex flex-wrap gap-3">
								<Button onClick={handleUpdateAccount} className="gap-2" disabled={isUpdatingAccount || isEditingAnotherAdmin}>
									{isUpdatingAccount ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
									Lưu tài khoản
								</Button>
								<Button type="button" variant="destructive" className="gap-2" onClick={handleDeleteAccount} disabled={isDeletingAccount || isEditingAnotherAdmin}>
									{isDeletingAccount ? <Loader2 className="h-4 w-4 animate-spin" /> : <Trash2 className="h-4 w-4" />}
									Xóa tài khoản
								</Button>
								<Button
									variant="outline"
									onClick={() => {
										setSelectedAccountId(null);
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
						<div className="rounded-lg border border-dashed border-border p-8 text-center text-sm text-muted-foreground">Chưa chọn tài khoản nào để xem chi tiết.</div>
					)}
				</Card>
			</div>
		</div>
	);
}
