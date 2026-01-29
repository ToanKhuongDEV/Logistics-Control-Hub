"use client"

import React from "react"

export function TrackingMap() {
  // Truck positions on US map (approximate coordinates)
  const trucks = [
    { id: 1, x: 85, y: 25 },
    { id: 2, x: 75, y: 35 },
    { id: 3, x: 65, y: 40 },
    { id: 4, x: 55, y: 45 },
    { id: 5, x: 45, y: 35 },
    { id: 6, x: 40, y: 50 },
    { id: 7, x: 35, y: 45 },
    { id: 8, x: 30, y: 55 },
    { id: 9, x: 25, y: 50 },
    { id: 10, x: 20, y: 60 },
    { id: 11, x: 15, y: 45 },
    { id: 12, x: 10, y: 55 },
  ]

  return (
    <div className="relative w-full h-96 bg-gradient-to-br from-card via-background to-card rounded-xl border border-border overflow-hidden">
      {/* SVG Background */}
      <svg
        className="absolute inset-0 w-full h-full"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 100 70"
        preserveAspectRatio="none"
      >
        {/* Grid lines representing US map */}
        <g strokeWidth="0.15" stroke="oklch(0.25 0.03 260)" fill="none">
          {/* Vertical lines */}
          {Array.from({ length: 11 }).map((_, i) => (
            <line key={`v${i}`} x1={i * 10} y1="0" x2={i * 10} y2="70" />
          ))}
          {/* Horizontal lines */}
          {Array.from({ length: 8 }).map((_, i) => (
            <line key={`h${i}`} x1="0" y1={i * 10} x2="100" y2={i * 10} />
          ))}
        </g>

        {/* Route connections */}
        <g strokeWidth="0.4" stroke="oklch(0.68 0.18 45)" fill="none" opacity="0.6">
          <path d="M 85 25 Q 75 30 65 40 T 45 35 T 30 55" />
          <path d="M 75 35 Q 55 40 40 50 T 20 60" />
          <path d="M 55 45 Q 40 48 25 50 T 15 45" />
        </g>

        {/* Truck icons */}
        {trucks.map((truck) => (
          <g key={truck.id}>
            {/* Truck Icon - Simple square with rounded corners */}
            <rect
              x={truck.x - 1.5}
              y={truck.y - 1.2}
              width="3"
              height="2.4"
              rx="0.3"
              fill="oklch(0.68 0.18 45)"
            />
            {/* Truck bed detail */}
            <rect
              x={truck.x - 1.2}
              y={truck.y - 0.8}
              width="2.4"
              height="1.6"
              rx="0.2"
              fill="none"
              stroke="oklch(0.68 0.18 45)"
              strokeWidth="0.2"
            />
          </g>
        ))}
      </svg>

      {/* Animated pulse effect */}
      <div className="absolute inset-0 pointer-events-none">
        {trucks.slice(0, 3).map((truck, i) => (
          <div
            key={`pulse-${i}`}
            className="absolute w-1.5 h-1.5 rounded-full bg-accent animate-pulse"
            style={{
              left: `${truck.x}%`,
              top: `${truck.y}%`,
              animation: `pulse ${2 + i * 0.3}s cubic-bezier(0, 0, 0.2, 1) infinite`,
            }}
          />
        ))}
      </div>
    </div>
  )
}
