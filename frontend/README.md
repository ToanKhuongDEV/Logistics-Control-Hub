# Logistics Control Hub Frontend

This folder contains the Next.js dashboard for Logistics Control Hub.

## Stack

- Next.js 16 with App Router
- React 19
- TypeScript
- Tailwind CSS v4
- Radix UI and shadcn-style local components
- Axios with `withCredentials` for HttpOnly authentication cookies
- Leaflet for maps
- Recharts for dashboard charts
- React Hook Form and Zod for forms
- Framer Motion for small UI transitions

## Pages

| Route | Purpose |
| --- | --- |
| `/login` | Sign in |
| `/forgot-password` | Request password reset email |
| `/reset-password` | Reset password with token |
| `/dashboard` | Operational overview |
| `/orders` | Order management |
| `/fleet` | Vehicle management |
| `/drivers` | Driver management |
| `/depots` | Depot management |
| `/history` | Routing run history |
| `/driver` | Driver delivery portal |
| `/accounts` | Admin account management |
| `/audit` | Audit log search |
| `/settings` | Company and settings area |

Menu visibility is permission-based. The permission list comes from the backend `/api/v1/auth/me` response.

## Environment

Create `frontend/.env.local` from `frontend/.env.local.example`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

`NEXT_PUBLIC_API_URL` is used by `src/lib/api.ts` as the Axios base URL. In Docker builds it must be passed at build time because this is a public Next.js environment variable.

## Local Development

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

## Production Build

```bash
cd frontend
npm run build
npm run start
```

The Dockerfile uses `output: "standalone"` from `next.config.ts` and starts the generated `server.js` on port `3000`.
