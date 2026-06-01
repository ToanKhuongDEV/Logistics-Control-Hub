"use client";

import React, { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";

import { Sidebar } from "@/components/sidebar";
import { useAuth } from "@/contexts/auth-context";

export function DashboardLayout({ children }: { children: React.ReactNode }) {
	const { user } = useAuth();
	const pathname = usePathname();
	const router = useRouter();

	useEffect(() => {
		if (user?.role === "DRIVER" && pathname !== "/driver") {
			router.replace("/driver");
		}
	}, [pathname, router, user?.role]);

	if (user?.role === "DRIVER" && pathname !== "/driver") {
		return null;
	}

	return (
		<div className="flex min-h-screen bg-background relative">
			<Sidebar />
			<div className="flex-1 overflow-auto h-screen">{children}</div>
		</div>
	);
}
