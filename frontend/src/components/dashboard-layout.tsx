"use client";

import React from "react";

import { Sidebar } from "@/components/sidebar";

export function DashboardLayout({ children }: { children: React.ReactNode }) {
	return (
		<div className="flex min-h-screen bg-background">
			<Sidebar />
			<div className="flex-1 overflow-auto">{children}</div>
		</div>
	);
}
