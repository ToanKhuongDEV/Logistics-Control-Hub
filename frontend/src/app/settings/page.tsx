"use client";

import { useState } from "react";
import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card } from "@/components/ui/card";
import { Globe, Bell, Lock, Users, Save, X } from "lucide-react";

export default function SettingsPage() {
	const [activeTab, setActiveTab] = useState<"company" | "notifications" | "system" | "users">("company");
	const [isEditingCompany, setIsEditingCompany] = useState(false);

	const [companyData, setCompanyData] = useState({
		name: "LogiTower Vietnam",
		email: "contact@logitower.vn",
		phone: "+84 28 1234 5678",
		address: "123 Đường Trần Hưng Đạo, Quận 1, TPHCM",
		website: "www.logitower.vn",
		taxId: "0123456789",
		description: "",
	});

	const handleCompanySave = () => {
		setIsEditingCompany(false);
		// TODO: Call API to save
	};

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex flex-col h-full">
					<div className="border-b border-border bg-card">
						<div className="px-8 py-6">
							<h1 className="text-3xl font-bold text-foreground">Cài đặt</h1>
							<p className="text-muted-foreground mt-2">Quản lý cài đặt hệ thống, công ty và người dùng</p>
						</div>
					</div>

					<div className="p-8">
						{/* Tabs */}
						<div className="flex gap-2 mb-8 border-b border-border pb-4">
							<button onClick={() => setActiveTab("company")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "company" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<Globe className="w-4 h-4" />
									Công ty
								</div>
							</button>
							<button onClick={() => setActiveTab("notifications")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "notifications" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<Bell className="w-4 h-4" />
									Thông báo
								</div>
							</button>
							<button onClick={() => setActiveTab("system")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "system" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<Lock className="w-4 h-4" />
									Hệ thống
								</div>
							</button>
							<button onClick={() => setActiveTab("users")} className={`px-4 py-2 font-medium transition-colors ${activeTab === "users" ? "text-primary border-b-2 border-primary" : "text-muted-foreground hover:text-foreground"}`}>
								<div className="flex items-center gap-2">
									<Users className="w-4 h-4" />
									Người dùng
								</div>
							</button>
						</div>

						{/* Company Settings */}
						{activeTab === "company" && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<div className="flex items-center justify-between mb-6">
										<h2 className="text-xl font-semibold text-foreground">Thông tin công ty</h2>
										<Button onClick={() => setIsEditingCompany(!isEditingCompany)} variant={isEditingCompany ? "default" : "outline"} className="gap-2">
											{isEditingCompany ? (
												<>
													<X className="w-4 h-4" />
													Hủy
												</>
											) : (
												"Sửa"
											)}
										</Button>
									</div>

									<div className="space-y-4">
										<div>
											<Label htmlFor="companyName">Tên công ty</Label>
											<Input id="companyName" value={companyData.name} onChange={(e) => setCompanyData({ ...companyData, name: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
										</div>

										<div>
											<Label htmlFor="companyEmail">Email công ty</Label>
											<Input id="companyEmail" type="email" value={companyData.email} onChange={(e) => setCompanyData({ ...companyData, email: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
										</div>

										<div>
											<Label htmlFor="phone">Số điện thoại</Label>
											<Input id="phone" value={companyData.phone} onChange={(e) => setCompanyData({ ...companyData, phone: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
										</div>

										<div>
											<Label htmlFor="address">Địa chỉ</Label>
											<Input id="address" value={companyData.address} onChange={(e) => setCompanyData({ ...companyData, address: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
										</div>

										<div>
											<Label htmlFor="website">Website</Label>
											<Input id="website" value={companyData.website} onChange={(e) => setCompanyData({ ...companyData, website: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
										</div>

										<div>
											<Label htmlFor="taxId">Mã số thuế</Label>
											<Input id="taxId" value={companyData.taxId} onChange={(e) => setCompanyData({ ...companyData, taxId: e.target.value })} disabled={!isEditingCompany} className="mt-2" />
										</div>

										{isEditingCompany && (
											<Button onClick={handleCompanySave} className="w-full bg-primary hover:bg-primary/90 gap-2">
												<Save className="w-4 h-4" />
												Lưu thay đổi
											</Button>
										)}
									</div>
								</Card>
							</div>
						)}

						{/* Notification Settings */}
						{activeTab === "notifications" && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<h2 className="text-xl font-semibold text-foreground mb-6">Cài đặt thông báo</h2>
									<div className="bg-card rounded-lg border border-border p-12 text-center">
										<p className="text-muted-foreground">Tính năng đang được phát triển</p>
									</div>
								</Card>
							</div>
						)}

						{/* System Settings */}
						{activeTab === "system" && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<h2 className="text-xl font-semibold text-foreground mb-6">Cài đặt hệ thống</h2>
									<div className="bg-card rounded-lg border border-border p-12 text-center">
										<p className="text-muted-foreground">Tính năng đang được phát triển</p>
									</div>
								</Card>
							</div>
						)}

						{/* User Management */}
						{activeTab === "users" && (
							<div className="max-w-2xl">
								<Card className="p-6">
									<h2 className="text-xl font-semibold text-foreground mb-6">Quản lý người dùng</h2>
									<div className="bg-card rounded-lg border border-border p-12 text-center">
										<p className="text-muted-foreground">Tính năng đang được phát triển</p>
									</div>
								</Card>
							</div>
						)}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
