"use client";

import { LayoutGrid, Truck, Package, Settings, LogOut, User, Warehouse, History, Users, ScrollText } from "lucide-react";
import { motion } from "framer-motion";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { useAuth } from "@/contexts/auth-context";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/logo";
import { hasPermission } from "@/lib/auth";

export function Sidebar() {
	const pathname = usePathname();
	const { user, logout } = useAuth();

	const menuItems = [
		{
			name: "Tổng quan",
			href: "/dashboard",
			icon: LayoutGrid,
		},
		{
			name: "Đội xe",
			href: "/fleet",
			icon: Truck,
		},
		{
			name: "Đơn hàng",
			href: "/orders",
			icon: Package,
		},
		{
			name: "Lái xe",
			href: "/drivers",
			icon: User,
		},
		{
			name: "Kho",
			href: "/depots",
			icon: Warehouse,
		},
		{
			name: "Lịch sử",
			href: "/history",
			icon: History,
		},
		...(hasPermission(user, "account.manage")
			? [
					{
						name: "Tài khoản",
						href: "/accounts",
						icon: Users,
					},
				]
			: []),
		...(hasPermission(user, "audit.read")
			? [
					{
						name: "Audit",
						href: "/audit",
						icon: ScrollText,
					},
				]
			: []),
		{
			name: "Cài đặt",
			href: "/settings",
			icon: Settings,
		},
	];

	return (
		<div className="sticky top-0 left-0 flex h-screen w-64 flex-col border-r border-sidebar-border bg-sidebar">
			<div className="shrink-0 border-b border-sidebar-border p-6">
				<Logo />
			</div>

			<div className="flex-1 overflow-y-auto">
				<nav className="space-y-2 p-4">
					{menuItems.map((item) => {
						const Icon = item.icon;
						const isActive = pathname === item.href;

						return (
							<Link key={item.href} href={item.href} className={cn("relative flex items-center gap-3 rounded-lg px-4 py-3 transition-colors duration-200", isActive ? "text-primary-foreground" : "text-sidebar-foreground/60 hover:bg-sidebar-accent hover:bg-opacity-10 hover:text-sidebar-foreground")}>
								{isActive && (
									<motion.div
										layoutId="active-nav-item"
										className="absolute inset-0 -z-10 rounded-lg bg-primary shadow-md"
										transition={{
											type: "spring",
											stiffness: 300,
											damping: 30,
										}}
									/>
								)}
								<Icon className={cn("z-10 h-5 w-5 transition-all", isActive && "h-6 w-6")} />
								<span className={cn("z-10 font-medium transition-all", isActive && "text-lg font-bold")}>{item.name}</span>
							</Link>
						);
					})}
				</nav>
			</div>

			<div className="shrink-0 space-y-3 border-t border-sidebar-border bg-sidebar p-4">
				{user && (
					<div className="flex items-center gap-3 rounded-lg bg-sidebar-accent/50 px-3 py-2">
						<div className="flex h-8 w-8 items-center justify-center rounded-full bg-accent">
							<User className="h-4 w-4 text-accent-foreground" />
						</div>
						<div className="min-w-0 flex-1">
							<p className="truncate text-sm font-medium text-sidebar-foreground">{user.fullName || user.username}</p>
							<p className="truncate text-xs text-sidebar-foreground/60">{user.email || "Điều phối viên"}</p>
						</div>
					</div>
				)}

				<Button onClick={logout} variant="outline" className="w-full justify-start gap-2 border-sidebar-border text-sidebar-foreground hover:bg-destructive hover:text-destructive-foreground" size="sm">
					<LogOut className="h-4 w-4" />
					<span>Đăng xuất</span>
				</Button>

				<p className="text-center text-xs text-sidebar-foreground opacity-60">LogiTower v1.0</p>
			</div>
		</div>
	);
}
