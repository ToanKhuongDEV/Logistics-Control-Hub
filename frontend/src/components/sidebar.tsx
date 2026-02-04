"use client";

import { LayoutGrid, Truck, Package, BarChart3, Settings, LogOut, User } from "lucide-react";
import { motion } from "framer-motion";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { useAuth } from "@/contexts/auth-context";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/logo";

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
			name: "Cài đặt",
			href: "/settings",
			icon: Settings,
		},
	];

	return (
		<div className="w-64 bg-sidebar border-r border-sidebar-border h-screen flex flex-col sticky top-0 left-0">
			{/* Logo */}
			<div className="p-6 border-b border-sidebar-border shrink-0">
				<Logo />
			</div>

			{/* Navigation */}
			<div className="flex-1 overflow-y-auto">
				<nav className="p-4 space-y-2">
					{menuItems.map((item) => {
						const Icon = item.icon;
						const isActive = pathname === item.href;

						return (
							<Link key={item.href} href={item.href} className={cn("relative flex items-center gap-3 px-4 py-3 rounded-lg transition-colors duration-200", isActive ? "text-primary-foreground" : "text-sidebar-foreground/60 hover:text-sidebar-foreground hover:bg-sidebar-accent hover:bg-opacity-10")}>
								{isActive && (
									<motion.div
										layoutId="active-nav-item"
										className="absolute inset-0 bg-primary rounded-lg shadow-md -z-10"
										transition={{
											type: "spring",
											stiffness: 300,
											damping: 30,
										}}
									/>
								)}
								<Icon className={cn("w-5 h-5 transition-all z-10", isActive && "w-6 h-6")} />
								<span className={cn("font-medium transition-all z-10", isActive && "text-lg font-bold")}>{item.name}</span>
							</Link>
						);
					})}
				</nav>
			</div>

			{/* Footer with User Info and Logout */}
			<div className="p-4 border-t border-sidebar-border space-y-3 bg-sidebar shrink-0">
				{/* User Info */}
				{user && (
					<div className="flex items-center gap-3 px-3 py-2 bg-sidebar-accent/50 rounded-lg">
						<div className="w-8 h-8 bg-accent rounded-full flex items-center justify-center">
							<User className="w-4 h-4 text-accent-foreground" />
						</div>
						<div className="flex-1 min-w-0">
							<p className="text-sm font-medium text-sidebar-foreground truncate">{user.fullName || user.username}</p>
							<p className="text-xs text-sidebar-foreground/60 truncate">{user.email || "Điều phối viên"}</p>
						</div>
					</div>
				)}

				{/* Logout Button */}
				<Button onClick={logout} variant="outline" className="w-full justify-start gap-2 text-sidebar-foreground hover:bg-destructive hover:text-destructive-foreground border-sidebar-border" size="sm">
					<LogOut className="w-4 h-4" />
					<span>Đăng xuất</span>
				</Button>

				<p className="text-xs text-sidebar-foreground opacity-60 text-center">LogiTower v1.0</p>
			</div>
		</div>
	);
}
