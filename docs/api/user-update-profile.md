# API Cập nhật thông tin người dùng

API này cho phép người dùng cập nhật thông tin cá nhân của họ.

## Endpoint

```http
POST /api/auth/update-profile
```

## Headers

| Tên            | Giá trị              | Mô tả                           |
|----------------|----------------------|--------------------------------|
| Content-Type   | application/json     | Định dạng dữ liệu gửi lên      |
| Authorization  | Bearer {token}       | JWT token xác thực người dùng   |

## Request Body

```json
{
    "name": "Tên mới",
    "password": "Mật khẩu mới",
    "confirmPassword": "Xác nhận mật khẩu mới"
}
```

| Trường          | Kiểu    | Bắt buộc | Mô tả                                |
|----------------|---------|----------|-------------------------------------|
| name           | string  | Có       | Tên mới của người dùng              |
| password       | string  | Không    | Mật khẩu mới (nếu muốn đổi)        |
| confirmPassword| string  | Không*   | Xác nhận mật khẩu mới              |

(*) Bắt buộc nếu có trường password

## Response

### Success Response

- **Status Code:** 200 OK
- **Content:**

```json
{
    "message": "Cập nhật thông tin thành công",
    "user": {
        "id": 123,
        "name": "Tên mới",
        "email": "user@example.com",
        "avatar": "https://example.com/avatar.jpg"
    }
}
```

### Error Responses

#### Unauthorized (401)
Người dùng chưa đăng nhập hoặc token không hợp lệ

```json
{
    "error": "Chưa đăng nhập"
}
```

hoặc

```json
{
    "error": "Token không hợp lệ"
}
```

#### Bad Request (400)
Dữ liệu gửi lên không hợp lệ

```json
{
    "error": "Tên không được để trống"
}
```

hoặc

```json
{
    "error": "Mật khẩu phải có ít nhất 6 ký tự"
}
```

hoặc

```json
{
    "error": "Mật khẩu xác nhận không khớp"
}
```

#### Internal Server Error (500)
Lỗi server

```json
{
    "error": "Có lỗi xảy ra: {chi tiết lỗi}"
}
```

## Ví dụ

### Request

```http
POST /api/auth/update-profile HTTP/1.1
Host: your-domain.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "name": "Nguyễn Văn A",
    "password": "newpassword123",
    "confirmPassword": "newpassword123"
}
```

### Success Response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
    "message": "Cập nhật thông tin thành công",
    "user": {
        "id": 123,
        "name": "Nguyễn Văn A",
        "email": "user@example.com",
        "avatar": "https://example.com/avatar.jpg"
    }
}
```

## Notes

- API này yêu cầu người dùng đã đăng nhập (có JWT token hợp lệ)
- Trường `name` là bắt buộc và không được để trống
- Trường `password` và `confirmPassword` là tùy chọn. Nếu muốn đổi mật khẩu:
  - Phải cung cấp cả hai trường
  - Mật khẩu phải có ít nhất 6 ký tự
  - Mật khẩu xác nhận phải khớp với mật khẩu mới
- Nếu không muốn đổi mật khẩu, có thể bỏ qua hai trường này
- Thông tin email không thể thay đổi qua API này 