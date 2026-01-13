package org.example.hung_hypebeast_backend.util;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.entity.Category;
import org.example.hung_hypebeast_backend.entity.Product;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.example.hung_hypebeast_backend.repository.CategoryRepository;
import org.example.hung_hypebeast_backend.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // <--- 1. Inject thêm Repo này

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Kiểm tra nếu DB đã có dữ liệu thì không thêm nữa
        if (productRepository.count() > 0) {
            return;
        }

        System.out.println(">>> Bắt đầu khởi tạo dữ liệu mẫu (Seeding)...");

        // --- BƯỚC 1: TẠO CATEGORY TRƯỚC ---
        Category catAo = Category.builder().name("Áo Thời Trang").build();
        Category catQuan = Category.builder().name("Quần Thời Trang").build();

        // Lưu Category xuống DB để nó sinh ra ID
        categoryRepository.saveAll(List.of(catAo, catQuan));


        // --- BƯỚC 2: TẠO SẢN PHẨM & GÁN CATEGORY ---

        // 1. Áo Thun (Thuộc catAo)
        Product p1 = Product.builder()
                .name("Áo Thun Rồng Hypebeast")
                .description("Chất liệu Cotton 100%, hình in Rồng phản quang.")
                .basePrice(new BigDecimal("350000"))
                .category(catAo) // <--- Gán Category Áo
                .build();
        p1.setSkus(generateSkus(p1, "TSHIRT-RONG", new String[]{"M", "L"}, new String[]{"Black", "White"}));


        // 2. Hoodie (Thuộc catAo)
        Product p2 = Product.builder()
                .name("Hoodie Basic Zip")
                .description("Hoodie form rộng, nỉ bông dày dặn.")
                .basePrice(new BigDecimal("550000"))
                .category(catAo) // <--- Gán Category Áo
                .build();
        p2.setSkus(generateSkus(p2, "HOODIE-ZIP", new String[]{"L", "XL"}, new String[]{"Grey", "Navy"}));


        // 3. Quần Short (Thuộc catQuan)
        Product p3 = Product.builder()
                .name("Quần Short Kaki Túi Hộp")
                .description("Quần short năng động, nhiều túi tiện lợi.")
                .basePrice(new BigDecimal("280000"))
                .category(catQuan) // <--- Gán Category Quần
                .build();
        p3.setSkus(generateSkus(p3, "SHORT-KAKI", new String[]{"29", "30"}, new String[]{"Beige", "Black"}));

        // --- BƯỚC 3: LƯU TẤT CẢ ---
        productRepository.saveAll(List.of(p1, p2, p3));

        System.out.println(">>> Đã khởi tạo xong: 2 Categories - 3 Sản phẩm - 12 SKUs!");
    }

    // Hàm phụ trợ giữ nguyên
    private List<ProductSku> generateSkus(Product product, String codePrefix, String[] sizes, String[] colors) {
        List<ProductSku> skus = new ArrayList<>();

        for (String size : sizes) {
            for (String color : colors) {
                String skuCode = String.format("%s-%s-%s", codePrefix, color.toUpperCase(), size).replace(" ", "");

                // Logic giá: Ví dụ Size XL hoặc Size 30 sẽ đắt hơn 10.000đ
                BigDecimal skuPrice = product.getBasePrice();
                if (size.equals("XL") || size.equals("30")) {
                    skuPrice = skuPrice.add(new BigDecimal("10000"));
                }

                ProductSku sku = ProductSku.builder()
                        .skuCode(skuCode)
                        .size(size)
                        .color(color)
                        .quantity(10)
                        .price(skuPrice)
                        .product(product)
                        .build();

                skus.add(sku);
            }
        }
        return skus;
    }
}
