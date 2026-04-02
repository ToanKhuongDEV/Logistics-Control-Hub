"use client";

import { useState, useEffect } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { OrderForm } from "@/components/order-form";
import { OrderTable } from "@/components/order-table";
import { OrderStats } from "@/components/order-stats";
import { OrderFilters } from "@/components/order-filters";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Pagination } from "@/components/pagination";
import { orderApi } from "@/lib/order-api";
import { depotApi } from "@/lib/depot-api";
import { Order, OrderRequest, OrderStatistics, OrderStatus } from "@/types/order-types";
import { Depot } from "@/types/depot-types";
import { toast } from "sonner";

const ITEMS_PER_PAGE = 10;

export default function OrdersPage() {
	const [orders, setOrders] = useState<Order[]>([]);
	const [statistics, setStatistics] = useState<OrderStatistics | null>(null);
	const [isFormOpen, setIsFormOpen] = useState(false);
	const [editingOrder, setEditingOrder] = useState<Order | null>(null);
	const [currentPage, setCurrentPage] = useState(1);
	const [totalPages, setTotalPages] = useState(0);
	const [totalElements, setTotalElements] = useState(0);
	const [isLoading, setIsLoading] = useState(false);
	const [isFormSubmitting, setIsFormSubmitting] = useState(false);
	const [depots, setDepots] = useState<Depot[]>([]);
	const [selectedOrderIds, setSelectedOrderIds] = useState<number[]>([]);
	const [bulkStatus, setBulkStatus] = useState<OrderStatus | "">("");
	const [isBulkUpdating, setIsBulkUpdating] = useState(false);

	const [searchQuery, setSearchQuery] = useState("");
	const [statusFilter, setStatusFilter] = useState<OrderStatus | "all">("all");
	const [depotFilter, setDepotFilter] = useState("all");
	const [sortBy, setSortBy] = useState("createdAt,desc");

	const fetchOrders = async () => {
		setIsLoading(true);
		try {
			const response = await orderApi.getOrders({
				page: currentPage - 1,
				size: ITEMS_PER_PAGE,
				status: statusFilter !== "all" ? statusFilter : undefined,
				search: searchQuery || undefined,
				depotId: depotFilter !== "all" ? Number(depotFilter) : undefined,
				sort: [sortBy],
			});

			setOrders(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
			setSelectedOrderIds([]);
		} catch (error: any) {
			console.error("Error fetching orders:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách đơn hàng");
		} finally {
			setIsLoading(false);
		}
	};

	const fetchStatistics = async () => {
		try {
			const stats = await orderApi.getStatistics();
			setStatistics(stats);
		} catch (error: any) {
			console.error("Error fetching statistics:", error);
		}
	};

	const fetchDepots = async () => {
		try {
			const response = await depotApi.getDepots({ page: 0, size: 100 });
			setDepots(response.data.filter((depot) => depot.isActive));
		} catch (error) {
			console.error("Error fetching depots:", error);
			toast.error("Không thể tải danh sách kho");
		}
	};

	useEffect(() => {
		fetchOrders();
	}, [currentPage, statusFilter, searchQuery, depotFilter, sortBy]);

	useEffect(() => {
		fetchStatistics();
		fetchDepots();
	}, []);

	useEffect(() => {
		setCurrentPage(1);
	}, [searchQuery, statusFilter, depotFilter, sortBy]);

	const handleAddOrder = async (data: OrderRequest) => {
		setIsFormSubmitting(true);
		try {
			await orderApi.createOrder(data);
			toast.success("Thêm đơn hàng mới thành công");
			setIsFormOpen(false);
			await fetchOrders();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error creating order:", error);
			toast.error(error?.response?.data?.message || "Không thể thêm đơn hàng mới");
		} finally {
			setIsFormSubmitting(false);
		}
	};

	const handleEditOrder = (order: Order) => {
		setEditingOrder(order);
		setIsFormOpen(true);
	};

	const handleUpdateOrder = async (data: OrderRequest) => {
		if (!editingOrder) return;

		setIsFormSubmitting(true);
		try {
			await orderApi.updateOrder(editingOrder.id, data);
			toast.success("Cập nhật đơn hàng thành công");
			setIsFormOpen(false);
			setEditingOrder(null);
			await fetchOrders();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error updating order:", error);
			toast.error(error?.response?.data?.message || "Không thể cập nhật đơn hàng");
		} finally {
			setIsFormSubmitting(false);
		}
	};

	const handleDeleteOrder = async (id: number) => {
		try {
			await orderApi.deleteOrder(id);
			toast.success("Xóa đơn hàng thành công");
			await fetchOrders();
			await fetchStatistics();

			if (orders.length === 1 && currentPage > 1) {
				setCurrentPage(currentPage - 1);
			}
		} catch (error: any) {
			console.error("Error deleting order:", error);
			toast.error(error?.response?.data?.message || "Không thể xóa đơn hàng");
		}
	};

	const handleCloseForm = () => {
		setIsFormOpen(false);
		setEditingOrder(null);
	};

	const handleFormSubmit = (data: OrderRequest) => {
		if (editingOrder) {
			handleUpdateOrder(data);
		} else {
			handleAddOrder(data);
		}
	};

	const handlePageChange = (page: number) => {
		setCurrentPage(page);
	};

	const handleToggleOrderSelection = (orderId: number) => {
		setSelectedOrderIds((current) => (current.includes(orderId) ? current.filter((id) => id !== orderId) : [...current, orderId]));
	};

	const handleBulkStatusUpdate = async () => {
		if (!bulkStatus || selectedOrderIds.length === 0) {
			toast.error("Hãy chọn đơn hàng và trạng thái cần cập nhật");
			return;
		}

		setIsBulkUpdating(true);
		try {
			await orderApi.updateOrdersStatusBulk({
				orderIds: selectedOrderIds,
				status: bulkStatus,
			});
			toast.success("Cập nhật trạng thái hàng loạt thành công");
			setBulkStatus("");
			setSelectedOrderIds([]);
			await fetchOrders();
			await fetchStatistics();
		} catch (error: any) {
			console.error("Error bulk updating order status:", error);
			toast.error(error?.response?.data?.message || "Không thể cập nhật trạng thái hàng loạt");
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
							<h1 className="text-3xl font-bold text-foreground">Quản lý đơn hàng</h1>
							<p className="text-muted-foreground mt-2">Quản lý và theo dõi toàn bộ đơn hàng của công ty</p>
						</div>
					</div>

					<div className="p-8 space-y-6">
						<OrderStats totalOrders={statistics?.total || 0} pendingOrders={statistics?.pending || 0} inTransitOrders={statistics?.inTransit || 0} />

						<OrderFilters
							searchQuery={searchQuery}
							onSearchChange={setSearchQuery}
							status={statusFilter}
							onStatusChange={setStatusFilter}
							depotId={depotFilter}
							onDepotChange={setDepotFilter}
							sortBy={sortBy}
							onSortByChange={setSortBy}
							depots={depots}
						/>

						<div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
							<div className="flex flex-col gap-3 sm:flex-row sm:items-center">
									<Select value={bulkStatus} onValueChange={(value) => setBulkStatus(value as OrderStatus | "")}>
										<SelectTrigger className="w-full sm:w-[220px] bg-card border-border">
											<SelectValue placeholder="Cập nhật trạng thái" />
										</SelectTrigger>
										<SelectContent>
											<SelectItem value={OrderStatus.CREATED}>Đã tạo</SelectItem>
											<SelectItem value={OrderStatus.IN_TRANSIT}>Đang giao</SelectItem>
											<SelectItem value={OrderStatus.DELIVERED}>Đã giao</SelectItem>
											<SelectItem value={OrderStatus.CANCELLED}>Đã hủy</SelectItem>
										</SelectContent>
									</Select>
									<Button onClick={handleBulkStatusUpdate} disabled={selectedOrderIds.length === 0 || !bulkStatus || isBulkUpdating} className="gap-2">
										Cập nhật hàng loạt
									</Button>
								</div>

							<Button onClick={() => setIsFormOpen(true)} className="bg-primary hover:bg-primary/90 text-primary-foreground gap-2">
								<Plus className="w-4 h-4" />
								Thêm đơn hàng mới
							</Button>
						</div>

						<div className="space-y-4">
							<OrderTable
								orders={orders}
								onEdit={handleEditOrder}
								onDelete={handleDeleteOrder}
								isLoading={isLoading}
								selectedOrderIds={selectedOrderIds}
								onToggleOrderSelection={handleToggleOrderSelection}
							/>

							{totalElements > 0 && (
								<Pagination
									currentPage={currentPage}
									totalPages={totalPages}
									itemsPerPage={ITEMS_PER_PAGE}
									totalItems={totalElements}
									onPageChange={handlePageChange}
									entityName="đơn hàng"
								/>
							)}
						</div>

						{isFormOpen && <OrderForm order={editingOrder || undefined} onSubmit={handleFormSubmit} onClose={handleCloseForm} isSubmitting={isFormSubmitting} />}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
