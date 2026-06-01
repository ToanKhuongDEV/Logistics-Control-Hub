export type ExcelFileType = "DEPOT" | "DRIVER" | "ORDER" | "ROUTING" | "VEHICLE";

export interface ExcelExportParams {
	type: ExcelFileType;
	search?: string;
	status?: string;
	depotId?: number;
	fromDate?: string;
	toDate?: string;
	maxRows?: number;
}
