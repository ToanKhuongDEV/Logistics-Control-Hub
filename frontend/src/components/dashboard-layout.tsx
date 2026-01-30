"use client";

import React from "react";

import { Sidebar } from "@/components/sidebar";

export function DashboardLayout({ children }: { children: React.ReactNode }) {
	return (
		<div className="flex min-h-screen bg-background relative">
			<Sidebar />
			<div className="flex-1 overflow-auto h-screen">{children}</div>
		</div>
	);
}
