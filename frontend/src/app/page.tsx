"use client";

import { useEffect } from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { Logo } from "@/components/logo";

export default function Home() {
	const router = useRouter();

	useEffect(() => {
		const timer = setTimeout(() => {
			router.push("/login");
		}, 20000);

		return () => clearTimeout(timer);
	}, [router]);

	const handleClick = () => {
		router.push("/login");
	};

	return (
		<div className="relative w-full h-screen bg-black cursor-pointer overflow-hidden" onClick={handleClick}>
			<Image src="/flash-card1.png" alt="Splash Screen" fill priority className="object-cover" sizes="100vw" />

			<div className="absolute inset-0 flex flex-col items-center justify-center z-10 bg-black/60">
				<div className="flex flex-col items-center animate-in fade-in zoom-in duration-1000">
					{/* Logo scaled up significantly. Using arbitrary variant to force white text on "Logi" part which defaults to sidebar-foreground */}
					<div className="mb-12">
						<Logo className="scale-[5] [&_.text-sidebar-foreground]:text-white" />
					</div>

					<p className="text-white/80 mt-12 text-sm font-medium tracking-[0.2em] uppercase border-t border-white/30 pt-4 px-8">AI Supply Chain Control Tower</p>
				</div>
			</div>
		</div>
	);
}
