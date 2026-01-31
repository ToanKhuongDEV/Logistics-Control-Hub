"use client";

import { useState, useEffect } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { OrderForm } from "@/components/order-form";
import { OrderTable } from "@/components/order-table";
import { OrderStats } from "@/components/order-stats";
import { OrderFilters } from "@/components/order-filters";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Pagination } from "@/components/pagination";
import { orderApi } from "@/lib/order-api";
import { Order, OrderRequest, OrderStatistics, OrderStatus } from "@/types/order-types";
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

	// Filter states
	const [searchQuery, setSearchQuery] = useState("");
	const [statusFilter, setStatusFilter] = useState<OrderStatus | "all">("all");

	// Fetch orders
	const fetchOrders = async () => {
		setIsLoading(true);
		try {
			const response = await orderApi.getOrders({
				page: currentPage - 1, // Backend uses 0-based indexing
				size: ITEMS_PER_PAGE,
				status: statusFilter !== "all" ? statusFilter : undefined,
				search: searchQuery || undefined,
			});

			setOrders(response.data);
			setTotalPages(response.pagination.totalPages);
			setTotalElements(response.pagination.totalElements);
		} catch (error: any) {
			console.error("Error fetching orders:", error);
			toast.error(error?.response?.data?.message || "Không thể tải danh sách đơn hàng");
		} finally {
			setIsLoading(false);
		}
	};

	// Fetch statistics
	const fetchStatistics = async () => {
		try {
			const stats = await orderApi.getStatistics();
			setStatistics(stats);
		} catch (error: any) {
			console.error("Error fetching statistics:", error);
		}
	};

	// Initial load and when filters/page change
	useEffect(() => {
		fetchOrders();
	}, [currentPage, statusFilter, searchQuery]);

	// Fetch statistics on mount and after CRUD operations
	useEffect(() => {
		fetchStatistics();
	}, []);

	// Reset to page 1 when filters change
	useEffect(() => {
		setCurrentPage(1);
	}, [searchQuery, statusFilter]);

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

			// Reset to page 1 if current page becomes empty after deletion
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
						{/* Statistics Cards */}
						<OrderStats totalOrders={statistics?.total || 0} pendingOrders={statistics?.pending || 0} inTransitOrders={statistics?.inTransit || 0} />
						{/* Filters */}
						<OrderFilters searchQuery={searchQuery} onSearchChange={setSearchQuery} status={statusFilter} onStatusChange={setStatusFilter} />

						{/* Actions */}
						<div className="flex items-center justify-end">
							<Button onClick={() => setIsFormOpen(true)} className="bg-primary hover:bg-primary/90 text-primary-foreground gap-2">
								<Plus className="w-4 h-4" />
								Thêm đơn hàng mới
							</Button>
						</div>

						{/* Table and Pagination */}
						<div className="space-y-4">
							<OrderTable orders={orders} onEdit={handleEditOrder} onDelete={handleDeleteOrder} isLoading={isLoading} />

							{totalElements > 0 && <Pagination currentPage={currentPage} totalPages={totalPages} itemsPerPage={ITEMS_PER_PAGE} totalItems={totalElements} onPageChange={handlePageChange} entityName="đơn hàng" />}
						</div>

						{/* Order Form Modal */}
						{isFormOpen && <OrderForm order={editingOrder || undefined} onSubmit={handleFormSubmit} onClose={handleCloseForm} isSubmitting={isFormSubmitting} />}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
