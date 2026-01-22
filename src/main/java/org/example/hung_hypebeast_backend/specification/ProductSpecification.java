package org.example.hung_hypebeast_backend.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.hung_hypebeast_backend.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    /**
     * Tạo Specification để filter sản phẩm dựa trên các tiêu chí
     * @param categoryId - ID của category (có thể null)
     * @param minPrice - Giá tối thiểu (có thể null)
     * @param maxPrice - Giá tối đa (có thể null)
     * @param keyword - Từ khóa tìm kiếm trong tên sản phẩm (có thể null)
     * @return Specification<Product>
     */
    public static Specification<Product> filterProducts(Long categoryId, BigDecimal minPrice,
                                                         BigDecimal maxPrice, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter theo category
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Filter theo giá tối thiểu
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }

            // Filter theo giá tối đa
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
            }

            // Filter theo từ khóa trong tên sản phẩm
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + keyword.toLowerCase() + "%"
                ));
            }

            // Kết hợp tất cả predicates với AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification để filter theo category
     */
    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) ->
            categoryId == null ? null : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Specification để filter theo giá tối thiểu
     */
    public static Specification<Product> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
            minPrice == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
    }

    /**
     * Specification để filter theo giá tối đa
     */
    public static Specification<Product> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
            maxPrice == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
    }

    /**
     * Specification để tìm kiếm theo từ khóa trong tên
     */
    public static Specification<Product> hasKeywordInName(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + keyword.toLowerCase() + "%"
            );
        };
    }
}

