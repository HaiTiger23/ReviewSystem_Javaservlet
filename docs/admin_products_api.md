# API Quản Lý Sản Phẩm Dành Cho Admin

## Thông tin chung

### Base URL
```
/api/admin/products
```

### Xác thực
- Tất cả các API đều yêu cầu JWT token trong header
- Token phải thuộc về tài khoản có role ADMIN
```
Authorization: Bearer <jwt_token>
```

### Response Format
Tất cả API đều trả về dữ liệu dạng JSON với format:
```json
// Success response
{
    "success": true,
    "data": { ... },
    "message": "Thông báo thành công"
}

// Error response
{
    "error": "Thông báo lỗi"
}
```

## 1. Lấy Danh Sách Sản Phẩm

### Endpoint
```
GET /api/admin/products
```

### Query Parameters
| Tham số | Kiểu | Mô tả | Mặc định |
|---------|------|-------|-----------|
| page | number | Số trang | 1 |
| limit | number | Số sản phẩm mỗi trang | 10 |
| category | number | ID danh mục cần lọc | (không có) |
| search | string | Tìm theo tên hoặc mô tả | (không có) |
| sort | string | Cách sắp xếp:<br>- price_asc: Giá tăng dần<br>- price_desc: Giá giảm dần<br>- rating_desc: Đánh giá cao nhất<br>- newest: Mới nhất | newest |

### Success Response (200 OK)
```json
{
    "products": [
        {
            "id": 1,
            "name": "iPhone 13",
            "slug": "iphone-13-1234567890",
            "description": "Điện thoại iPhone 13",
            "price": 20000000,
            "category_id": 1,
            "category_name": "Điện thoại",
            "user_id": 1,
            "rating": 4.5,
            "review_count": 10,
            "primary_image": "iphone13-main.jpg",
            "created_at": "2024-03-20T10:00:00Z",
            "updated_at": "2024-03-20T10:00:00Z"
        }
    ],
    "pagination": {
        "total": 50,
        "page": 1,
        "limit": 10,
        "totalPages": 5
    }
}
```

## 2. Lấy Chi Tiết Sản Phẩm

### Endpoint
```
GET /api/admin/products/{id}
```

### Parameters
| Tham số | Kiểu | Vị trí | Mô tả |
|---------|------|---------|-------|
| id | number | path | ID của sản phẩm |

### Success Response (200 OK)
```json
{
    "product": {
        "id": 1,
        "name": "iPhone 13",
        "slug": "iphone-13-1234567890",
        "description": "Điện thoại iPhone 13",
        "price": 20000000,
        "category_id": 1,
        "category_name": "Điện thoại",
        "user_id": 1,
        "rating": 4.5,
        "review_count": 10,
        "images": [
            "iphone13-1.jpg",
            "iphone13-2.jpg"
        ],
        "specifications": [
            {
                "id": 1,
                "product_id": 1,
                "name": "Màn hình",
                "value": "6.1 inch"
            }
        ],
        "created_at": "2024-03-20T10:00:00Z",
        "updated_at": "2024-03-20T10:00:00Z"
    }
}
```

## 3. Thêm Sản Phẩm Mới

### Endpoint
```
POST /api/admin/products
```

### Content-Type
- `multipart/form-data` (khi upload file)
- `application/json` (khi không có file)

### Request Body (multipart/form-data)
| Tham số | Kiểu | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| name | string | Có | Tên sản phẩm |
| category | number | Có | ID danh mục |
| price | number | Có | Giá sản phẩm (BigDecimal) |
| description | string | Không | Mô tả sản phẩm |
| images[] | file | Có | File hình ảnh (tối đa 5MB/file)<br>File đầu tiên sẽ là ảnh chính (is_primary=true) |
| specifications | string (JSON) | Không | Thông số kỹ thuật dạng JSON array |

### Request Body (application/json)
```json
{
    "name": "iPhone 13",
    "category": 1,
    "price": "20000000",
    "description": "Điện thoại iPhone 13",
    "specifications": [
        {
            "name": "Màn hình",
            "value": "6.1 inch"
        }
    ]
}
```

### Success Response (200 OK)
```json
{
    "id": 1,
    "name": "iPhone 13",
    "message": "Thêm sản phẩm thành công"
}
```

## 4. Cập Nhật Sản Phẩm

### Endpoint

```http
PUT /api/admin/products/{id}
```

### Tham số đường dẫn

| Tham số | Kiểu | Bắt buộc | Mô tả |
|---------|------|-----------|-------|
| id | number | Có | ID của sản phẩm cần cập nhật |

### Loại nội dung

- `multipart/form-data` (khi có upload file ảnh)
- `application/json` (khi không có file ảnh)

