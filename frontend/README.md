# TechByte Service Desk — Frontend

React 19, React Router 7, Vite 8 và Axios. Phiên bản dependencies được khóa trong
`package-lock.json`; Node 22 (từ 22.12 trở lên) được dùng trong Docker và CI.

## Chạy frontend để phát triển

```bash
cd frontend
npm ci
npm run dev
```

Vite dùng cổng 5173 và proxy `/api` tới backend ở `http://localhost:8080`.
Đây là Vite dev server, không yêu cầu cấu hình Nginx trên máy phát triển.

```bash
npm run lint
npm run test:ci
npm run build
```

API đăng nhập nhận `email`/`password`, trả `accessToken` và `user`. Các adapter trong
`src/api/contract.js` ánh xạ response snake_case của backend cho giao diện. Lịch sử
và bình luận được lấy trong response chi tiết ticket, không gọi endpoint riêng.

## Triển khai trên VPS

Dùng `frontend/Dockerfile` với build context là root repository. Bản React production
được build với API `/api`, cùng origin với giao diện; Nginx phục vụ thư mục `dist`
và fallback về `index.html` cho React Router.

Xem [Docker/Nginx trên VPS](../docs/nginx-server.md) và [CI/CD](../docs/deployment.md).
