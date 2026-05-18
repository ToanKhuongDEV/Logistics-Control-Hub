import apiClient from "./api";
import { ExcelExportParams, ExcelFileType } from "@/types/excel-types";

const EXCEL_EXPORT_API_BASE = "/api/v1/excel/export";

function appendParam(queryParams: URLSearchParams, key: string, value: string | number | undefined) {
	if (value !== undefined && value !== "") {
		queryParams.append(key, String(value));
	}
}

function fallbackFilename(type: ExcelFileType) {
	return `${type.toLowerCase()}-export-${new Date().toISOString().slice(0, 10)}.xlsx`;
}

function filenameFromContentDisposition(contentDisposition: string | undefined, type: ExcelFileType) {
	if (!contentDisposition) {
		return fallbackFilename(type);
	}

	const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
	if (utf8Match?.[1]) {
		return decodeURIComponent(utf8Match[1].replace(/"/g, ""));
	}

	const filenameMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
	return filenameMatch?.[1] ?? fallbackFilename(type);
}

function saveBlob(blob: Blob, filename: string) {
	const downloadUrl = window.URL.createObjectURL(blob);
	const link = document.createElement("a");
	link.href = downloadUrl;
	link.download = filename;
	document.body.appendChild(link);
	link.click();
	link.remove();
	window.URL.revokeObjectURL(downloadUrl);
}

export const excelApi = {
	async export(params: ExcelExportParams): Promise<void> {
		const queryParams = new URLSearchParams();
		appendParam(queryParams, "type", params.type);
		appendParam(queryParams, "search", params.search);
		appendParam(queryParams, "status", params.status);
		appendParam(queryParams, "depotId", params.depotId);
		appendParam(queryParams, "fromDate", params.fromDate);
		appendParam(queryParams, "toDate", params.toDate);
		appendParam(queryParams, "maxRows", params.maxRows);

		const response = await apiClient.get<Blob>(`${EXCEL_EXPORT_API_BASE}?${queryParams.toString()}`, {
			responseType: "blob",
		});

		const contentDisposition = response.headers["content-disposition"];
		const filename = filenameFromContentDisposition(typeof contentDisposition === "string" ? contentDisposition : undefined, params.type);
		saveBlob(response.data, filename);
	},
};
