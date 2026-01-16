package org.example.hung_hypebeast_backend.repository;

import jakarta.persistence.LockModeType;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    // 🔥 VŨ KHÍ BÍ MẬT: PESSIMISTIC LOCK
    // Khi gọi hàm này, Database sẽ KHÓA dòng SKU này lại.
    // Các transaction khác muốn đọc/sửa dòng này phải CHỜ transaction này xong.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProductSku s WHERE s.id = :id")
    Optional<ProductSku> findByIdWithLock(@Param("id") Long id);
}
