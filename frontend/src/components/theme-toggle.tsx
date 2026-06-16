"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function ThemeToggle({ className }: { className?: string }) {
	const { resolvedTheme, setTheme } = useTheme();
	const [mounted, setMounted] = useState(false);

	useEffect(() => {
		setMounted(true);
	}, []);

	const isDark = mounted ? resolvedTheme === "dark" : true;

	return (
		<Button
			type="button"
			variant="outline"
			size="sm"
			className={cn("w-full justify-start gap-2 border-border text-foreground hover:bg-accent hover:text-accent-foreground", className)}
			onClick={() => setTheme(isDark ? "light" : "dark")}
			aria-label={isDark ? "Chuyển sang theme sáng" : "Chuyển sang theme tối"}
			title={isDark ? "Theme sáng" : "Theme tối"}
		>
			{isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
			<span>{isDark ? "Theme sáng" : "Theme tối"}</span>
		</Button>
	);
}
