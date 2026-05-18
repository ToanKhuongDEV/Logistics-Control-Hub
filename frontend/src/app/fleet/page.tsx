"use client";

import { useState, useEffect } from "react";
import { Plus, Truck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { VehicleForm } from "@/components/vehicle-form";
import { FleetTable } from "@/components/fleet-table";
import { FleetStats } from "@/components/fleet-stats";
import { FleetFilters } from "@/components/fleet-filters";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { ExcelExportButton } from "@/components/excel-export-button";
import { Pagination } from "@/components/pagination";
import { useAuth } from "@/contexts/auth-context";
import { hasPermission } from "@/lib/auth";
import { vehicleApi } from "@/lib/vehicle-api";
import { depotApi } from "@/lib/depot-api";
import { Vehicle, VehicleRequest, VehicleStatistics, VehicleStatus } from "@/types/vehicle-types";
import { Depot } from "@/types/depot-types";
import { toast } from "sonner";
import { PageHeader } from "@/components/page-header";

const ITEMS_PER_PAGE = 10;

export default function FleetPage() {
	const { user } = useAuth();
	const canManageVehicles = hasPermission(user, "vehicle.manage");
	const canBulkReassignDepot = hasPermission(user, "vehicle.reassign");
	const [vehicles, setVehicles] = useState<Vehicle[]>([]);
	const [statistics, setStatistics] = useState<VehicleStatistics | null>(null);
	const [isFormOpen, setIsFormOpen] = useState(false);
	const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
	const [currentPage, setCurrentPage] = useState(1);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [isLoading, setIsLoading] = useState(false);
	const [isFormSubmitting, setIsFormSubmitting] = useState(false);
	const [depots, setDepots] = useState<Depot[]>([]);
	const [selectedVehicleIds, setSelectedVehicleIds] = useState<number[]>([]);
	const [bulkDepotId, setBulkDepotId] = useState("");
	const [isBulkUpdating, setIsBulkUpdating] = useState(false);

	const [searchQuery, setSearchQuery] = useState("");
	const [statusFilter, setStatusFilter] = useState<VehicleStatus | "all">("all");
	const [depotFilter, setDepotFilter] = useState("all");

	const fetchVehicles = async () => {
		setIsLoading(true);
		try {
			const response = await vehicleApi.getVehicles({
				page: currentPage - 1,
				size: ITEMS_PER_PAGE,
				status: statusFilter !== "all" ? statusFilter : undefined,
				search: searchQuery || undefined,
				depotId: depotFilter !== "all" ? Number(depotFilter) : undefined,
			});

			setVehicles(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
			setSelectedVehicleIds([]);
		} catch (error: any) {
			console.error("Error fetching vehicles:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách xe");
		} finally {
			setIsLoading(false);
		}
	};

	const fetchStatistics = async () => {
		try {
			const stats = await vehicleApi.getStatistics();
			setStatistics(stats);
		} catch (error: any) {
			console.error("Error fetching statistics:", error);
		}
	};

	const fetchDepots = async () => {
		try {
			const response = await depotApi.getDepots({ page: 0, size: 100 });
			setDepots(response.data.filter((depot) => depot.isActive));
		} catch (error: any) {
			console.error("Error fetching depots:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách kho");
		}
	};

	useEffect(() => {
		fetchVehicles();
	}, [currentPage, statusFilter, searchQuery, depotFilter]);

	useEffect(() => {
		fetchStatistics();
		fetchDepots();
	}, []);

	useEffect(() => {
		setCurrentPage(1);
	}, [searchQuery, statusFilter, depotFilter]);

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

	const handleToggleVehicleSelection = (vehicleId: number) => {
		setSelectedVehicleIds((current) => (current.includes(vehicleId) ? current.filter((id) => id !== vehicleId) : [...current, vehicleId]));
	};

	const handleBulkDepotUpdate = async () => {
		if (!bulkDepotId || selectedVehicleIds.length === 0) {
			toast.error("Hãy chọn phương tiện và kho đích cần cập nhật");
			return;
		}

		setIsBulkUpdating(true);
		try {
			await vehicleApi.updateVehiclesDepotBulk({
				vehicleIds: selectedVehicleIds,
				depotId: Number(bulkDepotId),
			});
			toast.success("Chuyển kho trực thuộc hàng loạt thành công");
			setBulkDepotId("");
			setSelectedVehicleIds([]);
			await fetchVehicles();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error bulk updating vehicle depot:", error);
			toast.error(error?.response?.data?.message || "Không thể cập nhật kho trực thuộc hàng loạt");
		} finally {
			setIsBulkUpdating(false);
		}
	};

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex flex-col h-full">
					<div className="border-b border-border bg-card">
						<div className="px-8 py-6">
							<PageHeader tag="Bảng điều khiển đội xe" icon={Truck} />
							<h1 className="text-3xl font-bold text-foreground">Quản lý đội xe</h1>
							<p className="text-muted-foreground mt-2">Dispatcher được điều phối xe trong kho của mình, admin mới được chuyển chéo kho</p>
						</div>
					</div>

					<div className="p-8 space-y-6">
						<FleetStats
							totalVehicles={statistics?.total || 0}
							activeVehicles={statistics?.active || 0}
							maintenanceVehicles={statistics?.maintenance || 0}
							idleVehicles={statistics?.idle || 0}
							averageCostPerKm={statistics?.averageCostPerKm}
							totalCapacityKg={statistics?.totalCapacityKg}
							totalCapacityM3={statistics?.totalCapacityM3}
						/>

						<FleetFilters
							searchQuery={searchQuery}
							onSearchChange={setSearchQuery}
							status={statusFilter}
							onStatusChange={setStatusFilter}
							depotId={depotFilter}
							onDepotChange={setDepotFilter}
							depots={depots}
						/>

						<div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
							<div className="flex flex-col gap-3 sm:flex-row sm:items-center">
								{canBulkReassignDepot && (
									<>
										<Select value={bulkDepotId} onValueChange={setBulkDepotId}>
											<SelectTrigger className="w-full sm:w-[260px] bg-card border-border">
												<SelectValue placeholder="Chuyển kho trực thuộc" />
											</SelectTrigger>
											<SelectContent>
												{depots.map((depot) => (
													<SelectItem key={depot.id} value={String(depot.id)}>
														{depot.name}
													</SelectItem>
												))}
											</SelectContent>
										</Select>
										<Button onClick={handleBulkDepotUpdate} disabled={selectedVehicleIds.length === 0 || !bulkDepotId || isBulkUpdating} className="gap-2">
											Chuyển kho hàng loạt
										</Button>
									</>
								)}
							</div>

							<div className="flex flex-col gap-3 sm:flex-row sm:items-center">
								<ExcelExportButton
									type="VEHICLE"
									title="Xuất danh sách xe"
									filters={{
										search: searchQuery || undefined,
										status: statusFilter !== "all" ? statusFilter : undefined,
										depotId: depotFilter !== "all" ? Number(depotFilter) : undefined,
									}}
								/>
								{canManageVehicles && (
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
								)}
							</div>
						</div>

						<div className="space-y-4">
							<FleetTable
								vehicles={vehicles}
								onEdit={handleEditVehicle}
								onDelete={handleDeleteVehicle}
								isLoading={isLoading}
								selectedVehicleIds={selectedVehicleIds}
								onToggleVehicleSelection={handleToggleVehicleSelection}
								canManage={canManageVehicles}
							/>

							{totalElements > 0 && (
								<Pagination
									currentPage={currentPage}
									totalPages={totalPages}
									itemsPerPage={ITEMS_PER_PAGE}
									totalItems={totalElements}
									onPageChange={handlePageChange}
									entityName="phương tiện"
								/>
							)}
						</div>
					</div>
				</div>

				{isFormOpen && <VehicleForm vehicle={editingVehicle || undefined} onSubmit={handleFormSubmit} onClose={handleCloseForm} isSubmitting={isFormSubmitting} />}
			</DashboardLayout>
		</ProtectedRoute>
	);
}
