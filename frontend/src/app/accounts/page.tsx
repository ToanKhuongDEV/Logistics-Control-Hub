"use client";

import { ProtectedRoute } from "@/components/protected-route";
import { DashboardLayout } from "@/components/dashboard-layout";
import { Card } from "@/components/ui/card";
import { Users } from "lucide-react";
import { useAuth } from "@/contexts/auth-context";
import { hasPermission } from "@/lib/auth";
import { AccountManagementPanel } from "@/components/account-management-panel";
import { PageHeader } from "@/components/page-header";

export default function AccountsPage() {
	const { user } = useAuth();
	const canManageAccounts = hasPermission(user, "account.manage");

	return (
		<ProtectedRoute>
			<DashboardLayout>
				<div className="flex flex-col h-full">
					<div className="border-b border-border bg-card">
						<div className="px-8 py-6">
							<PageHeader tag="Bảng điều khiển tài khoản" icon={Users} />
							<h1 className="text-3xl font-bold text-foreground">Tài khoản</h1>
							<p className="mt-2 text-muted-foreground">Quản lý người dùng, vai trò và kho phụ trách cho từng dispatcher</p>
						</div>
					</div>

					<div className="p-8">
						{canManageAccounts ? (
							<AccountManagementPanel />
						) : (
							<Card className="max-w-2xl p-10 text-center">
								<div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
									<Users className="h-6 w-6" />
								</div>
								<h2 className="text-xl font-semibold text-foreground">Chỉ admin mới được quản lý tài khoản</h2>
								<p className="mt-2 text-muted-foreground">Dispatcher có thể đổi mật khẩu trong phần cài đặt nhưng không được chỉnh nhân sự hoặc gán kho.</p>
							</Card>
						)}
					</div>
				</div>
			</DashboardLayout>
		</ProtectedRoute>
	);
}
