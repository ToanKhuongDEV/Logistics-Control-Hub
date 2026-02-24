"use client";

import React, { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { Route } from "@/lib/routing-api";

// Fix for Leaflet default icons
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
	iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
	iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
	shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
});

interface LeafletMapProps {
	routes?: Route[];
}

// Sample vehicle locations across Vietnam (static display)
const vehicleLocations = [
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

// Decode a single Google Encoded Polyline segment → [lat, lng][]
function decodePolylineSegment(encoded: string): [number, number][] {
	const poly: [number, number][] = [];
	let index = 0;
	let lat = 0;
	let lng = 0;

	while (index < encoded.length) {
		let shift = 0;
		let result = 0;
		let byte: number;

		do {
			byte = encoded.charCodeAt(index++) - 63;
			result |= (byte & 0x1f) << shift;
			shift += 5;
		} while (byte >= 0x20);

		const deltaLat = (result & 1) !== 0 ? ~(result >> 1) : result >> 1;
		lat += deltaLat;

		shift = 0;
		result = 0;

		do {
			byte = encoded.charCodeAt(index++) - 63;
			result |= (byte & 0x1f) << shift;
			shift += 5;
		} while (byte >= 0x20);

		const deltaLng = (result & 1) !== 0 ? ~(result >> 1) : result >> 1;
		lng += deltaLng;

		poly.push([lat / 1e5, lng / 1e5]);
	}

	return poly;
}

// Decode a full polyline string that may contain multiple "|"-separated segments
function decodePolyline(polylineStr: string): [number, number][] {
	if (!polylineStr) return [];
	const segments = polylineStr.split("|");
	return segments.flatMap((seg) => decodePolylineSegment(seg));
}

const ROUTE_COLORS = ["#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899"];

export function LeafletMap({ routes }: LeafletMapProps) {
	const mapContainer = useRef<HTMLDivElement>(null);
	const map = useRef<L.Map | null>(null);
	const vehicleMarkersRef = useRef<L.Marker[]>([]);
	const routeLayersRef = useRef<L.Layer[]>([]);

	// Initialize map once
	useEffect(() => {
		if (!mapContainer.current || map.current) return;

		map.current = L.map(mapContainer.current).setView([15.5, 106.5], 6);

		L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
			attribution: "© OpenStreetMap contributors",
			maxZoom: 19,
		}).addTo(map.current);

		// Add static vehicle markers
		vehicleLocations.forEach((vehicle) => {
			const color = vehicle.status === "ACTIVE" ? "#3cba54" : vehicle.status === "MAINTENANCE" ? "#fbbf24" : "#9ca3af";

			const html = `
        <div style="
          width: 32px; height: 32px;
          background-color: ${color};
          border: 2px solid white;
          border-radius: 50%;
          display: flex; align-items: center; justify-content: center;
          box-shadow: 0 2px 8px rgba(0,0,0,0.3);
          font-size: 16px;
        ">🚚</div>
      `;

			const customIcon = L.divIcon({ html, className: "custom-icon", iconSize: [32, 32], iconAnchor: [16, 16], popupAnchor: [0, -16] });

			const statusText = vehicle.status === "ACTIVE" ? "✓ Hoạt động" : vehicle.status === "MAINTENANCE" ? "⚙ Bảo trì" : "⊗ Đang rảnh";
			const driverInfo = vehicle.driver ? `<br><span style="color:#666">Tài xế: ${vehicle.driver}</span>` : "";

			const marker = L.marker([vehicle.lat, vehicle.lng], { icon: customIcon })
				.bindPopup(
					`<div style="padding:8px;font-size:12px">
            <strong>${vehicle.code}</strong><br>
            <span style="color:#666">${vehicle.city}</span>${driverInfo}<br>
            Trạng thái: <strong style="color:${color}">${statusText}</strong>
          </div>`,
				)
				.addTo(map.current!);

			vehicleMarkersRef.current.push(marker);
		});

		return () => {
			if (map.current) {
				vehicleMarkersRef.current.forEach((m) => map.current?.removeLayer(m));
				vehicleMarkersRef.current = [];
				routeLayersRef.current.forEach((l) => map.current?.removeLayer(l));
				routeLayersRef.current = [];
				map.current.remove();
				map.current = null;
			}
		};
	}, []);

	// Render routes & stop markers when routes prop changes
	useEffect(() => {
		if (!map.current) return;

		// Clear previous route layers (polylines + stop markers)
		routeLayersRef.current.forEach((l) => map.current?.removeLayer(l));
		routeLayersRef.current = [];

		if (!routes || routes.length === 0) return;

		routes.forEach((route, idx) => {
			const color = ROUTE_COLORS[idx % ROUTE_COLORS.length];

			// Draw polyline
			if (route.polyline) {
				const coordinates = decodePolyline(route.polyline);
				if (coordinates.length > 0) {
					const polyline = L.polyline(coordinates, { color, weight: 5, opacity: 0.8 })
						.bindPopup(
							`<div style="padding:8px;font-size:12px">
                <strong>Tuyến ${idx + 1}</strong><br>
                Xe ID: ${route.vehicleId}<br>
                Khoảng cách: ${route.totalDistanceKm.toFixed(2)} km<br>
                Thời gian: ${route.totalDurationMin} phút
              </div>`,
						)
						.addTo(map.current!);
					routeLayersRef.current.push(polyline);
				}
			}

			// Draw stop markers
			if (route.stops && route.stops.length > 0) {
				route.stops.forEach((stop) => {
					if (stop.latitude == null || stop.longitude == null) return;

					const stopIcon = L.divIcon({
						html: `<div style="
                width:24px;height:24px;
                background:${color};
                border:2px solid white;
                border-radius:50%;
                display:flex;align-items:center;justify-content:center;
                color:white;font-size:11px;font-weight:bold;
                box-shadow:0 1px 4px rgba(0,0,0,0.4);
              ">${stop.stopSequence}</div>`,
						className: "custom-icon",
						iconSize: [24, 24],
						iconAnchor: [12, 12],
						popupAnchor: [0, -14],
					});

					const marker = L.marker([stop.latitude!, stop.longitude!], { icon: stopIcon })
						.bindPopup(
							`<div style="padding:8px;font-size:12px">
                <strong>Điểm dừng #${stop.stopSequence}</strong><br>
                Đơn hàng ID: ${stop.orderId ?? "—"}<br>
                Khoảng cách từ trước: ${stop.distanceFromPrevKm?.toFixed(2) ?? "—"} km<br>
                Thời gian từ trước: ${stop.durationFromPrevMin ?? "—"} phút
              </div>`,
						)
						.addTo(map.current!);

					routeLayersRef.current.push(marker);
				});
			}
		});

		// Zoom bản đồ vào vùng chứa tất cả các tuyến
		if (routeLayersRef.current.length > 0) {
			const group = L.featureGroup(routeLayersRef.current);
			map.current.fitBounds(group.getBounds(), { padding: [50, 50] });
		}
	}, [routes]);

	return (
		<div className="relative w-full h-96 bg-card rounded-xl border border-border overflow-hidden">
			<div ref={mapContainer} className="w-full h-full" style={{ background: "#f8fafc" }} />
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
