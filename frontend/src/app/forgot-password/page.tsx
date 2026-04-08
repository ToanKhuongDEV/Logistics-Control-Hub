"use client";

import { useState } from "react";
import Link from "next/link";
import { Mail, Loader2 } from "lucide-react";
import { Logo } from "@/components/logo";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { authService } from "@/lib/auth";
import { toast } from "sonner";

export default function ForgotPasswordPage() {
	const [email, setEmail] = useState("");
	const [isSubmitting, setIsSubmitting] = useState(false);

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setIsSubmitting(true);

		try {
			await authService.forgotPassword({ email });
			toast.success("Nếu email tồn tại, hệ thống đã gửi hướng dẫn đặt lại mật khẩu.");
		} catch (error: any) {
			console.error("Forgot password error:", error);
			toast.error(error.response?.data?.message || "Không thể gửi yêu cầu lúc này");
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
					<h1 className="text-2xl font-bold text-foreground">Quên mật khẩu</h1>
					<p className="mt-2 text-sm leading-6 text-muted-foreground">Nhập email tài khoản. Nếu email hợp lệ, hệ thống sẽ gửi liên kết đặt lại mật khẩu qua Gmail.</p>
				</div>

				<form onSubmit={handleSubmit} className="space-y-5">
					<div className="space-y-2">
						<Label htmlFor="email">Email</Label>
						<Input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Nhập email tài khoản" required />
					</div>

					<Button type="submit" className="w-full gap-2" disabled={isSubmitting}>
						{isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Mail className="h-4 w-4" />}
						Gửi email đặt lại mật khẩu
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