### Thân yêu cầu (multipart/form-data)

#### Thông tin sản phẩm

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|-----------|-------|
| name | string | Không | Tên sản phẩm |
| price | number | Không | Giá sản phẩm (VND) |
| description | string | Không | Mô tả chi tiết (HTML) |
| categoryId | number | Không | ID danh mục sản phẩm |
| stock | integer | Không | Số lượng tồn kho |
| images | file[] | Không | Danh sách ảnh sản phẩm (tối đa 5 ảnh) |
| thumbnail | file | Không | Ảnh đại diện sản phẩm |
| isActive | boolean | Không | Trạng thái hiển thị (true/false) |
| specifications | string | Không | Thông số kỹ thuật (JSON string) |

### Thân yêu cầu (application/json)

```json
{
  "name": "Tên sản phẩm",
  "price": 1000000,
  "description": "Mô tả sản phẩm",
  "categoryId": 1,
  "stock": 100,
  "isActive": true,
  "specifications": {
    "màn hình": "6.5 inch",
    "ram": "8GB",
    "bộ nhớ": "128GB"
  }
}
```

### Phản hồi thành công (200 OK)

```json
{
  "success": true,
  "message": "Cập nhật sản phẩm thành công",
  "data": {
    "id": 1,
    "name": "Tên sản phẩm",
    "price": 1000000,
    "categoryId": 1,
    "thumbnail": "/uploads/products/1/thumbnail.jpg",
    "images": [
      "/uploads/products/1/image1.jpg",
      "/uploads/products/1/image2.jpg"
    ]
  }
}
```

### Phản hồi lỗi

#### 400 Bad Request - Dữ liệu không hợp lệ

```json
{
  "success": false,
  "error": {
    "code": "INVALID_INPUT",
    "message": "Dữ liệu đầu vào không hợp lệ",
    "details": {
      "price": "Giá phải lớn hơn 0"
    }
  }
}
```

#### 401 Unauthorized - Chưa đăng nhập

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Vui lòng đăng nhập"
  }
}
```

#### 403 Forbidden - Không có quyền

```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "Bạn không có quyền thực hiện thao tác này"
  }
}
```

#### 404 Not Found - Không tìm thấy sản phẩm

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Không tìm thấy sản phẩm"
  }
}
```

#### 500 Internal Server Error - Lỗi máy chủ

```json
{
  "success": false,
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "Đã xảy ra lỗi, vui lòng thử lại sau"
  }
}
```

### Ghi chú

1. Khi cập nhật ảnh sản phẩm, cần gửi lại toàn bộ danh sách ảnh mới (nếu muốn giữ ảnh cũ cần gửi lại)
2. Nếu không gửi trường nào thì giá trị của trường đó sẽ được giữ nguyên
3. Dung lượng tối đa mỗi ảnh là 5MB
4. Định dạng ảnh hỗ trợ: JPG, PNG, JPEG
5. Kích thước ảnh khuyến nghị: 800x800px

## 5. Xóa Sản Phẩm

### Endpoint
```
DELETE /api/admin/products/{id}
```

### Parameters
| Tham số | Kiểu | Vị trí | Mô tả |
|---------|------|---------|-------|
| id | number | path | ID của sản phẩm |

### Success Response (200 OK)
```json
{
    "message": "Xóa sản phẩm thành công"
}
```

### Error Response
```json
{
    "error": "Không tìm thấy sản phẩm" // 404
}
```

## Mã Lỗi

| Mã | Mô tả |
|----|-------|
| 400 | Bad Request - Dữ liệu gửi lên không hợp lệ |
| 401 | Unauthorized - Chưa đăng nhập |
| 403 | Forbidden - Không có quyền truy cập |
| 404 | Not Found - Không tìm thấy sản phẩm |
| 500 | Internal Server Error - Lỗi máy chủ |

## Ví Dụ Sử Dụng

### Lấy danh sách sản phẩm có phân trang và tìm kiếm
```
GET /api/admin/products?page=1&limit=10&search=iphone&sort=price_desc
```

### Thêm sản phẩm mới (với file ảnh)
```
POST /api/admin/products
Content-Type: multipart/form-data

name: iPhone 13
category: 1
price: 20000000
description: Điện thoại iPhone 13
images[]: <file1>
images[]: <file2>
specifications: [{"name":"Màn hình","value":"6.1 inch"}]
```

### Cập nhật sản phẩm (không có file)
```
PUT /api/admin/products/1
Content-Type: application/json

{
    "name": "iPhone 13 Pro",
    "price": "25000000"
}
```

### Xóa sản phẩm
```
DELETE /api/admin/products/1
``` 