"use client";

import dynamic from "next/dynamic";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
	ArrowRight,
	CalendarClock,
	CheckCircle2,
	ClipboardList,
	Clock3,
	LogOut,
	MapPin,
	Navigation,
	PackageCheck,
	PackageOpen,
	RefreshCw,
	Route as RouteIcon,
	ShieldAlert,
	Truck,
	User,
} from "lucide-react";
import { toast } from "sonner";

import { Logo } from "@/components/logo";
import { ProtectedRoute } from "@/components/protected-route";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/contexts/auth-context";
import { driverPortalApi, DriverDeliveryOrder } from "@/lib/driver-portal-api";
import { hasPermission } from "@/lib/auth";
import { cn } from "@/lib/utils";
import { Route, RoutingRun } from "@/lib/routing-api";
import { OrderStatus } from "@/types/order-types";

const LeafletMap = dynamic(() => import("@/components/leaflet-map").then((mod) => mod.LeafletMap), {
	ssr: false,
	loading: () => <div className="flex h-96 w-full items-center justify-center rounded-lg border border-border bg-muted/20 text-sm text-muted-foreground">Đang tải bản đồ...</div>,
});

type DriverMobileView = "orders" | "route" | "history";

function formatDateTime(value?: string | null) {
	if (!value) return "--";
	try {
		return new Intl.DateTimeFormat("vi-VN", {
			day: "2-digit",
			month: "2-digit",
			year: "numeric",
			hour: "2-digit",
			minute: "2-digit",
		}).format(new Date(value));
	} catch {
		return value;
	}
}

function formatNumber(value?: number | null, digits = 0) {
	if (value == null) return "--";
	return new Intl.NumberFormat("vi-VN", {
		maximumFractionDigits: digits,
		minimumFractionDigits: digits,
	}).format(Number(value));
}

function statusLabel(status?: string | null) {
	if (status === OrderStatus.IN_TRANSIT) return "Đang giao";
	if (status === OrderStatus.DELIVERED) return "Đã giao";
	if (status === OrderStatus.CANCELLED) return "Đã hủy";
	if (status === OrderStatus.CREATED) return "Đã tạo";
	return status || "--";
}

function routeStatusLabel(status?: string | null) {
	if (status === "COMPLETED") return "Hoàn thành";
	if (status === "IN_PROGRESS") return "Đang chạy";
	if (status === "CREATED") return "Đã tạo";
	if (status === "CANCELLED") return "Đã hủy";
	return status || "--";
}

