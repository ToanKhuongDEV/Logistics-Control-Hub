"use client";

import type { ReactNode } from "react";
import type { LucideIcon } from "lucide-react";

type PageHeaderProps = {
	tag: string;
	icon: LucideIcon;
	title?: string;
	description?: string;
	actions?: ReactNode;
};

export function PageHeader({ tag, icon: Icon }: PageHeaderProps) {
	return (
		<div className="inline-flex items-center gap-2 rounded-full border border-emerald-500/20 bg-emerald-500/10 px-4 py-1.5 text-xs font-semibold uppercase tracking-[0.24em] text-emerald-700">
			<Icon className="h-3.5 w-3.5" />
			{tag}
		</div>
	);
}
