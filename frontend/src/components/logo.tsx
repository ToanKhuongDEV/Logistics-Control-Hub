import React from "react";
import { cn } from "@/lib/utils";

interface LogoProps {
	className?: string;
	iconClassName?: string;
	textClassName?: string;
}

export function Logo({ className, iconClassName, textClassName }: LogoProps) {
	return (
		<div className={cn("flex items-center gap-2", className)}>
			<div className={cn("w-8 h-8 bg-primary rounded-lg flex items-center justify-center font-bold text-accent-foreground", iconClassName)}>LT</div>

			<div className={cn("flex items-center", textClassName)}>
				<span className="font-bold text-sidebar-foreground">Logi</span>
				<span className="font-bold text-primary text-xs">Tower</span>
			</div>
		</div>
	);
}
