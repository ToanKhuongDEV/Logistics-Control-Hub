"use client";

import { useState, useEffect } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DriverForm } from "@/components/driver-form";
import { DriverTable } from "@/components/driver-table";
import { DriverStats } from "@/components/driver-stats";
import { DriverFilters } from "@/components/driver-filters";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Pagination } from "@/components/pagination";
import { driverApi } from "@/lib/driver-api";
import { Driver, DriverRequest, DriverStatistics } from "@/types/driver-types";
import { toast } from "sonner";

const ITEMS_PER_PAGE = 10;

export default function DriversPage() {
	const [drivers, setDrivers] = useState<Driver[]>([]);
	const [statistics, setStatistics] = useState<DriverStatistics | null>(null);
	const [isFormOpen, setIsFormOpen] = useState(false);
	const [editingDriver, setEditingDriver] = useState<Driver | null>(null);
	const [currentPage, setCurrentPage] = useState(1);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [isLoading, setIsLoading] = useState(false);
	const [isFormSubmitting, setIsFormSubmitting] = useState(false);
	const [searchQuery, setSearchQuery] = useState("");

	const fetchDrivers = async () => {
		setIsLoading(true);
		try {
			const response = await driverApi.getDrivers({
				page: currentPage - 1,
				size: ITEMS_PER_PAGE,
				search: searchQuery || undefined,
			});

			setDrivers(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
		} catch (error: any) {
			console.error("Error fetching drivers:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách tài xế");
		} finally {
			setIsLoading(false);
		}
	};

	const fetchStatistics = async () => {
		try {
			const stats = await driverApi.getStatistics();
			setStatistics(stats);
		} catch (error) {
			console.error("Error fetching statistics:", error);
		}
	};

	useEffect(() => {
		fetchDrivers();
	}, [currentPage, searchQuery]);

	useEffect(() => {
		fetchStatistics();
	}, []);

	const handleCreate = () => {
		setEditingDriver(null);
		setIsFormOpen(true);
	};

	const handleEdit = (driver: Driver) => {
		setEditingDriver(driver);
		setIsFormOpen(true);
	};

	const handleDelete = async (id: number) => {
		if (!confirm("Bạn có chắc chắn muốn xóa tài xế này?")) {
			return;
		}

		try {
			await driverApi.delete(id);
			toast.success("Xóa tài xế thành công");
			await fetchDrivers();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error deleting driver:", error);
			toast.error(error?.response?.data?.message || "Không thể xóa tài xế");
		}
	};

	const handleFormSubmit = async (data: DriverRequest) => {
		setIsFormSubmitting(true);
		try {
			if (editingDriver) {
				await driverApi.update(editingDriver.id, data);
				toast.success("Cập nhật tài xế thành công");
			} else {
				await driverApi.create(data);
				toast.success("Thêm tài xế mới thành công");
			}
			setIsFormOpen(false);
			await fetchDrivers();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error saving driver:", error);
			const errorMessage = error?.response?.data?.message || (editingDriver ? "Không thể cập nhật tài xế" : "Không thể thêm tài xế mới");
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
							<h1 className="text-3xl font-bold text-foreground">Quản lý tài xế</h1>
							<p className="text-muted-foreground mt-2">Quản lý và theo dõi toàn bộ tài xế của công ty</p>
						</div>
					</div>

					<div className="p-8 space-y-6">
						{/* Statistics Cards */}
						<DriverStats statistics={statistics} />

						{/* Filters */}
						{/* Filters and Actions */}
						<DriverFilters searchQuery={searchQuery} onSearchChange={handleSearchChange} onClearFilters={handleClearFilters}>
							<Button onClick={handleCreate} className="bg-primary hover:bg-primary/90 text-primary-foreground gap-2">
								<Plus className="w-4 h-4" />
								Thêm tài xế mới
							</Button>
						</DriverFilters>

						{/* Table and Pagination */}
						<div className="space-y-4">
							<DriverTable drivers={drivers} onEdit={handleEdit} onDelete={handleDelete} isLoading={isLoading} />

							{totalElements > 0 && <Pagination currentPage={currentPage} totalPages={totalPages} itemsPerPage={ITEMS_PER_PAGE} totalItems={totalElements} onPageChange={setCurrentPage} entityName="tài xế" />}
						</div>

						{/* Driver Form Modal */}
						{isFormOpen && <DriverForm driver={editingDriver} onSubmit={handleFormSubmit} onClose={() => setIsFormOpen(false)} isSubmitting={isFormSubmitting} />}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
