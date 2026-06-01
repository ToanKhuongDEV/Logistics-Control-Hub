"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { usePathname } from "next/navigation";
import { authService, User } from "@/lib/auth";

interface AuthContextType {
	user: User | null;
	isLoading: boolean;
	isAuthenticated: boolean;
	login: (username: string, password: string) => Promise<User>;
	logout: () => void;
	refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
	children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
	const [user, setUser] = useState<User | null>(null);
	const [isLoading, setIsLoading] = useState(true);
	const pathname = usePathname();

	// HttpOnly cookies are not readable from JS, so validate the session with the API.
	useEffect(() => {
		const isPublicAuthRoute = pathname === "/" || ["/login", "/forgot-password", "/reset-password"].some((route) =>
			pathname.startsWith(route),
		);

		if (isPublicAuthRoute) {
			setIsLoading(false);
			return;
		}

		const loadUser = async () => {
			try {
				const userData = await authService.getCurrentUser();
				setUser(userData);
			} catch (error) {
				console.error("Failed to load user:", error);
				setUser(null);
			}
			setIsLoading(false);
		};

		loadUser();
	}, [pathname]);

	const login = async (username: string, password: string) => {
		await authService.login(username, password);
		const userData = await authService.getCurrentUser();
		setUser(userData);
		return userData;
	};

	const logout = () => {
		void (async () => {
			await authService.logout();
			setUser(null);
			window.location.href = "/login";
		})();
	};

	const refreshUser = async () => {
		const userData = await authService.getCurrentUser();
		setUser(userData);
	};

	const value: AuthContextType = {
		user,
		isLoading,
		isAuthenticated: !!user,
		login,
		logout,
		refreshUser,
	};

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
	const context = useContext(AuthContext);
	if (context === undefined) {
		throw new Error("useAuth must be used within an AuthProvider");
	}
	return context;
}
