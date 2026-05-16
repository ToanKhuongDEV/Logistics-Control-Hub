"use client";

import { useState, useEffect } from "react";
import { Plus, Warehouse } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DepotForm } from "@/components/depot-form";
import { DepotTable } from "@/components/depot-table";
import { DepotStats } from "@/components/depot-stats";
import { DepotFilters } from "@/components/depot-filters";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { ExcelExportButton } from "@/components/excel-export-button";
import { Pagination } from "@/components/pagination";
import { useAuth } from "@/contexts/auth-context";
import { hasPermission } from "@/lib/auth";
import { depotApi } from "@/lib/depot-api";
import { Depot, DepotRequest, DepotStatistics } from "@/types/depot-types";
import { toast } from "sonner";
import { PageHeader } from "@/components/page-header";

const ITEMS_PER_PAGE = 10;

export default function DepotsPage() {
	const { user } = useAuth();
	const canManageDepots = hasPermission(user, "depot.manage");
	const [depots, setDepots] = useState<Depot[]>([]);
	const [statistics, setStatistics] = useState<DepotStatistics | null>(null);
	const [isFormOpen, setIsFormOpen] = useState(false);
	const [editingDepot, setEditingDepot] = useState<Depot | null>(null);
	const [currentPage, setCurrentPage] = useState(1);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [isLoading, setIsLoading] = useState(false);
	const [isFormSubmitting, setIsFormSubmitting] = useState(false);
	const [searchQuery, setSearchQuery] = useState("");

	const fetchDepots = async () => {
		setIsLoading(true);
		try {
			const response = await depotApi.getDepots({
				page: currentPage - 1,
				size: ITEMS_PER_PAGE,
				search: searchQuery || undefined,
			});

			setDepots(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
		} catch (error: any) {
			console.error("Error fetching depots:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách kho");
		} finally {
			setIsLoading(false);
		}
	};

	const fetchStatistics = async () => {
		try {
			const stats = await depotApi.getStatistics();
			setStatistics(stats);
		} catch (error) {
			console.error("Error fetching statistics:", error);
		}
	};

	useEffect(() => {
		fetchDepots();
	}, [currentPage, searchQuery]);

	useEffect(() => {
		fetchStatistics();
	}, []);

	const handleCreate = () => {
		setEditingDepot(null);
		setIsFormOpen(true);
	};

	const handleEdit = (depot: Depot) => {
		setEditingDepot(depot);
		setIsFormOpen(true);
	};

	const handleDelete = async (id: number) => {
		if (!confirm("Bạn có chắc chắn muốn xóa kho này?")) {
			return;
		}

		try {
			await depotApi.deleteDepot(id);
			toast.success("Xóa kho thành công");
			await fetchDepots();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error deleting depot:", error);
			toast.error(error?.response?.data?.message || "Không thể xóa kho");
		}
	};

	const handleFormSubmit = async (data: DepotRequest) => {
		setIsFormSubmitting(true);
		try {
			if (editingDepot) {
				await depotApi.updateDepot(editingDepot.id, data);
				toast.success("Cập nhật kho thành công");
			} else {
				await depotApi.createDepot(data);
				toast.success("Thêm kho mới thành công");
			}
			setIsFormOpen(false);
			await fetchDepots();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error saving depot:", error);
			const errorMessage = error?.response?.data?.message || (editingDepot ? "Không thể cập nhật kho" : "Không thể thêm kho mới");
			toast.error(errorMessage);
			throw error;
		} finally {
			setIsFormSubmitting(false);
		}
	};

	const handleSearchChange = (value: string) => {
		setSearchQuery(value);
		setCurrentPage(1);
	};

	const handleClearFilters = () => {
		setSearchQuery("");
		setCurrentPage(1);
	};

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex flex-col h-full">
					<div className="border-b border-border bg-card">
						<div className="px-8 py-6">
							<PageHeader tag="Bảng điều khiển kho" icon={Warehouse} />
							<h1 className="text-3xl font-bold text-foreground">Quản lý kho</h1>
							<p className="text-muted-foreground mt-2">Danh sách kho theo phạm vi được phân công</p>
						</div>
					</div>

					<div className="p-8 space-y-6">
						<DepotStats statistics={statistics} />

						<DepotFilters searchQuery={searchQuery} onSearchChange={handleSearchChange} onClearFilters={handleClearFilters}>
							<ExcelExportButton
								type="DEPOT"
								title="Xuất danh sách kho"
								filters={{
									search: searchQuery || undefined,
								}}
							/>
							{canManageDepots && (
								<Button onClick={handleCreate} className="bg-primary hover:bg-primary/90 text-primary-foreground gap-2">
									<Plus className="w-4 h-4" />
									Thêm kho mới
								</Button>
							)}
						</DepotFilters>

						<div className="space-y-4">
							<DepotTable depots={depots} onEdit={handleEdit} onDelete={handleDelete} isLoading={isLoading} canManage={canManageDepots} />

							{totalElements > 0 && <Pagination currentPage={currentPage} totalPages={totalPages} itemsPerPage={ITEMS_PER_PAGE} totalItems={totalElements} onPageChange={setCurrentPage} entityName="kho" />}
						</div>

						{isFormOpen && <DepotForm depot={editingDepot} onSubmit={handleFormSubmit} onClose={() => setIsFormOpen(false)} isSubmitting={isFormSubmitting} />}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
