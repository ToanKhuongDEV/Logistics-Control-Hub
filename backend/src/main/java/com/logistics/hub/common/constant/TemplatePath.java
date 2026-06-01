package com.logistics.hub.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TemplatePath {

    VEHICLE("templates/import/vehicle_import_template.xlsx", "templates/export/vehicle_export_template.xlsx"),
    DEPOT("templates/import/depot_import_template.xlsx", "templates/export/depot_export_template.xlsx"),
    ORDER("templates/import/order_import_template.xlsx", "templates/export/order_export_template.xlsx"),
    DRIVER("templates/import/driver_import_template.xlsx", "templates/export/driver_export_template.xlsx"),
    ROUTING("templates/import/routing_import_template.xlsx", "templates/export/routing_export_template.xlsx");


    private final String importPath;
    private final String exportPath;


}
