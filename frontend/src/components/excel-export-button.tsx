"use client";

import { useState } from "react";
import { AxiosError } from "axios";
import { Download, X } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { excelApi } from "@/lib/excel-api";
import { ExcelExportParams, ExcelFileType } from "@/types/excel-types";

const MAX_EXPORT_ROWS = 50000;

type ExportFilters = Omit<ExcelExportParams, "type" | "fromDate" | "toDate" | "maxRows">;

interface ExcelExportButtonProps {
	type: ExcelFileType;
	filters?: ExportFilters;
	label?: string;
	title?: string;
	disabled?: boolean;
}

function getErrorMessage(error: unknown) {
	if (error instanceof AxiosError) {
		return error.response?.data?.message ?? "Không thể xuất file Excel";
	}
	return "Không thể xuất file Excel";
}

export function ExcelExportButton({ type, filters, label = "Xuất Excel", title = "Xuất dữ liệu Excel", disabled = false }: ExcelExportButtonProps) {
	const [isOpen, setIsOpen] = useState(false);
	const [isExporting, setIsExporting] = useState(false);
	const [fromDate, setFromDate] = useState("");
	const [toDate, setToDate] = useState("");
	const [maxRows, setMaxRows] = useState("100");

	const handleExport = async () => {
		if (fromDate && toDate && fromDate > toDate) {
			toast.error("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
			return;
		}

		const normalizedMaxRows = Number(maxRows);
		if (!Number.isInteger(normalizedMaxRows) || normalizedMaxRows <= 0 || normalizedMaxRows > MAX_EXPORT_ROWS) {
			toast.error(`Số dòng tối đa phải nằm trong khoảng 1 - ${MAX_EXPORT_ROWS.toLocaleString("vi-VN")}`);
			return;
		}

		setIsExporting(true);
		try {
			await excelApi.export({
				type,
				...filters,
				fromDate: fromDate || undefined,
				toDate: toDate || undefined,
				maxRows: normalizedMaxRows,
			});
			toast.success("Đã tải file Excel");
			setIsOpen(false);
		} catch (error: unknown) {
			console.error("Excel export error:", error);
			toast.error(getErrorMessage(error));
		} finally {
			setIsExporting(false);
		}
	};

	return (
		<>
			<Button type="button" variant="outline" onClick={() => setIsOpen(true)} disabled={disabled || isExporting} className="gap-2">
				<Download className="w-4 h-4" />
				{isExporting ? "Đang xuất..." : label}
			</Button>

			{isOpen && (
				<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
					<div className="mx-4 w-full max-w-md rounded-lg bg-card shadow-lg">
						<div className="flex items-center justify-between border-b border-border p-5">
							<h2 className="text-lg font-semibold text-foreground">{title}</h2>
							<button type="button" onClick={() => setIsOpen(false)} disabled={isExporting} className="text-muted-foreground transition-colors hover:text-foreground">
								<X className="h-5 w-5" />
							</button>
						</div>

						<div className="space-y-4 p-5">
							<div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
								<div className="space-y-2">
									<Label htmlFor={`${type}-from-date`}>Từ ngày</Label>
									<Input id={`${type}-from-date`} type="date" value={fromDate} onChange={(event) => setFromDate(event.target.value)} disabled={isExporting} />
								</div>
								<div className="space-y-2">
									<Label htmlFor={`${type}-to-date`}>Đến ngày</Label>
									<Input id={`${type}-to-date`} type="date" value={toDate} onChange={(event) => setToDate(event.target.value)} disabled={isExporting} />
								</div>
							</div>

							<div className="space-y-2">
								<Label htmlFor={`${type}-max-rows`}>Số dòng tối đa</Label>
								<Input id={`${type}-max-rows`} type="number" min={1} max={MAX_EXPORT_ROWS} value={maxRows} onChange={(event) => setMaxRows(event.target.value)} disabled={isExporting} />
							</div>
						</div>

						<div className="flex justify-end gap-3 border-t border-border p-5">
							<Button type="button" variant="outline" onClick={() => setIsOpen(false)} disabled={isExporting}>
								Hủy
							</Button>
							<Button type="button" onClick={handleExport} disabled={isExporting} className="gap-2">
								<Download className="w-4 h-4" />
								{isExporting ? "Đang xuất..." : "Tải file"}
							</Button>
						</div>
					</div>
				</div>
			)}
		</>
	);
}