export default function DriverPage() {
	const { user, logout } = useAuth();
	const [orders, setOrders] = useState<DriverDeliveryOrder[]>([]);
	const [latestRun, setLatestRun] = useState<RoutingRun | null>(null);
	const [historyRuns, setHistoryRuns] = useState<RoutingRun[]>([]);
	const [focusedRun, setFocusedRun] = useState<RoutingRun | null>(null);
	const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
	const [isLoading, setIsLoading] = useState(true);
	const [isRefreshing, setIsRefreshing] = useState(false);
	const [isLoadingRun, setIsLoadingRun] = useState(false);
	const [completingOrderId, setCompletingOrderId] = useState<number | null>(null);
	const [mobileView, setMobileView] = useState<DriverMobileView>("orders");

	const canUseDriverPortal = hasPermission(user, "driver.delivery.read");

	const selectedOrder = useMemo(() => orders.find((order) => order.id === selectedOrderId) ?? orders[0] ?? null, [orders, selectedOrderId]);
	const activeRun = focusedRun ?? latestRun;
	const routes = activeRun?.routes ?? [];
	const selectedRoute = useMemo(() => {
		if (!routes.length) return null;
		if (selectedOrder?.routeId != null) {
			return routes.find((route) => route.id === selectedOrder.routeId) ?? routes[0];
		}
		return routes[0];
	}, [routes, selectedOrder?.routeId]);
	const routeStops = useMemo(() => [...(selectedRoute?.stops ?? [])].sort((a, b) => a.stopSequence - b.stopSequence), [selectedRoute]);
	const totalDistance = routes.reduce((sum, route) => sum + Number(route.totalDistanceKm ?? 0), 0);
	const remainingStops = orders.filter((order) => order.status === OrderStatus.IN_TRANSIT).length;

	const loadDriverData = useCallback(async (showSpinner = true) => {
		if (showSpinner) {
			setIsLoading(true);
		} else {
			setIsRefreshing(true);
		}

		const [ordersResult, latestResult, historyResult] = await Promise.allSettled([
			driverPortalApi.getMyOrders({ page: 0, size: 50 }),
			driverPortalApi.getMyLatestRoutingRun(),
			driverPortalApi.getMyRoutingHistory(0, 10),
		]);

		if (ordersResult.status === "fulfilled") {
			const nextOrders = ordersResult.value.data;
			setOrders(nextOrders);
			setSelectedOrderId((current) => (current && nextOrders.some((order) => order.id === current) ? current : nextOrders[0]?.id ?? null));
		} else {
			toast.error("Không thể tải đơn giao hàng của tài xế");
		}

		if (latestResult.status === "fulfilled") {
			setLatestRun(latestResult.value);
			setFocusedRun((current) => current ?? latestResult.value);
		}

		if (historyResult.status === "fulfilled") {
			setHistoryRuns(historyResult.value.content);
		}

		setIsLoading(false);
		setIsRefreshing(false);
	}, []);

	useEffect(() => {
		if (canUseDriverPortal) {
			void loadDriverData();
		} else {
			setIsLoading(false);
		}
	}, [canUseDriverPortal, loadDriverData]);

	const handleCompleteOrder = async (orderId: number) => {
		setCompletingOrderId(orderId);
		try {
			await driverPortalApi.completeMyOrder(orderId);
			toast.success("Đã hoàn thành đơn giao hàng");
			await loadDriverData(false);
		} catch (error: any) {
			toast.error(error?.response?.data?.message || "Không thể hoàn thành đơn giao hàng");
		} finally {
			setCompletingOrderId(null);
		}
	};

	const handleSelectRun = async (run: RoutingRun) => {
		setFocusedRun(run);
		setIsLoadingRun(true);
		try {
			const detail = await driverPortalApi.getMyRoutingRun(run.id);
			setFocusedRun(detail);
			setMobileView("route");
		} catch {
			setMobileView("route");
		 } finally {
			setIsLoadingRun(false);
		}
	};

	return (
		<ProtectedRoute>
			{canUseDriverPortal ? (
				<div className="min-h-screen bg-muted/30 text-foreground">
					{false && (
					<DriverDesktopView
						userName={user?.fullName || user?.username || "Tài xế"}
						userEmail={user?.email}
						orders={orders}
						selectedOrder={selectedOrder}
						selectedRoute={selectedRoute}
						routeStops={routeStops}
						activeRun={activeRun}
						historyRuns={historyRuns}
						totalDistance={totalDistance}
						remainingStops={remainingStops}
						isLoading={isLoading}
						isRefreshing={isRefreshing}
						isLoadingRun={isLoadingRun}
						completingOrderId={completingOrderId}
						onRefresh={() => void loadDriverData(false)}
						onLogout={logout}
						onSelectOrder={(order) => setSelectedOrderId(order.id)}
						onCompleteOrder={(orderId) => void handleCompleteOrder(orderId)}
						onSelectRun={(run) => void handleSelectRun(run)}
					/>
					)}
					<DriverMobileView
						userName={user?.fullName || user?.username || "Tài xế"}
						orders={orders}
						selectedOrder={selectedOrder}
						selectedRoute={selectedRoute}
						routeStops={routeStops}
						activeRun={activeRun}
						historyRuns={historyRuns}
						remainingStops={remainingStops}
						totalDistance={totalDistance}
						isLoading={isLoading}
						isRefreshing={isRefreshing}
						isLoadingRun={isLoadingRun}
						completingOrderId={completingOrderId}
						mobileView={mobileView}
						onMobileViewChange={setMobileView}
						onRefresh={() => void loadDriverData(false)}
						onLogout={logout}
						onSelectOrder={(order) => {
							setSelectedOrderId(order.id);
							setMobileView("orders");
						}}
						onCompleteOrder={(orderId) => void handleCompleteOrder(orderId)}
						onSelectRun={(run) => void handleSelectRun(run)}
					/>
				</div>
			) : (
				<DriverAccessDenied />
			)}
		</ProtectedRoute>
	);
}

