"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { authService, User } from "@/lib/auth";

interface AuthContextType {
	user: User | null;
	isLoading: boolean;
	isAuthenticated: boolean;
	login: (username: string, password: string) => Promise<void>;
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

	// Load user on mount if token exists
	useEffect(() => {
		const loadUser = async () => {
			if (authService.isAuthenticated()) {
				try {
					const userData = await authService.getCurrentUser();
					setUser(userData);
				} catch (error) {
					console.error("Failed to load user:", error);
					authService.logout();
				}
			}
			setIsLoading(false);
		};

		loadUser();
	}, []);

	const login = async (username: string, password: string) => {
		await authService.login(username, password);
		const userData = await authService.getCurrentUser();
		setUser(userData);
	};

	const logout = () => {
		authService.logout();
		setUser(null);
		window.location.href = "/login";
	};

	const refreshUser = async () => {
		if (authService.isAuthenticated()) {
			const userData = await authService.getCurrentUser();
			setUser(userData);
		}
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
