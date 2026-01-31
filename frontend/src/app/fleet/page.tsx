"use client";

import { useState, useEffect, useMemo } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { VehicleForm } from "@/components/vehicle-form";
import { FleetTable } from "@/components/fleet-table";
import { FleetStats } from "@/components/fleet-stats";
import { FleetFilters } from "@/components/fleet-filters";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Pagination } from "@/components/pagination";
import { vehicleApi } from "@/lib/vehicle-api";
import { Vehicle, VehicleRequest, VehicleStatistics, VehicleStatus } from "@/types/vehicle-types";
import { toast } from "sonner";

const ITEMS_PER_PAGE = 10;

export default function FleetPage() {
	const [vehicles, setVehicles] = useState<Vehicle[]>([]);
	const [statistics, setStatistics] = useState<VehicleStatistics | null>(null);
	const [isFormOpen, setIsFormOpen] = useState(false);
	const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
	const [currentPage, setCurrentPage] = useState(1);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [isLoading, setIsLoading] = useState(false);
	const [isFormSubmitting, setIsFormSubmitting] = useState(false);

	// Filter states
	const [searchQuery, setSearchQuery] = useState("");
	const [statusFilter, setStatusFilter] = useState<VehicleStatus | "all">("all");

	// Fetch vehicles
	const fetchVehicles = async () => {
		setIsLoading(true);
		try {
			const response = await vehicleApi.getVehicles({
				page: currentPage - 1, // Backend uses 0-based indexing
				size: ITEMS_PER_PAGE,
				status: statusFilter !== "all" ? statusFilter : undefined,
				search: searchQuery || undefined,
			});

			setVehicles(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
		} catch (error: any) {
			console.error("Error fetching vehicles:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách xe");
		} finally {
			setIsLoading(false);
		}
	};

	// Fetch statistics
	const fetchStatistics = async () => {
		try {
			const stats = await vehicleApi.getStatistics();
			setStatistics(stats);
		} catch (error: any) {
			console.error("Error fetching statistics:", error);
		}
	};

	// Initial load and when filters/page change
	useEffect(() => {
		fetchVehicles();
	}, [currentPage, statusFilter, searchQuery]);

	// Fetch statistics on mount and after CRUD operations
	useEffect(() => {
		fetchStatistics();
	}, []);

	// Reset to page 1 when filters change
	useEffect(() => {
		setCurrentPage(1);
	}, [searchQuery, statusFilter]);

	const handleAddVehicle = async (data: VehicleRequest) => {
		setIsFormSubmitting(true);
		try {
			await vehicleApi.createVehicle(data);
			toast.success("Thêm xe mới thành công");
			setIsFormOpen(false);
			await fetchVehicles();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error creating vehicle:", error);
			toast.error(error?.response?.data?.message || "Không thể thêm xe mới");
		} finally {
			setIsFormSubmitting(false);
		}
	};

	const handleEditVehicle = (vehicle: Vehicle) => {
		setEditingVehicle(vehicle);
		setIsFormOpen(true);
	};

	const handleUpdateVehicle = async (data: VehicleRequest) => {
		if (!editingVehicle) return;

		setIsFormSubmitting(true);
		try {
			await vehicleApi.updateVehicle(editingVehicle.id, data);
			toast.success("Cập nhật xe thành công");
			setIsFormOpen(false);
			setEditingVehicle(null);
			await fetchVehicles();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error updating vehicle:", error);
			toast.error(error?.response?.data?.message || "Không thể cập nhật xe");
		} finally {
			setIsFormSubmitting(false);
		}
	};

	const handleDeleteVehicle = async (id: number) => {
		try {
			await vehicleApi.deleteVehicle(id);
			toast.success("Xóa xe thành công");
			await fetchVehicles();
			await fetchStatistics();

			// Reset to page 1 if current page becomes empty after deletion
			if (vehicles.length === 1 && currentPage > 1) {
				setCurrentPage(currentPage - 1);
			}
		} catch (error: any) {
			console.error("Error deleting vehicle:", error);
			toast.error(error?.response?.data?.message || "Không thể xóa xe");
		}
	};

	const handleCloseForm = () => {
		setIsFormOpen(false);
		setEditingVehicle(null);
	};

	const handleFormSubmit = (data: VehicleRequest) => {
		if (editingVehicle) {
			handleUpdateVehicle(data);
		} else {
			handleAddVehicle(data);
		}
	};

	const handlePageChange = (page: number) => {
		setCurrentPage(page);
	};

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex flex-col h-full">
					<div className="border-b border-border bg-card">
						<div className="px-8 py-6">
							<h1 className="text-3xl font-bold text-foreground">Quản lý đội xe</h1>
							<p className="text-muted-foreground mt-2">Quản lý và theo dõi toàn bộ đội xe của công ty</p>
						</div>
					</div>

					<div className="p-8 space-y-6">
						{/* Statistics Cards */}
						<FleetStats
							totalVehicles={statistics?.total || 0}
							activeVehicles={statistics?.active || 0}
							maintenanceVehicles={statistics?.maintenance || 0}
							idleVehicles={statistics?.idle || 0}
							averageCostPerKm={statistics?.averageCostPerKm}
							totalCapacityKg={statistics?.totalCapacityKg}
							totalCapacityM3={statistics?.totalCapacityM3}
						/>

						{/* Filters */}
						<FleetFilters searchQuery={searchQuery} onSearchChange={setSearchQuery} status={statusFilter} onStatusChange={setStatusFilter} />

						{/* Actions */}
						<div className="flex items-center justify-end">
							<Button
								onClick={() => {
									setEditingVehicle(null);
									setIsFormOpen(true);
								}}
								className="bg-primary hover:bg-primary/90 text-primary-foreground gap-2"
							>
								<Plus className="w-4 h-4" />
								Thêm xe mới
							</Button>
						</div>

						{/* Table and Pagination */}
						<div className="space-y-4">
							<FleetTable vehicles={vehicles} onEdit={handleEditVehicle} onDelete={handleDeleteVehicle} isLoading={isLoading} />

							{totalElements > 0 && <Pagination currentPage={currentPage} totalPages={totalPages} itemsPerPage={ITEMS_PER_PAGE} totalItems={totalElements} onPageChange={handlePageChange} entityName="phương tiện" />}
						</div>
					</div>
				</div>

				{isFormOpen && <VehicleForm vehicle={editingVehicle || undefined} onSubmit={handleFormSubmit} onClose={handleCloseForm} isSubmitting={isFormSubmitting} />}
			</DashboardLayout>
		</ProtectedRoute>
	);
}
