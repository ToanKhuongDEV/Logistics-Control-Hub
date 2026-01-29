"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/auth-context";

interface ProtectedRouteProps {
	children: React.ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
	const { isAuthenticated, isLoading } = useAuth();
	const router = useRouter();

	useEffect(() => {
		if (!isLoading && !isAuthenticated) {
			router.push("/login");
		}
	}, [isAuthenticated, isLoading, router]);

	// Show loading spinner while checking auth
	if (isLoading) {
		return (
			<div className="min-h-screen flex items-center justify-center bg-background">
				<div className="flex flex-col items-center gap-4">
					<div className="w-12 h-12 border-4 border-primary/30 border-t-primary rounded-full animate-spin" />
					<p className="text-muted-foreground">Đang tải...</p>
				</div>
			</div>
		);
	}

	// Don't render children until authenticated
	if (!isAuthenticated) {
		return null;
	}

	return <>{children}</>;
}
