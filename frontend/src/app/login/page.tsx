"use client";

import React from "react";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Truck, Route, MapPin, Eye, EyeOff } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { toast } from "sonner";
import { Logo } from "@/components/logo";

export default function LoginPage() {
	const [username, setUsername] = useState("");
	const [password, setPassword] = useState("");
	const [isLoading, setIsLoading] = useState(false);
	const [showPassword, setShowPassword] = useState(false);
	const { login } = useAuth();
	const router = useRouter();

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setIsLoading(true);

		try {
			await login(username, password);
			toast.success("Đăng nhập thành công!");
			router.push("/dashboard");
		} catch (err: any) {
			console.error("Login error:", err);

			// Handle different error types
			if (err.response?.status === 401) {
				toast.error("Tên đăng nhập hoặc mật khẩu không đúng");
			} else if (err.code === "ERR_NETWORK") {
				toast.error("Không thể kết nối đến server. Vui lòng kiểm tra backend đã chạy chưa.");
			} else {
				toast.error(err.response?.data?.message || "Đã có lỗi xảy ra. Vui lòng thử lại.");
			}
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div className="min-h-screen flex">
			{/* Left Side - Branding & Visual */}
			<div className="hidden lg:flex lg:w-1/2 xl:w-3/5 relative overflow-hidden">
				{/* Animated Background Lines */}
				<div className="absolute inset-0 bg-gradient-to-br from-background via-card to-background">
					{/* Animated route lines */}
					<svg className="absolute inset-0 w-full h-full" xmlns="http://www.w3.org/2000/svg">
						<defs>
							<linearGradient id="line1" x1="0%" y1="0%" x2="100%" y2="0%">
								<stop offset="0%" stopColor="oklch(0.68 0.18 45)" stopOpacity="0" />
								<stop offset="50%" stopColor="oklch(0.68 0.18 45)" stopOpacity="0.8" />
								<stop offset="100%" stopColor="oklch(0.68 0.18 45)" stopOpacity="0" />
							</linearGradient>
							<linearGradient id="line2" x1="0%" y1="0%" x2="100%" y2="0%">
								<stop offset="0%" stopColor="oklch(0.75 0.15 60)" stopOpacity="0" />
								<stop offset="50%" stopColor="oklch(0.75 0.15 60)" stopOpacity="0.6" />
								<stop offset="100%" stopColor="oklch(0.75 0.15 60)" stopOpacity="0" />
							</linearGradient>
							<linearGradient id="line3" x1="0%" y1="0%" x2="100%" y2="0%">
								<stop offset="0%" stopColor="oklch(0.55 0.12 220)" stopOpacity="0" />
								<stop offset="50%" stopColor="oklch(0.55 0.12 220)" stopOpacity="0.5" />
								<stop offset="100%" stopColor="oklch(0.55 0.12 220)" stopOpacity="0" />
							</linearGradient>
						</defs>

						{/* Animated curved paths */}
						<path d="M0,300 Q200,200 400,350 T800,300 T1200,400" fill="none" stroke="url(#line1)" strokeWidth="3" className="animate-pulse" style={{ animationDuration: "3s" }} />
						<path d="M0,500 Q300,400 500,500 T900,450 T1200,550" fill="none" stroke="url(#line2)" strokeWidth="2" className="animate-pulse" style={{ animationDuration: "4s", animationDelay: "1s" }} />
						<path d="M0,700 Q250,600 450,700 T850,650 T1200,700" fill="none" stroke="url(#line3)" strokeWidth="2" className="animate-pulse" style={{ animationDuration: "5s", animationDelay: "0.5s" }} />
					</svg>

					{/* Grid pattern overlay */}
					<div
						className="absolute inset-0 opacity-10"
						style={{
							backgroundImage: `linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px),
              linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px)`,
							backgroundSize: "50px 50px",
						}}
					/>
				</div>

				{/* Content */}
				<div className="relative z-10 flex flex-col justify-between p-12 xl:p-16">
					{/* Logo */}
					<Logo className="scale-125 origin-left" iconClassName="w-10 h-10 text-lg" />

					{/* Main Content */}
					<div className="space-y-8">
						<div>
							<h2 className="text-4xl xl:text-5xl font-bold text-foreground leading-tight">
								Unified
								<br />
								<span className="text-primary">Logistics</span>
								<br />
								Platform
							</h2>
						</div>

						<p className="text-lg text-muted-foreground max-w-md leading-relaxed">Tối ưu hóa hiệu suất vận chuyển với insights thời gian thực, tự động hóa và quản lý đội xe thông minh.</p>

						{/* Feature Indicators */}
						<div className="flex flex-col gap-4">
							<FeatureItem icon={<Route className="w-5 h-5" />} label="Tối ưu tuyến đường" />
							<FeatureItem icon={<MapPin className="w-5 h-5" />} label="Theo dõi thời gian thực" />
							<FeatureItem icon={<Truck className="w-5 h-5" />} label="Quản lý đội xe" />
						</div>
					</div>

					{/* Bottom Stats */}
					<div className="flex gap-12">
						<StatItem value="98%" label="Giao hàng đúng hẹn" />
						<StatItem value="35%" label="Tiết kiệm chi phí" />
						<StatItem value="24/7" label="Hỗ trợ" />
					</div>
				</div>
			</div>

			{/* Right Side - Login Form */}
			<div className="w-full lg:w-1/2 xl:w-2/5 flex items-center justify-center p-8 bg-card">
				<div className="w-full max-w-md space-y-8">
					{/* Mobile Logo */}
					<div className="lg:hidden flex justify-center mb-8">
						<Logo className="scale-110" />
					</div>

					{/* Header */}
					<div className="space-y-2 text-center lg:text-left">
						<h2 className="text-3xl font-bold text-foreground">Đăng nhập</h2>
						<p className="text-muted-foreground">Truy cập vào bảng điều khiển logistics của bạn</p>
					</div>

					{/* Form */}
					<form onSubmit={handleSubmit} className="space-y-6">
						<div className="space-y-2">
							<Label htmlFor="username" className="text-foreground font-medium">
								Tên đăng nhập
							</Label>
							<Input id="username" type="text" placeholder="Nhập tên đăng nhập" value={username} onChange={(e) => setUsername(e.target.value)} className="h-12 bg-input border-border text-foreground placeholder:text-muted-foreground focus:border-primary focus:ring-primary" required />
						</div>

						<div className="space-y-2">
							<Label htmlFor="password" className="text-foreground font-medium">
								Mật khẩu
							</Label>
							<div className="relative">
								<Input
									id="password"
									type={showPassword ? "text" : "password"}
									placeholder="Nhập mật khẩu"
									value={password}
									onChange={(e) => setPassword(e.target.value)}
									className="h-12 bg-input border-border text-foreground placeholder:text-muted-foreground focus:border-primary focus:ring-primary pr-12"
									required
								/>
								<button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors" aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}>
									{showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
								</button>
							</div>
						</div>

						<Button type="submit" className="w-full h-12 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold text-base transition-all duration-200 hover:shadow-lg hover:shadow-primary/25" disabled={isLoading}>
							{isLoading ? (
								<div className="flex items-center gap-2">
									<div className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
									<span>Đang đăng nhập...</span>
								</div>
							) : (
								"Đăng nhập"
							)}
						</Button>
					</form>

					{/* Footer */}
					<p className="text-center text-sm text-muted-foreground pt-4">© 2026 LogiTower. Nền tảng logistics thông minh.</p>
				</div>
			</div>
		</div>
	);
}

function FeatureItem({ icon, label }: { icon: React.ReactNode; label: string }) {
	return (
		<div className="flex items-center gap-3">
			<div className="flex items-center justify-center w-10 h-10 rounded-lg bg-secondary text-primary">{icon}</div>
			<span className="text-foreground font-medium">{label}</span>
		</div>
	);
}

function StatItem({ value, label }: { value: string; label: string }) {
	return (
		<div>
			<p className="text-2xl font-bold text-primary">{value}</p>
			<p className="text-sm text-muted-foreground">{label}</p>
		</div>
	);
}