interface SharedDriverViewProps {
	userName: string;
	orders: DriverDeliveryOrder[];
	selectedOrder: DriverDeliveryOrder | null;
	selectedRoute: Route | null;
	routeStops: Route["stops"];
	activeRun: RoutingRun | null;
	historyRuns: RoutingRun[];
	totalDistance: number;
	remainingStops: number;
	isLoading: boolean;
	isRefreshing: boolean;
	isLoadingRun: boolean;
	completingOrderId: number | null;
	onRefresh: () => void;
	onLogout: () => void;
	onSelectOrder: (order: DriverDeliveryOrder) => void;
	onCompleteOrder: (orderId: number) => void;
	onSelectRun: (run: RoutingRun) => void;
}

function DriverDesktopView(props: SharedDriverViewProps & { userEmail?: string }) {
	const {
		userName,
		userEmail,
		orders,
		selectedOrder,
		selectedRoute,
		routeStops,
		activeRun,
		historyRuns,
		totalDistance,
		remainingStops,
		isLoading,
		isRefreshing,
		isLoadingRun,
		completingOrderId,
		onRefresh,
		onLogout,
		onSelectOrder,
		onCompleteOrder,
		onSelectRun,
	} = props;

	return (
		<div className="hidden">
			<aside className="flex w-72 flex-col border-r border-border bg-sidebar">
				<div className="border-b border-sidebar-border p-6">
					<Logo />
				</div>
				<div className="flex-1 space-y-6 p-5">
					<div className="rounded-lg border border-sidebar-border bg-sidebar-accent/40 p-4">
						<div className="flex items-center gap-3">
							<div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary text-primary-foreground">
								<User className="h-5 w-5" />
							</div>
							<div className="min-w-0">
								<p className="truncate text-sm font-semibold text-sidebar-foreground">{userName}</p>
								<p className="truncate text-xs text-sidebar-foreground/60">{userEmail || "Driver Portal"}</p>
							</div>
						</div>
					</div>
					<nav className="space-y-2">
						<DriverNavItem active icon={ClipboardList} label="Ca giao" />
						<DriverNavItem icon={RouteIcon} label="Tuyến đường" />
						<DriverNavItem icon={CalendarClock} label="Lịch sử" />
					</nav>
				</div>
				<div className="border-t border-sidebar-border p-5">
					<Button onClick={onLogout} variant="outline" className="w-full justify-start border-sidebar-border text-sidebar-foreground" title="Đăng xuất">
						<LogOut className="h-4 w-4" />
						Đăng xuất
					</Button>
				</div>
			</aside>

			<main className="flex-1 overflow-auto">
				<div className="mx-auto max-w-7xl space-y-6 p-8">
					<div className="flex items-start justify-between gap-4">
						<div>
							<div className="mb-3 inline-flex items-center gap-2 rounded-lg border border-emerald-500/20 bg-emerald-500/10 px-3 py-1 text-xs font-semibold text-emerald-700">
								<Truck className="h-3.5 w-3.5" />
								Driver Portal
							</div>
							<h1 className="text-3xl font-bold">Ca giao hôm nay</h1>
							<p className="mt-1 text-sm text-muted-foreground">{formatDateTime(new Date().toISOString())}</p>
						</div>
						<Button onClick={onRefresh} variant="outline" disabled={isRefreshing}>
							<RefreshCw className={cn("h-4 w-4", isRefreshing && "animate-spin")} />
							Làm mới
						</Button>
					</div>

					<div className="grid grid-cols-3 gap-4">
						<MetricTile icon={PackageOpen} label="Đơn đang giao" value={String(remainingStops)} tone="amber" />
						<MetricTile icon={RouteIcon} label="Số tuyến" value={String(activeRun?.routes?.length ?? 0)} tone="sky" />
						<MetricTile icon={Navigation} label="Tổng km" value={formatNumber(totalDistance, 1)} tone="emerald" />
					</div>

					<div className="grid grid-cols-12 gap-6">
						<section className="col-span-4 overflow-hidden rounded-lg border border-border bg-card">
							<SectionHeader title="Danh sách đơn" helper={`${orders.length} đơn`} />
							<OrderList orders={orders} selectedOrder={selectedOrder} isLoading={isLoading} onSelectOrder={onSelectOrder} />
						</section>

						<section className="col-span-4 overflow-hidden rounded-lg border border-border bg-card">
							<SectionHeader title="Chi tiết giao hàng" helper={selectedOrder?.code || "--"} />
							<OrderDetail order={selectedOrder} completingOrderId={completingOrderId} onCompleteOrder={onCompleteOrder} />
						</section>

						<section className="col-span-4 overflow-hidden rounded-lg border border-border bg-card">
							<SectionHeader title="Tuyến hiện tại" helper={selectedRoute ? `#${selectedRoute.id}` : "--"} />
							<RouteSummary route={selectedRoute} stops={routeStops} />
						</section>
					</div>

					<div className="grid grid-cols-12 gap-6">
						<section className="col-span-8 overflow-hidden rounded-lg border border-border bg-card">
							<SectionHeader title="Bản đồ tuyến" helper={routeStatusLabel(selectedRoute?.status)} />
							<div className="p-4">
								<LeafletMap routes={selectedRoute ? [selectedRoute] : activeRun?.routes ?? []} />
							</div>
						</section>

						<section className="col-span-4 overflow-hidden rounded-lg border border-border bg-card">
							<SectionHeader title="Lịch sử tuyến" helper={isLoadingRun ? "Đang tải" : `${historyRuns.length} lần`} />
							<HistoryList runs={historyRuns} activeRun={activeRun} onSelectRun={onSelectRun} />
						</section>
					</div>
				</div>
			</main>
		</div>
	);
}

