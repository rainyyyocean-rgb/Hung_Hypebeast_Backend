package org.example.hung_hypebeast_backend.repository;

import org.example.hung_hypebeast_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
