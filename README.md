# Hệ thống Xác thực Người dùng (User Authentication System)

Hệ thống API xác thực người dùng được phát triển bằng Java Servlet, kết nối với MySQL.

## Tính năng

### Xác thực người dùng
- **Đăng ký**: Tạo tài khoản mới
- **Đăng nhập**: Truy cập hệ thống
- **Đăng nhập bằng Email/Google**: Tùy chọn đăng nhập tiện lợi
- **Quên mật khẩu**: Khôi phục mật khẩu khi bị mất
- **Đăng xuất**: Rời khỏi hệ thống

## Cấu trúc dự án

```
ReviewSystem/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── api/
│       │               ├── controller/
│       │               │   ├── AuthController.java
│       │               │   └── AuthServlet.java
│       │               ├── dao/
│       │               │   └── UserDAO.java
│       │               ├── model/
│       │               │   └── User.java
│       │               ├── service/
│       │               │   └── AuthService.java
│       │               ├── util/
│       │               │   ├── DatabaseUtil.java
│       │               │   └── PasswordUtil.java
│       │               └── filter/
│       │                   └── CORSFilter.java
│       ├── resources/
│       │   └── sql/
│       │       └── create_tables.sql
│       └── webapp/
│           └── WEB-INF/
│               └── web.xml
└── pom.xml
```

## Cài đặt và Chạy

### Yêu cầu
- Java 11 hoặc cao hơn
- Maven
- MySQL

### Các bước cài đặt

1. **Clone dự án**

2. **Cấu hình cơ sở dữ liệu**
   - Tạo cơ sở dữ liệu MySQL
   - Chạy script SQL trong `src/main/resources/sql/create_tables.sql`
   - Cập nhật thông tin kết nối trong `DatabaseUtil.java` nếu cần

3. **Biên dịch và đóng gói**
   ```
   mvn clean package
   ```

4. **Triển khai file WAR** vào Tomcat hoặc servlet container khác

## API Endpoints

### Đăng ký
- **URL**: `/api/auth/register`
- **Method**: POST
- **Params**: `name`, `email`, `password`

### Đăng nhập
- **URL**: `/api/auth/login`
- **Method**: POST
- **Params**: `email`, `password`

### Đăng nhập với Provider
- **URL**: `/api/auth/login-provider`
- **Method**: POST
- **Params**: `email`, `name`, `avatar`, `provider`

### Quên mật khẩu
- **URL**: `/api/auth/forgot-password`
- **Method**: POST
- **Params**: `email`

### Đặt lại mật khẩu
- **URL**: `/api/auth/reset-password`
- **Method**: POST
- **Params**: `token`, `newPassword`

### Đăng xuất
- **URL**: `/api/auth/logout`
- **Method**: POST hoặc GET
