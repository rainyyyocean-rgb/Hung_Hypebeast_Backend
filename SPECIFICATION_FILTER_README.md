# API Filter Sản Phẩm Sử Dụng Specification

## Tổng quan
Đã tạo thêm API mới để lấy danh sách sản phẩm sử dụng **JPA Specification** thay vì `@Query` với các điều kiện `IS NULL`. 

## Lợi ích của Specification Pattern
1. **Dynamic Query Building**: Xây dựng query động dựa trên các tham số không null
2. **Type-Safe**: An toàn về kiểu dữ liệu, IDE có thể gợi ý và kiểm tra lỗi
3. **Reusable**: Có thể tái sử dụng và kết hợp các Specification
4. **Maintainable**: Dễ bảo trì và mở rộng khi cần thêm filter mới
5. **Cleaner Code**: Code rõ ràng hơn, tách biệt logic filter khỏi Repository

## Endpoint Mới

### GET /api/v1/products/filter

**Mô tả**: Lấy danh sách sản phẩm với filter động sử dụng JPA Specification

**Parameters**:
- `categoryId` (optional, Long): ID của category cần filter
- `minPrice` (optional, BigDecimal): Giá tối thiểu
- `maxPrice` (optional, BigDecimal): Giá tối đa
- `keyword` (optional, String): Từ khóa tìm kiếm trong tên sản phẩm
- `page` (default: 0, Integer): Số trang (bắt đầu từ 0)
- `size` (default: 10, Integer): Số lượng sản phẩm mỗi trang
- `sort` (default: "id", String): Trường để sắp xếp

**Response**: `Page<ProductResponse>`

## Ví dụ sử dụng

### 1. Lấy tất cả sản phẩm (trang đầu tiên, 10 items)
```
GET /api/v1/products/filter
```

### 2. Filter theo category
```
GET /api/v1/products/filter?categoryId=1
```

### 3. Filter theo khoảng giá
```
GET /api/v1/products/filter?minPrice=100000&maxPrice=500000
```

### 4. Filter theo từ khóa
```
GET /api/v1/products/filter?keyword=nike
```

### 5. Kết hợp nhiều filter
```
GET /api/v1/products/filter?categoryId=1&minPrice=200000&maxPrice=1000000&keyword=jordan
```

### 6. Với phân trang và sắp xếp
```
GET /api/v1/products/filter?categoryId=1&page=0&size=20&sort=basePrice
```

### 7. Sắp xếp giảm dần theo giá
```
GET /api/v1/products/filter?sort=basePrice,desc
```

## Cấu trúc Code

### 1. ProductSpecification.java
File chứa các Specification để filter sản phẩm:
- `filterProducts()`: Specification tổng hợp tất cả các filter
- `hasCategory()`: Filter theo category
- `hasPriceGreaterThanOrEqualTo()`: Filter theo giá tối thiểu
- `hasPriceLessThanOrEqualTo()`: Filter theo giá tối đa
- `hasKeywordInName()`: Tìm kiếm theo từ khóa trong tên

### 2. ProductRepository.java
Đã được extends thêm `JpaSpecificationExecutor<Product>` để hỗ trợ Specification queries.

### 3. ProductService.java & ProductServiceImpl.java
Thêm method mới:
```java
Page<ProductResponse> getProductsWithSpecification(
    Long categoryId, 
    BigDecimal minPrice, 
    BigDecimal maxPrice, 
    String keyword, 
    Pageable pageable
);
```

### 4. ProductController.java
Thêm endpoint mới `/filter` sử dụng method trên.

## So sánh với API cũ

### API cũ (GET /api/v1/products)
- Sử dụng `@Query` với điều kiện `IS NULL`
- Không hỗ trợ tìm kiếm theo từ khóa
- Query cố định trong Repository

```java
@Query("SELECT p FROM Product p WHERE " +
    "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
    "(:minPrice IS NULL OR p.basePrice >= :minPrice) AND " +
    "(:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
```

### API mới (GET /api/v1/products/filter)
- Sử dụng **JPA Specification**
- Hỗ trợ thêm tìm kiếm theo từ khóa
- Query được xây dựng động
- Dễ mở rộng và bảo trì

```java
Specification<Product> spec = ProductSpecification.filterProducts(
    categoryId, minPrice, maxPrice, keyword
);
Page<Product> productPage = productRepository.findAll(spec, pageable);
```

## Mở rộng trong tương lai

Để thêm filter mới (ví dụ: filter theo brand), chỉ cần:

1. Thêm method trong `ProductSpecification`:
```java
public static Specification<Product> hasBrand(String brand) {
    return (root, query, criteriaBuilder) -> 
        brand == null ? null : criteriaBuilder.equal(root.get("brand"), brand);
}
```

2. Update method `filterProducts()` để thêm predicate mới

3. Thêm parameter vào Controller và Service

## Testing

Có thể test bằng Swagger UI tại: `http://localhost:8080/swagger-ui.html`

Hoặc sử dụng Postman/cURL:
```bash
curl "http://localhost:8080/api/v1/products/filter?categoryId=1&keyword=nike&page=0&size=10"
```

## Kết luận

API mới sử dụng Specification Pattern cung cấp:
- ✅ Code sạch hơn và dễ maintain
- ✅ Type-safe query building
- ✅ Flexible và dễ mở rộng
- ✅ Tách biệt business logic khỏi data access layer
- ✅ Hỗ trợ tìm kiếm theo từ khóa