function DriverMobileView(props: SharedDriverViewProps & { mobileView: DriverMobileView; onMobileViewChange: (view: DriverMobileView) => void }) {
	const {
		userName,
		orders,
		selectedOrder,
		selectedRoute,
		routeStops,
		activeRun,
		historyRuns,
		remainingStops,
		totalDistance,
		isLoading,
		isRefreshing,
		isLoadingRun,
		completingOrderId,
		mobileView,
		onMobileViewChange,
		onRefresh,
		onLogout,
		onSelectOrder,
		onCompleteOrder,
		onSelectRun,
	} = props;

	return (
		<div className="mx-auto min-h-screen w-full max-w-[430px] bg-background pb-24 shadow-xl shadow-black/10 lg:border-x lg:border-border">
			<header className="sticky top-0 z-20 border-b border-border bg-background/95 px-4 py-3 backdrop-blur">
				<div className="mx-auto flex max-w-md items-center justify-between gap-3">
					<div className="min-w-0">
						<p className="text-xs text-muted-foreground">Driver Portal</p>
						<h1 className="truncate text-lg font-bold">{userName}</h1>
					</div>
					<div className="flex items-center gap-2">
						<Button onClick={onRefresh} variant="outline" size="icon-sm" disabled={isRefreshing} title="Làm mới" aria-label="Làm mới">
							<RefreshCw className={cn("h-4 w-4", isRefreshing && "animate-spin")} />
						</Button>
						<Button onClick={onLogout} variant="outline" size="icon-sm" title="Đăng xuất" aria-label="Đăng xuất">
							<LogOut className="h-4 w-4" />
						</Button>
					</div>
				</div>
			</header>

			<main className="mx-auto max-w-md space-y-4 px-4 py-4">
				<div className="grid grid-cols-3 gap-2">
					<CompactMetric label="Đơn" value={String(remainingStops)} />
					<CompactMetric label="Tuyến" value={String(activeRun?.routes?.length ?? 0)} />
					<CompactMetric label="Km" value={formatNumber(totalDistance, 1)} />
				</div>

				{mobileView === "orders" && (
					<div className="space-y-4">
						<OrderList orders={orders} selectedOrder={selectedOrder} isLoading={isLoading} onSelectOrder={onSelectOrder} compact />
						<OrderDetail order={selectedOrder} completingOrderId={completingOrderId} onCompleteOrder={onCompleteOrder} compact />
					</div>
				)}

				{mobileView === "route" && (
					<div className="space-y-4">
						<div className="overflow-hidden rounded-lg border border-border bg-card">
							<SectionHeader title="Bản đồ" helper={isLoadingRun ? "Đang tải" : routeStatusLabel(selectedRoute?.status)} />
							<div className="p-3">
								<LeafletMap routes={selectedRoute ? [selectedRoute] : activeRun?.routes ?? []} />
							</div>
						</div>
						<RouteSummary route={selectedRoute} stops={routeStops} />
					</div>
				)}

				{mobileView === "history" && (
					<div className="overflow-hidden rounded-lg border border-border bg-card">
						<SectionHeader title="Lịch sử tuyến" helper={`${historyRuns.length} lần`} />
						<HistoryList runs={historyRuns} activeRun={activeRun} onSelectRun={onSelectRun} />
					</div>
				)}
			</main>

			<nav className="fixed bottom-0 left-1/2 z-30 w-full max-w-[430px] -translate-x-1/2 border-t border-border bg-background/95 px-4 py-3 backdrop-blur">
				<div className="mx-auto grid max-w-md grid-cols-3 gap-2">
					<MobileNavButton active={mobileView === "orders"} icon={ClipboardList} label="Đơn" onClick={() => onMobileViewChange("orders")} />
					<MobileNavButton active={mobileView === "route"} icon={Navigation} label="Tuyến" onClick={() => onMobileViewChange("route")} />
					<MobileNavButton active={mobileView === "history"} icon={CalendarClock} label="Lịch sử" onClick={() => onMobileViewChange("history")} />
				</div>
			</nav>
		</div>
	);
}

