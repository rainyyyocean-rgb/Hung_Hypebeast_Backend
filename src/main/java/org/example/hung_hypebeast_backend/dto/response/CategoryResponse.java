package org.example.hung_hypebeast_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    // Không cần list products ở đây, vì menu chỉ cần hiển thị tên
}
