# API Danh mục (Categories API)

API danh mục cung cấp các endpoint để quản lý danh mục sản phẩm trong hệ thống Review System. Các thao tác thêm, sửa, xóa danh mục chỉ dành cho người dùng có quyền admin.

## Endpoint cơ sở

```
/api/categories
```

## Lấy danh sách danh mục

### Endpoint

```
GET /api/categories
```

### Response thành công (200 OK)

```json
{
  "categories": [
    {
      "id": 1,
      "name": "Điện thoại di động",
      "slug": "dien-thoai-di-dong",
      "parent_id": null,
      "created_at": "2025-05-03T23:26:46Z",
      "updated_at": "2025-05-03T23:27:32Z"
    }
  ]
}
```

## Lấy chi tiết danh mục

### Endpoint

```
GET /api/categories/{id}
```

### Response thành công (200 OK)

```json
{
  "category": {
    "id": 1,
    "name": "Điện thoại di động",
    "slug": "dien-thoai-di-dong",
    "parent_id": null,
    "created_at": "2025-05-03T23:26:46Z",
    "updated_at": "2025-05-03T23:27:32Z"
  }
}
```

### Response lỗi

- **404 Not Found**: Danh mục không tồn tại
  ```json
  {
    "error": "Không tìm thấy danh mục"
  }
  ```

## Thêm danh mục mới (Chỉ Admin)

### Endpoint

```
POST /api/categories
```

### Headers

```
Authorization: Bearer jwt_token
Content-Type: application/json
```

### Request Body

```json
{
  "name": "Điện thoại",
  "parent_id": null
}
```

### Response thành công (201 Created)

```json
{
  "success": true,
  "message": "Thêm danh mục thành công",
  "category": {
    "id": 1,
    "name": "Điện thoại",
    "slug": "dien-thoai",
    "parent_id": null
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

- **400 Bad Request**: Danh mục cha không tồn tại
  ```json
  {
    "error": "Danh mục cha không tồn tại"
  }
  ```

- **401 Unauthorized**: Không có token xác thực
  ```json
  {
    "error": "Không có token xác thực"
  }
  ```

- **403 Forbidden**: Không có quyền admin
  ```json
  {
    "error": "Bạn không có quyền thực hiện hành động này"
  }
  ```

## Cập nhật danh mục (Chỉ Admin)

### Endpoint

```
PUT /api/categories/{id}
```

### Headers

```
Authorization: Bearer jwt_token
Content-Type: application/json
```

### Request Body

```json
{
  "name": "Điện thoại di động",
  "parent_id": null
}
```

### Response thành công (200 OK)

```json
{
  "success": true,
  "message": "Cập nhật danh mục thành công",
  "category": {
    "id": 1,
    "name": "Điện thoại di động",
    "slug": "dien-thoai-di-dong",
    "parent_id": null
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

- **400 Bad Request**: Không thể chọn chính nó làm danh mục cha
  ```json
  {
    "error": "Không thể chọn chính danh mục này làm danh mục cha"
  }
  ```

- **401 Unauthorized**: Không có token xác thực
  ```json
  {
    "error": "Không có token xác thực"
  }
  ```

- **403 Forbidden**: Không có quyền admin
  ```json
  {
    "error": "Bạn không có quyền thực hiện hành động này"
  }
  ```

- **404 Not Found**: Danh mục không tồn tại
  ```json
  {
    "error": "Không tìm thấy danh mục"
  }
  ```

## Xóa danh mục (Chỉ Admin)

### Endpoint

```
DELETE /api/categories/{id}
```

### Headers

```
Authorization: Bearer jwt_token
```

### Response thành công (200 OK)

```json
{
  "success": true,
  "message": "Xóa danh mục thành công"
}
```

### Response lỗi

- **400 Bad Request**: Danh mục có sản phẩm
  ```json
  {
    "error": "Không thể xóa danh mục đã có sản phẩm"
  }
  ```

- **401 Unauthorized**: Không có token xác thực
  ```json
  {
    "error": "Không có token xác thực"
  }
  ```

- **403 Forbidden**: Không có quyền admin
  ```json
  {
    "error": "Bạn không có quyền thực hiện hành động này"
  }
  ```

- **404 Not Found**: Danh mục không tồn tại
  ```json
  {
    "error": "Không tìm thấy danh mục"
  }
  ```