function DriverAccessDenied() {
	return (
		<div className="flex min-h-screen items-center justify-center bg-background p-6">
			<div className="w-full max-w-md rounded-lg border border-border bg-card p-6 text-center shadow-sm">
				<div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-lg bg-destructive/10 text-destructive">
					<ShieldAlert className="h-6 w-6" />
				</div>
				<h1 className="text-xl font-bold">Không có quyền Driver Portal</h1>
				<p className="mt-2 text-sm text-muted-foreground">Tài khoản hiện tại không có quyền driver.delivery.read.</p>
				<Button asChild className="mt-5">
					<Link href="/dashboard">Về dashboard</Link>
				</Button>
			</div>
		</div>
	);
}

function DriverNavItem({ icon: Icon, label, active = false }: { icon: typeof ClipboardList; label: string; active?: boolean }) {
	return (
		<div className={cn("flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium", active ? "bg-primary text-primary-foreground" : "text-sidebar-foreground/60")}>
			<Icon className="h-4 w-4" />
			<span>{label}</span>
		</div>
	);
}

function MetricTile({ icon: Icon, label, value, tone }: { icon: typeof PackageOpen; label: string; value: string; tone: "amber" | "sky" | "emerald" }) {
	const toneClass = {
		amber: "bg-amber-500/10 text-amber-700 border-amber-500/20",
		sky: "bg-sky-500/10 text-sky-700 border-sky-500/20",
		emerald: "bg-emerald-500/10 text-emerald-700 border-emerald-500/20",
	}[tone];

	return (
		<div className="rounded-lg border border-border bg-card p-4">
			<div className="flex items-center justify-between gap-3">
				<div>
					<p className="text-sm text-muted-foreground">{label}</p>
					<p className="mt-1 text-2xl font-bold">{value}</p>
				</div>
				<div className={cn("flex h-11 w-11 items-center justify-center rounded-lg border", toneClass)}>
					<Icon className="h-5 w-5" />
				</div>
			</div>
		</div>
	);
}

function CompactMetric({ label, value }: { label: string; value: string }) {
	return (
		<div className="rounded-lg border border-border bg-card px-3 py-3 text-center">
			<p className="text-xs text-muted-foreground">{label}</p>
			<p className="mt-1 text-lg font-bold">{value}</p>
		</div>
	);
}

function SectionHeader({ title, helper }: { title: string; helper?: string }) {
	return (
		<div className="flex items-center justify-between gap-3 border-b border-border px-4 py-3">
			<h2 className="text-sm font-semibold">{title}</h2>
			{helper ? <span className="rounded-md bg-muted px-2 py-1 text-xs text-muted-foreground">{helper}</span> : null}
		</div>
	);
}

