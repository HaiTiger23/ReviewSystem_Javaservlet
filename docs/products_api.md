# API Sản phẩm (Products API)

API sản phẩm cung cấp các endpoint để quản lý sản phẩm trong hệ thống Review System.

## Endpoint cơ sở

```
/api/products
```

## Lấy danh sách sản phẩm

### Endpoint

```
GET /api/products
```

### Query Parameters

| Tham số | Mô tả | Mặc định |
|---------|-------|----------|
| page | Số trang | 1 |
| limit | Số sản phẩm mỗi trang | 10 |
| category_id | Lọc theo danh mục | (không có) |
| search | Tìm kiếm theo tên | (không có) |
| sort | Sắp xếp (price_asc, price_desc, newest, rating) | newest |

### Response thành công (200 OK)

```json
{
  "products": [
    {
      "id": 1,
      "name": "iPhone 13",
      "slug": "iphone-13",
      "description": "Điện thoại iPhone 13",
      "price": 20000000,
      "category_id": 1,
      "user_id": 1,
      "rating": 4.5,
      "review_count": 10,
      "images": [
        "iphone13-1.jpg",
        "iphone13-2.jpg"
      ],
      "specifications": [
        {
          "name": "Màn hình",
          "value": "6.1 inch"
        },
        {
          "name": "RAM",
          "value": "4GB"
        }
      ],
      "created_at": "2025-05-01T10:30:00Z",
      "updated_at": "2025-05-02T15:45:00Z"
    }
  ],
  "pagination": {
    "total": 50,
    "per_page": 10,
    "current_page": 1,
    "last_page": 5
  }
}
```

## Lấy chi tiết sản phẩm

### Endpoint

```
GET /api/products/{id}
```

### Response thành công (200 OK)

```json
{
  "product": {
    "id": 1,
    "name": "iPhone 13",
    "slug": "iphone-13",
    "description": "Điện thoại iPhone 13",
    "price": 20000000,
    "category_id": 1,
    "user_id": 1,
    "rating": 4.5,
    "review_count": 10,
    "images": [
      "iphone13-1.jpg",
      "iphone13-2.jpg"
    ],
    "specifications": [
      {
        "name": "Màn hình",
        "value": "6.1 inch"
      },
      {
        "name": "RAM",
        "value": "4GB"
      }
    ],
    "created_at": "2025-05-01T10:30:00Z",
    "updated_at": "2025-05-02T15:45:00Z"
  }
}
```

### Response lỗi

- **404 Not Found**: Sản phẩm không tồn tại
  ```json
  {
    "error": "Không tìm thấy sản phẩm"
  }
  ```

## Thêm sản phẩm mới

### Endpoint

```
POST /api/products
```

### Headers

```
Authorization: Bearer jwt_token
Content-Type: multipart/form-data
```

### Form Data

| Tham số | Mô tả | Bắt buộc |
|---------|-------|----------|
| name | Tên sản phẩm | Có |
| description | Mô tả sản phẩm | Không |
| price | Giá sản phẩm | Có |
| category_id | ID danh mục | Có |
| images[] | Hình ảnh sản phẩm (nhiều file) | Không |
| spec_name[] | Tên thông số kỹ thuật (nhiều) | Không |
| spec_value[] | Giá trị thông số kỹ thuật (nhiều) | Không |

### Response thành công (201 Created)

```json
{
  "success": true,
  "message": "Thêm sản phẩm thành công",
  "product": {
    "id": 1,
    "name": "iPhone 13",
    "slug": "iphone-13",
    "description": "Điện thoại iPhone 13",
    "price": 20000000,
    "category_id": 1,
    "user_id": 1
  }
}
```

### Response lỗi

- **400 Bad Request**: Dữ liệu không hợp lệ
  ```json
  {
    "error": "Vui lòng điền đầy đủ thông tin"
  }
  ```

- **400 Bad Request**: Danh mục không tồn tại
  ```json
  {
    "error": "Danh mục không tồn tại (ID: 1)"
  }
  ```

- **401 Unauthorized**: Không có token xác thực
  ```json
  {
    "error": "Không có token xác thực"
  }
  ```

## Thêm sản phẩm mới (JSON)

### Endpoint

```
POST /api/products
```

### Headers

```
Authorization: Bearer jwt_token
Content-Type: application/json
```

### Request Body

```json
{
  "name": "iPhone 13",
  "description": "Điện thoại iPhone 13",
  "price": 20000000,
  "category_id": 1,
  "specifications": [
    {
      "name": "Màn hình",
      "value": "6.1 inch"
    },
    {
      "name": "RAM",
      "value": "4GB"
    }
  ]
}
```

### Response thành công (201 Created)

```json
{
  "success": true,
  "message": "Thêm sản phẩm thành công",
  "product": {
    "id": 1,
    "name": "iPhone 13",
    "slug": "iphone-13",
    "description": "Điện thoại iPhone 13",
    "price": 20000000,
    "category_id": 1,
    "user_id": 1
  }
}
```

## Cập nhật sản phẩm

### Endpoint

```
PUT /api/products/{id}
```

### Headers

```
Authorization: Bearer jwt_token
Content-Type: multipart/form-data
```

### Form Data

| Tham số | Mô tả | Bắt buộc |
|---------|-------|----------|
| name | Tên sản phẩm | Có |
| description | Mô tả sản phẩm | Không |
| price | Giá sản phẩm | Có |
| category_id | ID danh mục | Có |
| images[] | Hình ảnh sản phẩm mới (nhiều file) | Không |
| remove_images[] | ID hình ảnh cần xóa | Không |
| spec_name[] | Tên thông số kỹ thuật (nhiều) | Không |
| spec_value[] | Giá trị thông số kỹ thuật (nhiều) | Không |

### Response thành công (200 OK)

```json
{
  "success": true,
  "message": "Cập nhật sản phẩm thành công",
  "product": {
    "id": 1,
    "name": "iPhone 13 Pro",
    "slug": "iphone-13-pro",
    "description": "Điện thoại iPhone 13 Pro",
    "price": 25000000,
    "category_id": 1,
    "user_id": 1
  }
}
```

### Response lỗi

- **400 Bad Request**: Dữ liệu không hợp lệ
  ```json
  {
    "error": "Vui lòng điền đầy đủ thông tin"
  }
  ```

- **401 Unauthorized**: Không có quyền
  ```json
  {
    "error": "Bạn không có quyền cập nhật sản phẩm này"
  }
  ```

- **404 Not Found**: Sản phẩm không tồn tại
  ```json
  {
    "error": "Không tìm thấy sản phẩm"
  }
  ```

## Xóa sản phẩm

### Endpoint

```
DELETE /api/products/{id}
```

### Headers

```
Authorization: Bearer jwt_token
```

### Response thành công (200 OK)

```json
{
  "success": true,
  "message": "Xóa sản phẩm thành công"
}
```

### Response lỗi

- **401 Unauthorized**: Không có quyền
  ```json
  {
    "error": "Bạn không có quyền xóa sản phẩm này"
  }
  ```

- **404 Not Found**: Sản phẩm không tồn tại
  ```json
  {
    "error": "Không tìm thấy sản phẩm"
  }
  ```
