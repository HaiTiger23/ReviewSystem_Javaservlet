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
```
PUT /api/admin/products/{id}
```

### Parameters
| Tham số | Kiểu | Vị trí | Mô tả |
|---------|------|---------|-------|
| id | number | path | ID của sản phẩm |

### Content-Type
- `multipart/form-data` (khi upload file)
- `application/json` (khi không có file)

### Request Body
Tương tự như khi thêm sản phẩm mới. Các trường không gửi lên sẽ giữ nguyên giá trị cũ.

### Success Response (200 OK)
```json
{
    "message": "Cập nhật sản phẩm thành công"
}
```

### Error Response
```json
{
    "error": "Không tìm thấy sản phẩm" // 404
}
```

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