function OrderList({ orders, selectedOrder, isLoading, onSelectOrder, compact = false }: { orders: DriverDeliveryOrder[]; selectedOrder: DriverDeliveryOrder | null; isLoading: boolean; onSelectOrder: (order: DriverDeliveryOrder) => void; compact?: boolean }) {
	if (isLoading) {
		return (
			<div className="space-y-3 p-4">
				{Array.from({ length: compact ? 3 : 6 }).map((_, index) => (
					<div key={index} className="h-20 animate-pulse rounded-lg bg-muted/40" />
				))}
			</div>
		);
	}

	if (!orders.length) {
		return (
			<div className="flex h-44 flex-col items-center justify-center gap-2 p-4 text-center text-sm text-muted-foreground">
				<PackageCheck className="h-8 w-8 opacity-40" />
				Không có đơn đang giao
			</div>
		);
	}

	return (
		<div className={cn("space-y-2 p-3", compact && "p-0")}>
			{orders.map((order) => {
				const active = selectedOrder?.id === order.id;
				return (
					<button
						key={order.id}
						type="button"
						onClick={() => onSelectOrder(order)}
						className={cn(
							"w-full rounded-lg border p-3 text-left transition-colors",
							active ? "border-primary bg-primary/5" : "border-border bg-background hover:border-primary/40",
						)}
					>
						<div className="flex items-start justify-between gap-3">
							<div className="min-w-0">
								<p className="truncate text-sm font-semibold">{order.code}</p>
								<p className="mt-1 truncate text-xs text-muted-foreground">{order.deliveryLocationName}</p>
							</div>
							<StatusPill status={order.status} />
						</div>
						<div className="mt-3 flex items-center justify-between text-xs text-muted-foreground">
							<span>Điểm #{order.stopSequence ?? "--"}</span>
							<span>{formatNumber(order.weightKg, 0)} kg</span>
						</div>
					</button>
				);
			})}
		</div>
	);
}

