"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { KeyRound, Loader2 } from "lucide-react";
import { Logo } from "@/components/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authService } from "@/lib/auth";
import { toast } from "sonner";

export default function ResetPasswordPage() {
	return (
		<Suspense fallback={<ResetPasswordFallback />}>
			<ResetPasswordContent />
		</Suspense>
	);
}

function ResetPasswordContent() {
	const router = useRouter();
	const searchParams = useSearchParams();
	const token = searchParams.get("token") || "";

	const [newPassword, setNewPassword] = useState("");
	const [confirmPassword, setConfirmPassword] = useState("");
	const [isSubmitting, setIsSubmitting] = useState(false);

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();

		if (!token) {
			toast.error("Liên kết đặt lại mật khẩu không hợp lệ");
			return;
		}

		if (newPassword !== confirmPassword) {
			toast.error("Mật khẩu xác nhận không khớp");
			return;
		}

		setIsSubmitting(true);
		try {
			await authService.resetPassword({ token, newPassword });
			toast.success("Đặt lại mật khẩu thành công");
			router.push("/login");
		} catch (error: any) {
			console.error("Reset password error:", error);
			toast.error(error.response?.data?.message || "Không thể đặt lại mật khẩu");
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<div className="flex min-h-screen items-center justify-center bg-background px-6 py-10">
			<div className="w-full max-w-md rounded-2xl border border-border bg-card p-8 shadow-sm">
				<div className="mb-8 flex justify-center">
					<Logo />
				</div>
				<div className="mb-6 text-center">
					<h1 className="text-2xl font-bold text-foreground">Đặt lại mật khẩu</h1>
					<p className="mt-2 text-sm leading-6 text-muted-foreground">Nhập mật khẩu mới cho tài khoản của bạn.</p>
				</div>

				<form onSubmit={handleSubmit} className="space-y-5">
					<div className="space-y-2">
						<Label htmlFor="newPassword">Mật khẩu mới</Label>
						<Input id="newPassword" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="Ít nhất 8 ký tự" required />
					</div>
					<div className="space-y-2">
						<Label htmlFor="confirmPassword">Xác nhận mật khẩu mới</Label>
						<Input id="confirmPassword" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="Nhập lại mật khẩu mới" required />
					</div>

					<Button type="submit" className="w-full gap-2" disabled={isSubmitting}>
						{isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <KeyRound className="h-4 w-4" />}
						Cập nhật mật khẩu
					</Button>
				</form>

				<div className="mt-6 text-center text-sm text-muted-foreground">
					<Link href="/login" className="text-primary hover:underline">
						Quay lại đăng nhập
					</Link>
				</div>
			</div>
		</div>
	);
}

function ResetPasswordFallback() {
	return (
		<div className="flex min-h-screen items-center justify-center bg-background px-6 py-10">
			<div className="flex items-center gap-3 rounded-2xl border border-border bg-card px-6 py-5 text-sm text-muted-foreground shadow-sm">
				<Loader2 className="h-4 w-4 animate-spin" />
				Đang tải trang đặt lại mật khẩu...
			</div>
		</div>
	);
}
