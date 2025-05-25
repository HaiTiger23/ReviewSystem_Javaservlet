# Tài liệu API Quản lý Người dùng (Admin)

## Tổng quan
API này cung cấp các chức năng quản lý người dùng cho quản trị viên. Tất cả các yêu cầu đều yêu cầu quyền admin.

## Các endpoint

### 1. Lấy danh sách người dùng (Phân trang và tìm kiếm)

**Endpoint:** `GET /admin/users`

**Tham số truy vấn:**
- `page` (tùy chọn, mặc định: 1): Trang hiện tại
- `limit` (tùy chọn, mặc định: 10): Số lượng bản ghi mỗi trang
- `search` (tùy chọn): Từ khóa tìm kiếm (tìm theo tên hoặc email)

**Yêu cầu xác thực:** Có (Admin)

**Ví dụ yêu cầu:**
```http
GET /admin/users?page=1&limit=10&search=john
```

**Phản hồi thành công (200 OK):**
```json
{
  "success": true,
  "users": [
    {
      "id": 1,
      "email": "admin@example.com",
      "name": "Admin",
      "role": "ADMIN",
      "createdAt": "2025-05-25T10:00:00Z"
    }
    // ... các user khác
  ],
  "total": 15,
  "page": 1,
  "limit": 10,
  "totalPages": 2
}
```

### 2. Cập nhật thông tin người dùng

**Endpoint:** `PUT /admin/users/{userId}`

**Tham số đường dẫn:**
- `userId` (bắt buộc): ID của người dùng cần cập nhật

**Dữ liệu yêu cầu (JSON):**
```json
{
  "email": "new.email@example.com",
  "name": "Tên mới",
  "role": "ADMIN" // hoặc "USER"
}
```

**Yêu cầu xác thực:** Có (Admin)

**Ví dụ yêu cầu:**
```http
PUT /admin/users/1
Content-Type: application/json

{
  "email": "admin.updated@example.com",
  "name": "Admin Updated",
  "role": "ADMIN"
}
```

**Phản hồi thành công (200 OK):**
```json
{
  "success": true,
  "message": "Cập nhật thông tin người dùng thành công",
  "user": {
    "id": 1,
    "email": "admin.updated@example.com",
    "name": "Admin Updated",
    "role": "ADMIN",
    "createdAt": "2025-05-25T10:00:00Z"
  }
}
```

## Mã lỗi

- **400 Bad Request**: Dữ liệu không hợp lệ (thiếu trường bắt buộc, định dạng không đúng)
- **403 Forbidden**: Không có quyền truy cập (không phải admin)
- **404 Not Found**: Không tìm thấy người dùng
- **500 Internal Server Error**: Lỗi server

## Ghi chú

1. Tất cả các API đều yêu cầu xác thực (token JWT)
2. Chỉ có admin mới có quyền truy cập các API này
3. Khi cập nhật thông tin, chỉ những trường được gửi lên mới được cập nhật
4. Role phải là một trong các giá trị: "ADMIN" hoặc "USER" (không phân biệt hoa thường)
