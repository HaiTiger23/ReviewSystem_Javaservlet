# API Quản lý Người dùng (Admin)

## 1. Lấy danh sách người dùng

Lấy danh sách người dùng với phân trang và tìm kiếm.

### Request

```http
GET /admin/users?page={page}&limit={limit}&search={search}
```

### Parameters

| Tham số | Kiểu   | Mô tả                                  |
|---------|--------|----------------------------------------|
| page    | number | Số trang (mặc định: 1)                 |
| limit   | number | Số lượng trên mỗi trang (mặc định: 10) |
| search  | string | Từ khóa tìm kiếm (không bắt buộc)      |

### Headers

| Header        | Value                  | Required |
|---------------|------------------------|----------|
| Authorization | Bearer {admin_token}   | Yes      |

### Response

```json
{
    "users": [
        {
            "id": 1,
            "email": "user@example.com",
            "fullName": "Nguyễn Văn A",
            "phone": "0123456789",
            "role": "user",
            "created_at": "2024-03-15T10:30:00Z",
            "updated_at": "2024-03-15T10:30:00Z"
        }
    ],
    "pagination": {
        "total": 100,
        "page": 1,
        "limit": 10,
        "totalPages": 10
    }
}
```

## 2. Cập nhật thông tin người dùng

Cập nhật thông tin và quyền của người dùng.

### Request

```http
PUT /admin/users/{id}
```

### Headers

| Header         | Value                | Required |
|----------------|---------------------|----------|
| Authorization  | Bearer {admin_token} | Yes      |
| Content-Type   | application/json    | Yes      |

### Body

```json
{
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "role": "admin"
}
```

### Response

```json
{
    "success": true,
    "message": "Cập nhật thông tin người dùng thành công",
    "user": {
        "id": 1,
        "email": "user@example.com",
        "fullName": "Nguyễn Văn A",
        "phone": "0123456789",
        "role": "admin",
        "created_at": "2024-03-15T10:30:00Z",
        "updated_at": "2024-03-15T11:00:00Z"
    }
}
```

## Mã lỗi

| Mã    | Mô tả                                           |
|-------|------------------------------------------------|
| 400   | Dữ liệu không hợp lệ                           |
| 403   | Không có quyền truy cập                        |
| 404   | Không tìm thấy người dùng                      |
| 500   | Lỗi server                                     |

## Ví dụ lỗi

```json
{
    "success": false,
    "message": "Email không được để trống"
}
``` 