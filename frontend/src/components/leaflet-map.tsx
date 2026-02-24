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

// Decode a Google Encoded Polyline string → [lat, lng][]
function decodePolyline(encoded: string): [number, number][] {
	if (!encoded) return [];
	const poly: [number, number][] = [];
	let index = 0,
		len = encoded.length;
	let lat = 0,
		lng = 0;

	try {
		while (index < len) {
			let b,
				shift = 0,
				result = 0;
			do {
				b = encoded.charCodeAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20 && index < len);

			const dlat = (result & 1) !== 0 ? ~(result >> 1) : result >> 1;
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charCodeAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20 && index < len);

			const dlng = (result & 1) !== 0 ? ~(result >> 1) : result >> 1;
			lng += dlng;

			poly.push([lat / 1e5, lng / 1e5]);
		}
	} catch (e) {
		console.error("Polyline decoding error:", e);
	}

	return poly;
}

const ROUTE_COLORS = ["#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899"];

// Function to calculate bearing between two points
function getBearing(lat1: number, lon1: number, lat2: number, lon2: number) {
	const y = Math.sin((lon2 - lon1) * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180));
	const x = Math.cos(lat1 * (Math.PI / 180)) * Math.sin(lat2 * (Math.PI / 180)) - Math.sin(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) * Math.cos((lon2 - lon1) * (Math.PI / 180));
	return ((Math.atan2(y, x) * (180 / Math.PI) + 360) % 360).toFixed(2);
}

export function LeafletMap({ routes }: LeafletMapProps) {
	const mapContainer = useRef<HTMLDivElement>(null);
	const map = useRef<L.Map | null>(null);
	const routeLayersRef = useRef<L.Layer[]>([]);

	// Initialize map once
	useEffect(() => {
		if (!mapContainer.current || map.current) return;

		map.current = L.map(mapContainer.current).setView([21.0, 105.8], 10);

		L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
			attribution: "© OpenStreetMap contributors",
			maxZoom: 19,
		}).addTo(map.current);

		return () => {
			if (map.current) {
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

		// Clear previous route layers
		routeLayersRef.current.forEach((l) => map.current?.removeLayer(l));
		routeLayersRef.current = [];

		if (!routes || routes.length === 0) return;

		routes.forEach((route, idx) => {
			const color = ROUTE_COLORS[idx % ROUTE_COLORS.length];

			// 1. Draw polyline
			let coordinates: [number, number][] = [];
			if (route.polyline) {
				coordinates = decodePolyline(route.polyline);
			} else if (route.stops && route.stops.length > 0) {
				coordinates = route.stops.filter((s) => s.latitude != null && s.longitude != null).map((s) => [s.latitude!, s.longitude!] as [number, number]);
			}

			if (coordinates.length > 0) {
				// Base line
				const polyline = L.polyline(coordinates, { color, weight: 5, opacity: 0.8 })
					.bindPopup(
						`<div style="padding:8px;font-size:12px">
                <strong>Tuyến ${idx + 1}</strong><br>
                Khoảng cách: ${route.totalDistanceKm.toFixed(2)} km<br>
                Thời gian: ${route.totalDurationMin} phút
              </div>`,
					)
					.addTo(map.current!);
				routeLayersRef.current.push(polyline);

				// Add directional arrows every N points or every M distance
				const arrowInterval = Math.max(5, Math.floor(coordinates.length / 15));
				for (let i = 0; i < coordinates.length - 1; i += arrowInterval) {
					const p1 = coordinates[i];
					const p2 = coordinates[i + 1];
					const angle = getBearing(p1[0], p1[1], p2[0], p2[1]);

					const arrowIcon = L.divIcon({
						html: `<div style="transform: rotate(${angle}deg); color: #f87171; filter: drop-shadow(0 0 2px white) drop-shadow(0 0 1px white);">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 19V5M5 12l7-7 7 7"/>
                    </svg>
                  </div>`,
						className: "arrow-icon",
						iconSize: [20, 20],
						iconAnchor: [10, 10],
					});

					const midpoint: [number, number] = [(p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2];
					const arrowMarker = L.marker(midpoint, { icon: arrowIcon, interactive: false }).addTo(map.current!);
					routeLayersRef.current.push(arrowMarker);
				}
			}

			// 2. Draw stop markers
			if (route.stops && route.stops.length > 0) {
				route.stops.forEach((stop) => {
					if (stop.latitude == null || stop.longitude == null) return;

					const isDepot = !stop.orderId; // Depot stops have no orderId

					const stopIcon = L.divIcon({
						html: `<div style="
                width: 24px; height: 24px;
                background: ${isDepot ? "#4b5563" : color};
                border: 2px solid white;
                border-radius: 50%;
                display: flex; align-items: center; justify-content: center;
                color: white; font-size: 11px; font-weight: bold;
                box-shadow: 0 1px 4px rgba(0,0,0,0.4);
              ">
                ${
									isDepot
										? `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21V10a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v11"/><path d="M8 21v-7a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v7"/><path d="M2 8l10-6 10 6"/></svg>`
										: stop.stopSequence
								}
              </div>`,
						className: "custom-icon",
						iconSize: [24, 24],
						iconAnchor: [12, 12],
						popupAnchor: [0, -14],
					});

					const marker = L.marker([stop.latitude!, stop.longitude!], { icon: stopIcon })
						.bindPopup(
							`<div style="padding:8px;font-size:12px">
                <strong>${isDepot ? "Điểm Kho (Depot)" : `Điểm dừng #${stop.stopSequence}`}</strong><br>
                ${stop.orderId ? `Đơn hàng ID: ${stop.orderId}<br>` : ""}
                Khoảng cách: ${stop.distanceFromPrevKm?.toFixed(2) ?? "0"} km<br>
                Thời gian: ${stop.durationFromPrevMin ?? "0"} phút
              </div>`,
						)
						.addTo(map.current!);

					routeLayersRef.current.push(marker);
				});
			}
		});

		// Zoom to fit all layers
		if (routeLayersRef.current.length > 0) {
			const group = L.featureGroup(routeLayersRef.current.filter((l) => !(l instanceof L.Marker && (l.options.icon as any).options.className === "arrow-icon")));
			if (group.getLayers().length > 0) {
				map.current.fitBounds(group.getBounds(), { padding: [50, 50] });
			}
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