function OrderDetail({ order, completingOrderId, onCompleteOrder, compact = false }: { order: DriverDeliveryOrder | null; completingOrderId: number | null; onCompleteOrder: (orderId: number) => void; compact?: boolean }) {
	if (!order) {
		return (
			<div className="flex h-64 flex-col items-center justify-center gap-2 p-4 text-center text-sm text-muted-foreground">
				<PackageOpen className="h-8 w-8 opacity-40" />
				Chưa có đơn giao hàng
			</div>
		);
	}

	const canComplete = order.status === OrderStatus.IN_TRANSIT;
	const isCompleting = completingOrderId === order.id;

	return (
		<div className={cn("space-y-4 p-4", compact && "rounded-lg border border-border bg-card")}>
			<div className="flex items-start justify-between gap-3">
				<div>
					<p className="text-xs text-muted-foreground">Mã đơn</p>
					<h3 className="mt-1 text-xl font-bold">{order.code}</h3>
				</div>
				<StatusPill status={order.status} />
			</div>

			<div className="rounded-lg border border-border bg-muted/20 p-3">
				<div className="mb-2 flex items-center gap-2 text-sm font-medium">
					<MapPin className="h-4 w-4 text-primary" />
					Điểm giao
				</div>
				<p className="text-sm">{order.deliveryLocationName}</p>
				<p className="mt-1 text-xs text-muted-foreground">{[order.deliveryStreet, order.deliveryCity, order.deliveryCountry].filter(Boolean).join(", ") || "--"}</p>
			</div>

			<div className="grid grid-cols-2 gap-3 text-sm">
				<InfoBlock label="Kho" value={order.depotName || "--"} />
				<InfoBlock label="Thứ tự dừng" value={`#${order.stopSequence ?? "--"}`} />
				<InfoBlock label="Khối lượng" value={`${formatNumber(order.weightKg, 0)} kg`} />
				<InfoBlock label="Thể tích" value={`${formatNumber(Number(order.volumeM3 ?? 0), 2)} m³`} />
			</div>

			<Button className="w-full" disabled={!canComplete || isCompleting} onClick={() => onCompleteOrder(order.id)}>
				{isCompleting ? <RefreshCw className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
				{canComplete ? "Hoàn thành đơn" : statusLabel(order.status)}
			</Button>
		</div>
	);
}

function InfoBlock({ label, value }: { label: string; value: string }) {
	return (
		<div className="rounded-lg border border-border bg-background p-3">
			<p className="text-xs text-muted-foreground">{label}</p>
			<p className="mt-1 truncate font-semibold">{value}</p>
		</div>
	);
}

function RouteSummary({ route, stops }: { route: Route | null; stops: Route["stops"] }) {
	if (!route) {
		return (
			<div className="flex h-64 flex-col items-center justify-center gap-2 p-4 text-center text-sm text-muted-foreground">
				<RouteIcon className="h-8 w-8 opacity-40" />
				Chưa có tuyến được gán
			</div>
		);
	}

	return (
		<div className="space-y-4 p-4">
			<div className="grid grid-cols-3 gap-2">
				<CompactMetric label="Km" value={formatNumber(Number(route.totalDistanceKm ?? 0), 1)} />
				<CompactMetric label="Phút" value={formatNumber(route.totalDurationMin, 0)} />
				<CompactMetric label="Điểm" value={String(stops.length)} />
			</div>
			<div className="space-y-2">
				{stops.map((stop) => (
					<div key={stop.id} className="flex items-center gap-3 rounded-lg border border-border bg-background p-3">
						<div className={cn("flex h-8 w-8 items-center justify-center rounded-lg text-xs font-bold", stop.orderId ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground")}>
							{stop.orderId ? stop.stopSequence : <Truck className="h-4 w-4" />}
						</div>
						<div className="min-w-0 flex-1">
							<p className="truncate text-sm font-medium">{stop.orderId ? `Đơn #${stop.orderId}` : "Kho"}</p>
							<p className="text-xs text-muted-foreground">{formatNumber(Number(stop.distanceFromPrevKm ?? 0), 1)} km từ điểm trước</p>
						</div>
						<ArrowRight className="h-4 w-4 text-muted-foreground" />
					</div>
				))}
			</div>
		</div>
	);
}

function HistoryList({ runs, activeRun, onSelectRun }: { runs: RoutingRun[]; activeRun: RoutingRun | null; onSelectRun: (run: RoutingRun) => void }) {
	if (!runs.length) {
		return (
			<div className="flex h-44 flex-col items-center justify-center gap-2 p-4 text-center text-sm text-muted-foreground">
				<Clock3 className="h-8 w-8 opacity-40" />
				Chưa có lịch sử tuyến
			</div>
		);
	}

	return (
		<div className="space-y-2 p-3">
			{runs.map((run) => {
				const active = activeRun?.id === run.id;
				return (
					<button key={run.id} type="button" onClick={() => onSelectRun(run)} className={cn("w-full rounded-lg border p-3 text-left transition-colors", active ? "border-primary bg-primary/5" : "border-border bg-background hover:border-primary/40")}>
						<div className="flex items-center justify-between gap-3">
							<p className="text-sm font-semibold">Run #{run.id}</p>
							<span className="text-xs text-muted-foreground">{routeStatusLabel(run.status)}</span>
						</div>
						<div className="mt-2 flex items-center justify-between text-xs text-muted-foreground">
							<span>{formatDateTime(run.createdAt || run.startTime)}</span>
							<span>{run.routes?.length ?? 0} tuyến</span>
						</div>
					</button>
				);
			})}
		</div>
	);
}

function StatusPill({ status }: { status: OrderStatus }) {
	const isInTransit = status === OrderStatus.IN_TRANSIT;
	return (
		<span className={cn("shrink-0 rounded-md border px-2 py-1 text-xs font-medium", isInTransit ? "border-amber-500/20 bg-amber-500/10 text-amber-700" : "border-emerald-500/20 bg-emerald-500/10 text-emerald-700")}>
			{statusLabel(status)}
		</span>
	);
}

function MobileNavButton({ active, icon: Icon, label, onClick }: { active: boolean; icon: typeof ClipboardList; label: string; onClick: () => void }) {
	return (
		<button type="button" onClick={onClick} className={cn("flex flex-col items-center gap-1 rounded-lg px-3 py-2 text-xs font-medium transition-colors", active ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-muted")}>
			<Icon className="h-4 w-4" />
			<span>{label}</span>
		</button>
	);
}
