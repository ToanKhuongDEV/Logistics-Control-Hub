import * as React from "react";

type ToastProps = {
	title?: string;
	description?: string;
	variant?: "default" | "destructive";
};

type ToastContextType = {
	toast: (props: ToastProps) => void;
};

const ToastContext = React.createContext<ToastContextType | undefined>(undefined);

export function ToastProvider({ children }: { children: React.ReactNode }) {
	const [toasts, setToasts] = React.useState<(ToastProps & { id: number })[]>([]);

	const toast = React.useCallback((props: ToastProps) => {
		const id = Date.now();
		setToasts((prev) => [...prev, { ...props, id }]);

		// Auto remove after 3 seconds
		setTimeout(() => {
			setToasts((prev) => prev.filter((t) => t.id !== id));
		}, 3000);
	}, []);

	return (
		<ToastContext.Provider value={{ toast }}>
			{children}
			<div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
				{toasts.map((t) => (
					<div key={t.id} className={`min-w-[300px] rounded-lg border p-4 shadow-lg animate-in slide-in-from-right ${t.variant === "destructive" ? "bg-red-500 text-white border-red-600" : "bg-card text-foreground border-border"}`}>
						{t.title && <div className="font-semibold">{t.title}</div>}
						{t.description && <div className="text-sm opacity-90 mt-1">{t.description}</div>}
					</div>
				))}
			</div>
		</ToastContext.Provider>
	);
}

export function useToast() {
	const context = React.useContext(ToastContext);
	if (!context) {
		throw new Error("useToast must be used within ToastProvider");
	}
	return context;
}
