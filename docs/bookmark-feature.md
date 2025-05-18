# Product Bookmark Feature Documentation

## Overview
The bookmark feature allows authenticated users to save products for later viewing. Users can bookmark/unbookmark products and view their bookmarked products list with sorting and pagination options.

## Database Schema

### Bookmarks Table
```sql
CREATE TABLE bookmarks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_bookmark (user_id, product_id)
);
```

## API Endpoints

### 1. Toggle Bookmark
Adds or removes a bookmark for a product.

**Endpoint:** `POST /api/products/bookmark/{productId}`

**Authentication:** Required (JWT Token)

**Response:**
```json
{
    "message": "Thêm bookmark thành công" | "Xoá bookmark thành công",
    "status": 1 | 0  // 1: added, 0: removed
}
```

**Error Responses:**
- `401 Unauthorized`: User not authenticated
- `404 Not Found`: Product not found
- `400 Bad Request`: Invalid request

### 2. Get Bookmarked Products
Retrieves a paginated list of user's bookmarked products.

**Endpoint:** `GET /api/products/bookmark-user`

**Authentication:** Required (JWT Token)

**Query Parameters:**
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 10)
- `sort`: Sorting option
  - `price_asc`: Sort by price ascending
  - `price_desc`: Sort by price descending
  - `name_asc`: Sort by name ascending
  - `name_desc`: Sort by name descending
  - `bookmark_date_desc`: Sort by bookmark date (default)

**Response:**
```json
{
    "products": [
        {
            "id": number,
            "name": string,
            "description": string,
            "price": number,
            "image": string,
            "category_id": number,
            "created_at": timestamp,
            "updated_at": timestamp,
            "bookmark_date": timestamp
        }
    ],
    "pagination": {
        "currentPage": number,
        "totalPages": number,
        "totalItems": number,
        "itemsPerPage": number
    }
}
```

**Error Responses:**
- `401 Unauthorized`: User not authenticated
- `500 Internal Server Error`: Server error

## Implementation Details

### Components

1. **ProductController**
   - Handles bookmark-related business logic
   - Manages user authentication verification
   - Processes sorting and pagination parameters

2. **ProductDAO**
   - Manages database operations for bookmarks
   - Implements efficient queries with proper joins
   - Handles transaction management

3. **BookmarkDAO**
   - Handles bookmark-specific database operations
   - Manages toggle functionality
   - Ensures data consistency

### Security Considerations

1. **Authentication**
   - All bookmark endpoints require valid JWT token
   - User ID is extracted from token for security
   - Token validation on each request

2. **Data Validation**
   - Product existence verification
   - User authorization checks
   - Input parameter validation

### Performance Optimizations

1. **Database**
   - Indexed foreign keys for faster joins
   - Optimized queries for pagination
   - Proper use of database transactions

2. **Caching**
   - Bookmark status cached in product responses
   - Pagination results optimized for performance

## Usage Examples

### Adding a Bookmark
```javascript
const response = await fetch('/api/products/bookmark/123', {
    method: 'POST',
    headers: {
        'Authorization': 'Bearer YOUR_JWT_TOKEN'
    }
});
const result = await response.json();
```

### Getting Bookmarked Products
```javascript
const response = await fetch('/api/products/bookmark-user?page=1&limit=10&sort=price_desc', {
    headers: {
        'Authorization': 'Bearer YOUR_JWT_TOKEN'
    }
});
const result = await response.json();
```

## Error Handling

The API implements consistent error handling:

1. **Authentication Errors**
   - Invalid or missing token
   - Expired token
   - Insufficient permissions

2. **Business Logic Errors**
   - Product not found
   - Invalid pagination parameters
   - Invalid sort parameters

3. **System Errors**
   - Database connection issues
   - Server errors
   - Network problems

## Testing

### Unit Tests
- Bookmark toggle functionality
- Pagination logic
- Sort parameter handling
- Authentication verification

### Integration Tests
- End-to-end bookmark flow
- API response format
- Error handling scenarios
- Performance under load

## Future Enhancements

1. **Planned Features**
   - Bookmark collections/folders
   - Bookmark sharing
   - Bookmark notifications
   - Bulk bookmark operations

2. **Performance Improvements**
   - Redis caching implementation
   - Query optimization
   - Response compression

3. **User Experience**
   - Real-time bookmark updates
   - Improved sorting options
   - Advanced filtering capabilities 