# API Xóa đánh giá của người dùng

API này cho phép người dùng xóa đánh giá của họ về một sản phẩm.

## Endpoint

```http
DELETE /api/user/{reviewId}
```

## Headers

| Tên            | Giá trị              | Mô tả                           |
|----------------|----------------------|--------------------------------|
| Authorization  | Bearer {token}       | JWT token xác thực người dùng   |

## Parameters

### Path Parameters

| Tên      | Kiểu    | Mô tả                    |
|----------|---------|--------------------------|
| reviewId | integer | ID của đánh giá cần xóa  |

## Response

### Success Response

- **Status Code:** 200 OK
- **Content:**

```json
{
    "message": "Xóa đánh giá thành công"
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

#### Forbidden (403)
Người dùng không có quyền xóa đánh giá này

```json
{
    "error": "Không có quyền xóa đánh giá này"
}
```

#### Not Found (404)
Không tìm thấy đánh giá

```json
{
    "error": "Không tìm thấy đánh giá"
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
DELETE /api/user/123 HTTP/1.1
Host: your-domain.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Success Response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
    "message": "Xóa đánh giá thành công"
}
```

## Notes

- API này chỉ cho phép người dùng xóa đánh giá của chính họ
- Sau khi xóa thành công, rating trung bình của sản phẩm sẽ được tính toán lại
- Tất cả các dữ liệu liên quan đến đánh giá (bao gồm cả lượt đánh dấu hữu ích) sẽ bị xóa 