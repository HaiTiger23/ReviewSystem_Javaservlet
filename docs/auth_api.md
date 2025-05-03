# API Xác thực (Authentication API)

API xác thực cung cấp các endpoint để đăng ký, đăng nhập, và quản lý tài khoản người dùng.

## Endpoint cơ sở

```
/api/auth
```

## Đăng ký tài khoản

### Endpoint

```
POST /api/auth/register
```

### Request Body

```json
{
  "name": "Tên người dùng",
  "email": "email@example.com",
  "password": "mật khẩu",
  "confirmPassword": "xác nhận mật khẩu"
}
```

### Response thành công (200 OK)

```json
{
  "user": {
    "id": 1,
    "name": "Tên người dùng",
    "email": "email@example.com"
  },
  "token": "jwt_token"
}
```

### Response lỗi

- **400 Bad Request**: Dữ liệu không hợp lệ
  ```json
  {
    "error": "Vui lòng điền đầy đủ thông tin"
  }
  ```

- **400 Bad Request**: Mật khẩu không khớp
  ```json
  {
    "error": "Mật khẩu không khớp nhau"
  }
  ```

- **409 Conflict**: Email đã tồn tại
  ```json
  {
    "error": "Email đã tồn tại"
  }
  ```

## Đăng nhập

### Endpoint

```
POST /api/auth/login
```

### Request Body

```json
{
  "email": "email@example.com",
  "password": "mật khẩu"
}
```

### Response thành công (200 OK)

```json
{
  "user": {
    "id": 1,
    "name": "Tên người dùng",
    "email": "email@example.com"
  },
  "token": "jwt_token"
}
```

### Response lỗi

- **400 Bad Request**: Dữ liệu không hợp lệ
  ```json
  {
    "error": "Vui lòng cung cấp email và mật khẩu"
  }
  ```

- **401 Unauthorized**: Đăng nhập thất bại
  ```json
  {
    "error": "Email hoặc mật khẩu không đúng"
  }
  ```

## Đăng nhập bằng Google/Email

### Endpoint

```
POST /api/auth/login-provider
```

### Request Body

```json
{
  "email": "email@example.com",
  "name": "Tên người dùng",
  "avatar": "url_avatar",
  "provider": "google"
}
```

### Response thành công (200 OK)

```json
{
  "user": {
    "id": 1,
    "name": "Tên người dùng",
    "email": "email@example.com"
  },
  "token": "jwt_token"
}
```

## Lấy thông tin người dùng hiện tại

### Endpoint

```
GET /api/auth/me
```

### Headers

```
Authorization: Bearer jwt_token
```

### Response thành công (200 OK)

```json
{
  "user": {
    "id": 1,
    "name": "Tên người dùng",
    "email": "email@example.com",
    "role": "USER"
  }
}
```

### Response lỗi

- **401 Unauthorized**: Token không hợp lệ hoặc hết hạn
  ```json
  {
    "error": "Không có quyền truy cập"
  }
  ```

## Yêu cầu đặt lại mật khẩu

### Endpoint

```
POST /api/auth/forgot-password
```

### Request Body

```json
{
  "email": "email@example.com"
}
```

### Response thành công (200 OK)

```json
{
  "message": "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn"
}
```

### Response lỗi

- **400 Bad Request**: Email không tồn tại
  ```json
  {
    "error": "Email không tồn tại trong hệ thống"
  }
  ```

## Đặt lại mật khẩu

### Endpoint

```
POST /api/auth/reset-password
```

### Request Body

```json
{
  "token": "reset_token",
  "password": "mật khẩu mới",
  "confirmPassword": "xác nhận mật khẩu mới"
}
```

### Response thành công (200 OK)

```json
{
  "message": "Mật khẩu đã được đặt lại thành công"
}
```

### Response lỗi

- **400 Bad Request**: Token không hợp lệ hoặc hết hạn
  ```json
  {
    "error": "Token không hợp lệ hoặc đã hết hạn"
  }
  ```

- **400 Bad Request**: Mật khẩu không khớp
  ```json
  {
    "error": "Mật khẩu không khớp nhau"
  }
  ```

## Đăng xuất

### Endpoint

```
POST /api/auth/logout
```

### Headers

```
Authorization: Bearer jwt_token
```

### Response thành công (200 OK)

```json
{
  "message": "Đã đăng xuất thành công"
}
```

## Đổi mật khẩu

### Endpoint

```
POST /api/auth/change-password
```

### Headers

```
Authorization: Bearer jwt_token
```

### Request Body

```json
{
  "currentPassword": "mật khẩu hiện tại",
  "newPassword": "mật khẩu mới",
  "confirmPassword": "xác nhận mật khẩu mới"
}
```

### Response thành công (200 OK)

```json
{
  "message": "Đổi mật khẩu thành công"
}
```

### Response lỗi

- **400 Bad Request**: Mật khẩu hiện tại không đúng
  ```json
  {
    "error": "Mật khẩu hiện tại không đúng"
  }
  ```

- **400 Bad Request**: Mật khẩu mới không khớp
  ```json
  {
    "error": "Mật khẩu mới không khớp nhau"
  }
  ```
