# Tài liệu API Hệ thống Đánh giá Sản phẩm

## Giới thiệu

Tài liệu này mô tả các API của hệ thống đánh giá sản phẩm. Hệ thống cho phép người dùng thêm, cập nhật, xóa đánh giá và đánh dấu đánh giá là hữu ích.

## Xác thực

Tất cả các API đánh giá đều yêu cầu xác thực bằng JWT token. Token này được lấy từ API đăng nhập.

### Đăng nhập

```
POST /api/auth/login
```

**Request Body:**

```json
{
  "email": "example@example.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "user": {
    "name": "Tên người dùng",
    "id": 5,
    "email": "example@example.com"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## API Đánh giá

### 1. Lấy danh sách đánh giá của sản phẩm

```
GET /api/product-reviews/{productId}
```

**Tham số URL:**

- `productId`: ID của sản phẩm

**Tham số Query:**

- `page`: Số trang (mặc định: 1)
- `limit`: Số lượng đánh giá trên mỗi trang (mặc định: 10)
- `sort`: Sắp xếp đánh giá (các giá trị: "newest", "oldest", "highest", "lowest")

**Headers:**

```
Content-Type: application/json
Authorization: Bearer {token}
```

**Response thành công (200 OK):**

```json
{
  "pagination": {
    "total": 10,
    "limit": 10,
    "totalPages": 1,
    "page": 1
  },
  "reviews": [
    {
      "id": 3,
      "rating": 5,
      "content": "Sản phẩm tuyệt vời, xứng đáng 5 sao!",
      "date": "04/05/2025",
      "userId": 5,
      "userName": "Doraemon",
      "helpfulCount": 1,
      "isHelpful": true
    },
    // ...
  ]
}
```

### 2. Thêm đánh giá mới

```
POST /api/product-reviews/{productId}
```

**Tham số URL:**

- `productId`: ID của sản phẩm

**Headers:**

```
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:**

```json
{
  "rating": 5,
  "content": "Sản phẩm tuyệt vời, xứng đáng 5 sao!"
}
```

**Response thành công (201 Created):**

```json
{
  "id": 4,
  "rating": 5,
  "content": "Sản phẩm tuyệt vời, xứng đáng 5 sao!",
  "date": "04/05/2025",
  "message": "Đánh giá của bạn đã được gửi thành công"
}
```

**Response lỗi (400 Bad Request):**

```json
{
  "error": "Vui lòng điền đầy đủ thông tin đánh giá"
}
```

**Response lỗi (403 Forbidden):**

```json
{
  "error": "Bạn đã đánh giá sản phẩm này trước đó"
}
```

### 3. Cập nhật đánh giá

```
PUT /api/reviews/{reviewId}
```

**Tham số URL:**

- `reviewId`: ID của đánh giá

**Headers:**

```
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:**

```json
{
  "rating": 4,
  "content": "Sản phẩm tốt, nhưng có thể cải thiện thêm"
}
```

**Response thành công (200 OK):**

```json
{
  "id": 3,
  "message": "Cập nhật đánh giá thành công"
}
```

**Response lỗi (403 Forbidden):**

```json
{
  "error": "Bạn không có quyền cập nhật đánh giá này"
}
```

**Response lỗi (404 Not Found):**

```json
{
  "error": "Không tìm thấy đánh giá"
}
```

### 4. Xóa đánh giá

```
DELETE /api/reviews/{reviewId}
```

**Tham số URL:**

- `reviewId`: ID của đánh giá

**Headers:**

```
Content-Type: application/json
Authorization: Bearer {token}
```

**Response thành công (200 OK):**

```json
{
  "message": "Xóa đánh giá thành công"
}
```

**Response lỗi (403 Forbidden):**

```json
{
  "error": "Bạn không có quyền xóa đánh giá này"
}
```

**Response lỗi (404 Not Found):**

```json
{
  "error": "Không tìm thấy đánh giá"
}
```

### 5. Đánh dấu đánh giá là hữu ích

```
POST /api/reviews/{reviewId}/helpful
```

**Tham số URL:**

- `reviewId`: ID của đánh giá

**Headers:**

```
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:**

```json
{
  "isHelpful": true
}
```

**Response thành công (200 OK):**

```json
{
  "id": 3,
  "isHelpful": true,
  "helpfulCount": 1
}
```

**Response lỗi (400 Bad Request):**

```json
{
  "error": "Vui lòng cung cấp dữ liệu"
}
```

**Response lỗi (404 Not Found):**

```json
{
  "error": "Không tìm thấy đánh giá"
}
```

## Mã lỗi

- **200 OK**: Yêu cầu thành công
- **201 Created**: Tạo mới thành công
- **400 Bad Request**: Dữ liệu không hợp lệ
- **401 Unauthorized**: Không có quyền truy cập (token không hợp lệ)
- **403 Forbidden**: Không có quyền thực hiện hành động
- **404 Not Found**: Không tìm thấy tài nguyên
- **500 Internal Server Error**: Lỗi máy chủ

## Ví dụ sử dụng với cURL

### Đăng nhập

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "doraemon@example.com", "password": "doraemon456"}'
```

### Thêm đánh giá mới

```bash
curl -X POST "http://localhost:8080/api/product-reviews/15" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{"rating": 5, "content": "Sản phẩm tuyệt vời, xứng đáng 5 sao!"}'
```

### Cập nhật đánh giá

```bash
curl -X PUT "http://localhost:8080/api/reviews/3" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{"rating": 4, "content": "Sản phẩm tốt, nhưng có thể cải thiện thêm"}'
```

### Xóa đánh giá

```bash
curl -X DELETE "http://localhost:8080/api/reviews/4" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Đánh dấu đánh giá là hữu ích

```bash
curl -X POST "http://localhost:8080/api/reviews/3/helpful" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{"isHelpful": true}'
```
