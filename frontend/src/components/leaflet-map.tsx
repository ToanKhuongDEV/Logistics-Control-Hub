"use client";

import React, { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// Fix for Leaflet default icons
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
	iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
	iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
	shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
});

interface VehicleLocation {
	id: number;
	code: string;
	lat: number;
	lng: number;
	status: "ACTIVE" | "MAINTENANCE" | "IDLE";
	city: string;
	driver?: string;
}

// Sample vehicle locations across Vietnam
const vehicleLocations: VehicleLocation[] = [
	{ id: 1, code: "VH001", lat: 21.0285, lng: 105.8542, status: "ACTIVE", city: "Hà Nội", driver: "Nguyễn Văn A" },
	{ id: 2, code: "VH002", lat: 10.8231, lng: 106.6797, status: "ACTIVE", city: "TP.HCM", driver: "Trần Thị B" },
	{ id: 3, code: "VH003", lat: 16.0544, lng: 108.2022, status: "MAINTENANCE", city: "Đà Nẵng", driver: "Lê Văn C" },
	{ id: 4, code: "VH004", lat: 20.8449, lng: 106.6881, status: "ACTIVE", city: "Hải Phòng", driver: "Phạm Thị D" },
	{ id: 5, code: "VH005", lat: 18.6762, lng: 105.6938, status: "ACTIVE", city: "Thanh Hóa", driver: "Hoàng Văn E" },
	{ id: 6, code: "VH006", lat: 12.2381, lng: 109.1967, status: "IDLE", city: "Nha Trang" },
	{ id: 7, code: "VH007", lat: 10.0452, lng: 105.7469, status: "ACTIVE", city: "Cần Thơ", driver: "Đỗ Thị F" },
	{ id: 8, code: "VH008", lat: 11.9404, lng: 108.4583, status: "ACTIVE", city: "Đà Lạt", driver: "Vũ Văn G" },
	{ id: 9, code: "VH009", lat: 21.5941, lng: 105.8446, status: "MAINTENANCE", city: "Vĩnh Phúc", driver: "Bùi Thị H" },
	{ id: 10, code: "VH010", lat: 10.371, lng: 107.0924, status: "ACTIVE", city: "Vũng Tàu", driver: "Mai Văn I" },
];

export function LeafletMap() {
	const mapContainer = useRef<HTMLDivElement>(null);
	const map = useRef<L.Map | null>(null);
	const markersRef = useRef<L.Marker[]>([]);

	useEffect(() => {
		if (!mapContainer.current || map.current) return;

		// Initialize map - centered on Vietnam
		map.current = L.map(mapContainer.current).setView([15.5, 106.5], 6);

		// Add OpenStreetMap tiles
		L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
			attribution: "© OpenStreetMap contributors",
			maxZoom: 19,
		}).addTo(map.current);

		// Add vehicle markers
		vehicleLocations.forEach((vehicle) => {
			const color = vehicle.status === "ACTIVE" ? "#3cba54" : vehicle.status === "MAINTENANCE" ? "#fbbf24" : "#9ca3af";

			const html = `
        <div style="
          width: 32px;
          height: 32px;
          background-color: ${color};
          border: 2px solid white;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 8px rgba(0,0,0,0.3);
          font-size: 16px;
        ">
          🚚
        </div>
      `;

			const customIcon = L.divIcon({
				html,
				className: "custom-icon",
				iconSize: [32, 32],
				iconAnchor: [16, 16],
				popupAnchor: [0, -16],
			});

			const statusText = vehicle.status === "ACTIVE" ? "✓ Hoạt động" : vehicle.status === "MAINTENANCE" ? "⚙ Bảo trì" : "⊗ Đang rảnh";

			const driverInfo = vehicle.driver ? `<br><span style="color: #666;">Tài xế: ${vehicle.driver}</span>` : "";

			const marker = L.marker([vehicle.lat, vehicle.lng], { icon: customIcon })
				.bindPopup(
					`<div style="padding: 8px; font-size: 12px;">
            <strong>${vehicle.code}</strong><br>
            <span style="color: #666;">${vehicle.city}</span>${driverInfo}<br>
            Trạng thái: <strong style="color: ${color};">${statusText}</strong>
          </div>`,
				)
				.addTo(map.current!);

			markersRef.current.push(marker);
		});

		return () => {
			// Cleanup
			if (map.current) {
				markersRef.current.forEach((marker) => map.current?.removeLayer(marker));
				markersRef.current = [];
				map.current.remove();
				map.current = null;
			}
		};
	}, []);

	return (
		<div className="relative w-full h-96 bg-card rounded-xl border border-border overflow-hidden">
			<div
				ref={mapContainer}
				className="w-full h-full"
				style={{
					background: "#f8fafc",
				}}
			/>
			<style jsx>{`
				:global(.leaflet-container) {
					background: #f8fafc;
				}
				:global(.leaflet-tile-pane) {
					filter: hue-rotate(200deg) saturate(0.8);
				}
				:global(.custom-icon) {
					background: none !important;
					border: none !important;
				}
			`}</style>
		</div>
	);
}
