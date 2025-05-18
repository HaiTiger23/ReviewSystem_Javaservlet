# API Cập nhật đánh giá của người dùng

API này cho phép người dùng cập nhật đánh giá của họ về một sản phẩm.

## Endpoint

```http
POST /api/user/{reviewId}
```

## Headers

| Tên            | Giá trị              | Mô tả                           |
|----------------|----------------------|--------------------------------|
| Content-Type   | application/json     | Định dạng dữ liệu gửi lên      |
| Authorization  | Bearer {token}       | JWT token xác thực người dùng   |

## Parameters

### Path Parameters

| Tên      | Kiểu    | Mô tả                    |
|----------|---------|--------------------------|
| reviewId | integer | ID của đánh giá cần sửa  |

### Request Body

```json
{
    "rating": integer,
    "content": string
}
```

| Trường   | Kiểu    | Bắt buộc | Mô tả                                |
|----------|---------|----------|-------------------------------------|
| rating   | integer | Có       | Điểm đánh giá (từ 1 đến 5)          |
| content  | string  | Có       | Nội dung đánh giá                   |

## Response

### Success Response

- **Status Code:** 200 OK
- **Content:**

```json
{
    "message": "Cập nhật đánh giá thành công",
    "review": {
        "id": integer,
        "rating": integer,
        "content": string,
        "helpfulCount": integer,
        "createdAt": datetime,
        "updatedAt": datetime,
        "product": {
            "name": string,
            "slug": string,
            "price": string,
            "rating": string,
            "reviewCount": string,
            "image": string
        }
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

#### Forbidden (403)
Người dùng không có quyền sửa đánh giá này

```json
{
    "error": "Bạn không có quyền cập nhật đánh giá này"
}
```

#### Bad Request (400)
Dữ liệu gửi lên không hợp lệ

```json
{
    "error": "Vui lòng điền đầy đủ thông tin đánh giá"
}
```

hoặc

```json
{
    "error": "Điểm đánh giá phải từ 1 đến 5"
}
```

hoặc

```json
{
    "error": "Dữ liệu không hợp lệ"
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
POST /api/user/123 HTTP/1.1
Host: your-domain.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "rating": 4,
    "content": "Sản phẩm rất tốt, đóng gói cẩn thận, giao hàng nhanh!"
}
```

### Success Response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
    "message": "Cập nhật đánh giá thành công",
    "review": {
        "id": 123,
        "rating": 4,
        "content": "Sản phẩm rất tốt, đóng gói cẩn thận, giao hàng nhanh!",
        "helpfulCount": 5,
        "createdAt": "2024-03-20T10:30:00",
        "updatedAt": "2024-03-20T11:45:00",
        "product": {
            "name": "Áo thun nam",
            "slug": "ao-thun-nam",
            "price": "199000",
            "rating": "4.5",
            "reviewCount": "42",
            "image": "/images/products/ao-thun-nam.jpg"
        }
    }
}
```

## Notes

- API này chỉ cho phép người dùng cập nhật đánh giá của chính họ
- Điểm đánh giá phải là số nguyên từ 1 đến 5
- Nội dung đánh giá không được để trống
- Sau khi cập nhật thành công, API sẽ trả về thông tin đánh giá mới nhất cùng với thông tin sản phẩm
- Thời gian `updatedAt` sẽ được cập nhật tự động
- Rating trung bình của sản phẩm sẽ được tính toán lại sau khi cập nhật đánh giá 