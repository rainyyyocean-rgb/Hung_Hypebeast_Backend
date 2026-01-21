# Hung Hypebeast Backend

Backend API cho hệ thống thương mại điện tử Hypebeast, được xây dựng với Spring Boot và PostgreSQL.

## 📋 Mục Lục

- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cài Đặt Môi Trường](#cài-đặt-môi-trường)
- [Cấu Hình Database](#cấu-hình-database)
- [Cấu Hình Ứng Dụng](#cấu-hình-ứng-dụng)
- [Chạy Ứng Dụng](#chạy-ứng-dụng)
- [Migration và Seed Dữ Liệu](#migration-và-seed-dữ-liệu)
- [API Documentation](#api-documentation)
- [Tài Khoản Mặc Định](#tài-khoản-mặc-định)

---

## 🔧 Yêu Cầu Hệ Thống

Trước khi bắt đầu, đảm bảo máy tính của bạn đã cài đặt:

- **Java Development Kit (JDK)**: Version 17 hoặc cao hơn
- **Apache Maven**: Version 3.6+ (hoặc sử dụng Maven Wrapper đi kèm)
- **Docker & Docker Compose**: Để chạy PostgreSQL database
- **Git**: Để clone repository

### Kiểm Tra Phiên Bản

```bash
# Kiểm tra Java version
java -version

# Kiểm tra Maven version
mvn -version

# Kiểm tra Docker version
docker --version
docker-compose --version
```

---

## 🚀 Cài Đặt Môi Trường

### Bước 1: Clone Repository

```bash
git clone <repository-url>
cd Hung_Hypebeast_Backend
```

### Bước 2: Cài Đặt Java JDK 17

#### Windows:
1. Tải Oracle JDK 17 từ [Oracle Website](https://www.oracle.com/java/technologies/downloads/#java17)
2. Cài đặt và thiết lập biến môi trường `JAVA_HOME`
3. Thêm `%JAVA_HOME%\bin` vào PATH

#### macOS:
```bash
# Sử dụng Homebrew
brew install openjdk@17

# Thiết lập JAVA_HOME
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc
```

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Bước 3: Cài Đặt Maven (Tùy Chọn)

Dự án đã bao gồm Maven Wrapper (`mvnw`), bạn có thể sử dụng trực tiếp mà không cần cài Maven.

Nếu muốn cài Maven global:

#### Windows:
1. Tải Maven từ [Apache Maven](https://maven.apache.org/download.cgi)
2. Giải nén và thêm `bin` folder vào PATH

#### macOS:
```bash
brew install maven
```

#### Linux:
```bash
sudo apt install maven
```

### Bước 4: Cài Đặt Docker

#### Windows & macOS:
- Tải và cài đặt [Docker Desktop](https://www.docker.com/products/docker-desktop)

#### Linux:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install docker.io docker-compose
sudo systemctl start docker
sudo systemctl enable docker
```

---

## 🗄️ Cấu Hình Database

### Bước 1: Tạo File `.env`

File `.env` đã có sẵn trong project với cấu hình mặc định. Bạn có thể chỉnh sửa nếu cần:

```env
# Database Configuration
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password123
POSTGRES_DB=hypebeast_db
POSTGRES_PORT=5432

# App Configuration
SERVER_PORT=8080

# Email Configuration (Gmail example)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

⚠️ **Lưu ý về Email Configuration:**
- Để gửi email, bạn cần cấu hình Gmail App Password
- Truy cập [Google App Passwords](https://myaccount.google.com/apppasswords)
- Tạo App Password và thay thế vào `MAIL_PASSWORD`

### Bước 2: Khởi Động PostgreSQL với Docker

```bash
# Khởi động PostgreSQL container
docker-compose up -d

# Kiểm tra container đang chạy
docker ps

# Xem logs của database
docker-compose logs -f db
```

### Bước 3: Kiểm Tra Kết Nối Database

```bash
# Kết nối vào PostgreSQL container
docker exec -it hypebeast_postgres_container psql -U postgres -d hypebeast_db

# Trong PostgreSQL shell, kiểm tra database
\l          # Liệt kê databases
\dt         # Liệt kê tables (sau khi chạy app lần đầu)
\q          # Thoát
```

---

## ⚙️ Cấu Hình Ứng Dụng

### File Cấu Hình Chính

Cấu hình ứng dụng nằm trong `src/main/resources/application.yaml`:

```yaml
server:
  port: ${SERVER_PORT}  # Mặc định: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: update  # Tự động tạo/cập nhật schema
    show-sql: true      # Hiển thị SQL queries

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 giờ
```

### Các Biến Môi Trường Quan Trọng

| Biến | Mô Tả | Giá Trị Mặc Định |
|------|-------|------------------|
| `POSTGRES_USER` | Username PostgreSQL | `postgres` |
| `POSTGRES_PASSWORD` | Password PostgreSQL | `password123` |
| `POSTGRES_DB` | Tên database | `hypebeast_db` |
| `POSTGRES_PORT` | Port PostgreSQL | `5432` |
| `SERVER_PORT` | Port ứng dụng Spring Boot | `8080` |
| `JWT_SECRET` | Secret key cho JWT | (có sẵn) |

---

## 🏃 Chạy Ứng Dụng

### Phương Án 1: Sử Dụng Maven Wrapper (Khuyến Nghị)

#### Windows:
```bash
# Build project
.\mvnw clean install

# Chạy ứng dụng
.\mvnw spring-boot:run
```

#### macOS/Linux:
```bash
# Cấp quyền thực thi cho Maven Wrapper
chmod +x mvnw

# Build project
./mvnw clean install

# Chạy ứng dụng
./mvnw spring-boot:run
```

### Phương Án 2: Sử Dụng Maven Global

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

### Phương Án 3: Chạy File JAR

```bash
# Build JAR file
./mvnw clean package

# Chạy JAR
java -jar target/Hung_Hypebeast_Backend-0.0.1-SNAPSHOT.jar
```

### Kiểm Tra Ứng Dụng Đã Chạy

Sau khi khởi động thành công, bạn sẽ thấy log:

```
Started HungHypebeastBackendApplication in X.XXX seconds
```

Truy cập: **http://localhost:8080**

---

## 🌱 Migration và Seed Dữ Liệu

### Tự Động Migration

Ứng dụng sử dụng **Hibernate DDL Auto** với mode `update`, tự động tạo/cập nhật schema khi khởi động.

### Seed Dữ Liệu Mẫu

Dữ liệu mẫu được tự động seed khi ứng dụng khởi động lần đầu thông qua class `DataMigration.java`.

#### Dữ Liệu Được Seed:

1. **Admin User**
   - Username: `admin`
   - Password: `admin123`
   - Email: `admin@hypebeast.com`
   - Role: `ADMIN`

2. **Categories** (2 danh mục)
   - Áo Thời Trang
   - Quần Thời Trang

3. **Products** (3 sản phẩm)
   - Áo Thun Rồng Hypebeast
   - Hoodie Basic Zip
   - Quần Short Kaki Túi Hộp

4. **Product SKUs** (12 biến thể)
   - Mỗi sản phẩm có nhiều size và màu sắc
   - Mỗi SKU có 10 sản phẩm trong kho

### Kiểm Tra Dữ Liệu Đã Seed

```bash
# Kết nối vào database
docker exec -it hypebeast_postgres_container psql -U postgres -d hypebeast_db

# Kiểm tra dữ liệu
SELECT * FROM users;
SELECT * FROM category;
SELECT * FROM product;
SELECT * FROM product_sku;
```

### Reset Database (Nếu Cần)

```bash
# Dừng và xóa container
docker-compose down -v

# Khởi động lại
docker-compose up -d

# Chạy lại ứng dụng để seed dữ liệu mới
./mvnw spring-boot:run
```

---

## 📚 API Documentation

### Swagger UI

Sau khi khởi động ứng dụng, truy cập Swagger UI để xem và test API:

**URL:** http://localhost:8080/swagger-ui/index.html


## 🔐 Tài Khoản Mặc Định

### Admin Account

```
Username: admin
Password: admin123
Email: admin@hypebeast.com
Role: ADMIN
```


## 🛠️ Troubleshooting

### Lỗi: Port 8080 đã được sử dụng

```bash
# Thay đổi port trong file .env
SERVER_PORT=8081

# Hoặc chạy với port khác
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Lỗi: Không kết nối được database

1. Kiểm tra Docker container đang chạy:
```bash
docker ps
```

2. Kiểm tra logs của PostgreSQL:
```bash
docker-compose logs db
```

3. Restart container:
```bash
docker-compose restart
```

### Lỗi: Maven build failed

```bash
# Clean và rebuild
./mvnw clean install -U

# Skip tests nếu cần
./mvnw clean install -DskipTests
```

### Lỗi: Permission denied (Linux/macOS)

```bash
# Cấp quyền cho Maven Wrapper
chmod +x mvnw
```

---

## 📝 Development Tips

### Hot Reload

Ứng dụng đã cấu hình Spring Boot DevTools để tự động reload khi có thay đổi code.

### View SQL Queries

SQL queries được hiển thị trong console khi `show-sql: true` trong `application.yaml`.

### Database GUI Tools

Bạn có thể sử dụng các tool sau để quản lý database:
- [DBeaver](https://dbeaver.io/)
- [pgAdmin](https://www.pgadmin.org/)
- [DataGrip](https://www.jetbrains.com/datagrip/)

**Connection Info:**
- Host: `localhost`
- Port: `5432`
- Database: `hypebeast_db`
- Username: `postgres`
- Password: `password123`

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra logs trong console
2. Xem lại các bước cài đặt
3. Đảm bảo tất cả services (Docker, Database) đang chạy
4. Liên hệ team để được hỗ trợ

---

## 📄 License

[Thêm thông tin license của bạn ở đây]
