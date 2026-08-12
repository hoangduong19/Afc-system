# Hệ Thống Thu Soát Vé Tự Động Liên Thông (AFC) - Phân Hệ Trung Tâm Thanh Toán Bù Trừ (FMC - Cấp 5)

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-orange.svg)](https://www.rabbitmq.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2B%20DDD-purple.svg)]()
[![Program](https://img.shields.io/badge/Viettel%20Digital%20Talent-2026-red.svg)](https://viettel.vn/)

## THÔNG TIN DỰ ÁN

* **Đề tài:** Xây dựng một hệ thống thẻ vé hành khách đơn giản phục vụ vận hành các tuyến đường sắt đô thị và giao thông công cộng.
* **Chương trình:** Viettel Digital Talent 2026
* **Lĩnh vực:** Software Engineer
* **Tác giả:** Hoàng Quốc Dương ([hoangduong190105@gmail.com](mailto:hoangduong190105@gmail.com))
* **Mentor hướng dẫn:** Nguyễn Hoàng Thông
* **Repositories:**
  * **Backend (FMC Center Level 5):** [https://github.com/hoangduong19/Afc-system](https://github.com/hoangduong19/Afc-system)
  * **Frontend (Admin Dashboard):** [https://github.com/hoangduong19/Afc-system-frontend](https://github.com/hoangduong19/Afc-system-frontend)

## GIỚI THIỆU TỔNG QUAN

Trong bối cảnh đô thị hóa nhanh chóng, việc chuyển đổi số giao thông công cộng (GTCC) thông qua **Hệ thống thu soát vé tự động liên thông (Automated Fare Collection - AFC)** là xu hướng tất yếu. Hệ thống AFC liên thông cho phép hành khách sử dụng một phương thức thanh toán/thẻ vé thống nhất trên nhiều phương tiện (Xe buýt, Đường sắt đô thị - Metro), đồng thời giúp các cơ quan quản lý và đơn vị vận hành (DNVH) thu thập dữ liệu tập trung, tính toán giá vé chính xác và thực hiện thanh toán bù trừ, phân bổ doanh thu công bằng.

Dự án này được thiết kế và triển khai dựa trên **Quyết định số 3680/QĐ-UBND** của UBND Thành phố Hà Nội (về Quy chuẩn kỹ thuật khung cho hệ thống vé điện tử liên thông) và tiêu chuẩn quốc tế **ISO/IEC 24014-1** (Kiến trúc hệ thống AFC liên thông).

### Mô Hình Phân Cấp 5 Cấp Trong Hệ Thống AFC
Hệ thống AFC liên thông được tổ chức thành 5 cấp chức năng độc lập:
1. **Cấp 1 – Vé & Phương thức thanh toán:** Thẻ thông minh (Smart Card), QR code, Thẻ ngân hàng, Ví điện tử.
2. **Cấp 2 – Thiết bị đầu cuối:** Cổng kiểm soát (PG/Gate), Máy bán vé (TVM), Thiết bị soát vé trên xe buýt.
3. **Cấp 3 – Hệ thống Ga/Bến/Tuyến (Station/Line System):** Quản lý tập trung dữ liệu tại từng ga/tuyến.
4. **Cấp 4 – Hệ thống Đơn vị Vận hành (Operator System):** Chịu trách nhiệm vận hành của từng doanh nghiệp khai thác.
5. **Cấp 5 – Trung tâm Thanh toán Bù trừ (Fare Management & Clearinghouse Center - FMC):** ***Phân hệ trọng tâm của dự án này***, đóng vai trò là "bộ não" trung tâm chịu trách nhiệm:
   * Quản lý thông tin thẻ/vé và danh sách đen (Blacklist).
   * Quản lý quy tắc giá vé linh hoạt và chính sách ưu đãi.
   * Tiếp nhận & xử lý dữ liệu giao dịch khối lượng lớn từ Cấp 4.
   * Xử lý hành trình bất thường, đối soát tự động & phân bổ doanh thu giữa các nhà vận hành.

```
+-----------------------------------------------------------------------+
|                 CẤP 5: TRUNG TÂM THANH TOÁN BÙ TRỪ (FMC)             |
|                 (Dự án này - Backend Spring Boot Core)                |
+-----------------------------------------------------------------------+
        ^                                               ^
        | (AMQP / RabbitMQ)                             | (RESTful API / HTTPS)
        v                                               v
+-----------------------------+         +-------------------------------+
| CẤP 4: ĐƠN VỊ VẬN HÀNH      |         |  ỨNG DỤNG HÀNH KHÁCH & FRONTEND|
| (Bus / Metro Operators)     |         |  (Admin Dashboard / Next.js)  |
+-----------------------------+         +-------------------------------+
        ^
        | (Thu thập dữ liệu)
+-----------------------------+
| CẤP 1 - CẤP 3: GA / THIẾT BỊ|
+-----------------------------+
```

## CÁC TÍNH NĂNG CHÍNH & GIẢI PHÁP NGHIỆP VỤ

### 1. Quản Lý Vòng Đời Thẻ & Danh Sách Đen (Card Lifecycle & Blacklist)
* **Finite State Machine (FSM):** Thẻ được quản lý chặt chẽ theo máy trạng thái hữu hạn (`ACTIVE` ↔ `SUSPENDED` → `REVOKED`), chống chuyển trạng thái không hợp lệ.
* **Đồng bộ danh sách đen thời gian thực:** Khi thẻ bị khóa (`SUSPENDED`) hoặc thu hồi (`REVOKED`), hệ thống phát sinh **Domain Event**, đẩy thông điệp bất đồng bộ qua RabbitMQ tới Cấp 4 và các thiết bị đầu cuối để từ chối giao dịch ngay lập tức.

### 2. Quản Lý Chính Sách Giá Vé Linh Hoạt (Fare Policy Management)
* **Versioning & Effective Date:** Quản lý quy tắc giá vé (`FareRule`), mức giá vé định kỳ (`FarePassPrice`) và chính sách ưu đãi (`FareDiscount`) theo từng phiên bản độc lập kèm thời gian hiệu lực. Cho phép điều chỉnh giá vé linh hoạt mà không làm mất lịch sử và không ảnh hưởng đến các giao dịch quá khứ.
* **Audit Logging:** Truy vết toàn bộ lịch sử chỉnh sửa giá vé (dữ liệu trước/sau, người thực hiện, thời gian, lý do) phục vụ kiểm toán nghiệp vụ.

### 3. Tiếp Nhận & Xử Lý Dữ Liệu Giao Dịch Khối Lượng Lớn (High-Performance Batch Processing)
* **Batch Preload (Giải quyết N+1 Query):** Trước khi xử lý lô giao dịch, hệ thống thực hiện gom mã thẻ/vé và load 1 lần vào memory cache, giữ số lượng query CSDL ở mức cố định ($O(1)$) thay vì phụ thuộc kích thước lô.
* **Redis Caching & JDBC Batch Write:** Caching dữ liệu tham chiếu tần suất đọc cao (nhà ga, nhà vận hành, bảng giá) trên Redis RESP; gom hàng nghìn kết quả ghi xuống PostgreSQL qua JDBC Batching theo từng Chunk.
* **Retry Mechanism & Dead Letter Queue (DLQ):** Áp dụng **Exponential Backoff Retry** xử lý lỗi tạm thời (mạng, lock database). Nếu vượt quá số lần retry, thông điệp tự động chuyển sang **DLQ** để đảm bảo không bị thất thoát giao dịch.

### 4. Phát Hiện Bất Thường, Đối Soát & Phân Bổ Doanh Thu (Clearing & Settlement)
* **Phát hiện bất thường:** Tự động nhận diện giao dịch trùng lặp (Duplicate), sai lệch giá vé (Fare Mismatch), hoặc chuyến đi không đầy đủ (Incomplete Journey - thiếu Checkout/Checkin).
* **Thuật toán phân bổ doanh thu (QĐ 3316):** Xử lý tính toán phân chia doanh thu chính xác cho vé lượt, vé ngày, vé tháng liên tuyến và vé đa phương thức giữa các nhà vận hành dựa trên tỷ lệ sử dụng thực tế.
* **Đối soát tự động (Reconciliation):** So sánh dữ liệu Cấp 4 báo cáo với kết quả Cấp 5 tính toán, phân loại theo các ngưỡng sai lệch (Khớp hoàn toàn, Cảnh báo ngưỡng nhỏ, Sai lệch nghiêm trọng) và hỗ trợ chốt sổ khóa kỳ quyết toán.

## KIẾN TRÚC MÃ NGUỒN (HEXAGONAL ARCHITECTURE + DDD)

Dự án áp dụng mô hình **Hexagonal Architecture (Ports & Adapters)** kết hợp **Domain-Driven Design (DDD)** nhằm tách biệt tuyệt đối logic nghiệp vụ cốt lõi khỏi tầng hạ tầng kỹ thuật:

```
src/main/java/com/metro/afc/
├── card/                # Domain Quản lý Thẻ & Vòng đời thẻ
├── ticket/              # Domain Quản lý Vé (Vé lượt, Vé ngày, Vé tháng)
├── fare/                # Domain Quản lý Quy tắc giá vé & Discount
├── trip/                # Domain Tiếp nhận & Xử lý Giao dịch chuyến đi
├── settlement/          # Domain Thanh toán bù trừ & Phân bổ doanh thu
├── operator/            # Domain Quản lý Đơn vị vận hành
├── station/ & route/    # Domain Quản lý Tuyến & Nhà ga
├── blacklist/           # Domain Quản lý Danh sách đen
├── identity/            # Domain Xác thực & Phân quyền (JWT, User, Role)
└── shared/              # Shared Kernel (DTO, Exception, Value Objects: Money)
```

Mỗi miền nghiệp vụ (Domain) được cấu trúc chuẩn 3 tầng:
* **`domain/`**: Chứa `Entities`, `Value Objects`, `Domain Events`, `Enums` và các giao diện `Ports` (không phụ thuộc bất kỳ framework nào).
* **`application/`**: Chứa `Application Services`, `Use Cases`, `DTOs` điều phối luồng nghiệp vụ.
* **`infrastructure/`**: Chứa các `Inbound Adapters` (REST Controllers, RabbitMQ Listeners) và `Outbound Adapters` (JPA Repositories, Spring AMQP Publishers, Redis Caching).

## CÔNG NGHỆ SỬ DỤNG

### Backend Core
* **Java 21** – Ngôn ngữ lập trình hiện đại.
* **Spring Boot 3.5.14** – Framework chính phát triển Backend API.
* **Spring Data JPA & Hibernate** – Tương tác cơ sở dữ liệu quan hệ.
* **Spring Security & JJWT (0.12.6)** – Xác thực không trạng thái (Stateless JWT Authentication) & Phân quyền RBAC.
* **Spring AMQP** – Giao tiếp bất đồng bộ qua RabbitMQ Broker.
* **Flyway DB** – Quản lý Migration và phiên bản CSDL.
* **Springdoc OpenAPI (v2.8.16)** – Tự động tạo tài liệu REST API (Swagger UI).
* **Lombok & MapStruct** – Tối ưu hóa mã nguồn boilerplate.

### Data & Caching
* **PostgreSQL 17** – Hệ quản trị CSDL quan hệ chính (Hỗ trợ đầy đủ giao dịch ACID, ràng buộc toàn vẹn tài chính).
* **Redis 7.0** – In-memory cache tối ưu tốc độ đọc quy tắc giá và danh mục hệ thống.
* **RabbitMQ 3 (Management)** – Message Broker phục vụ truyền nhận thông điệp AMQP.

### DevOps, Testing & Infrastructure
* **Docker & Docker Compose** – Đóng gói container hóa toàn bộ môi trường.
* **Testcontainers (JUnit 5)** – Integration Testing với container PostgreSQL & RabbitMQ thực tế.
* **JaCoCo Plugin** – Đo lường độ bao phủ kiểm thử (Code Coverage).
* **CloudAMQP & Railway** – Nền tảng triển khai đám mây thử nghiệm.

## HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY DỰ ÁN

### 1. Yêu Cầu Tiền Đề (Prerequisites)
* **JDK 21** hoặc cao hơn.
* **Maven 3.9+** (hoặc sử dụng wrapper `./mvnw` tích hợp sẵn).
* **Docker & Docker Compose** (cho môi trường local).

### 2. Cấu Hình Môi Trường (.env)
Tạo file `.env` từ file mẫu `.env .example` ở thư mục gốc dự án:

```bash
cp ".env .example" .env
```

Nội dung cấu hình mẫu (`.env`):
```env
DB_NAME=afc_db
DB_USERNAME=afc_user
DB_PASSWORD=afc_password
JWT_SECRET=afc-jwt-secret-key-2025-minimum-32-chars-length

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

### 3. Khởi Chạy Hạ Tầng Với Docker Compose
Khởi chạy các dịch vụ PostgreSQL 17, Redis 7 và RabbitMQ 3:

```bash
docker-compose up -d postgres rabbitmq redis
```

Kiểm tra trạng thái các container:
```bash
docker-compose ps
```

### 4. Khởi Chạy Backend Server
Chạy ứng dụng Spring Boot bằng Maven Wrapper:

* **Trên Linux/macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```
* **Trên Windows (PowerShell/CMD):**
  ```cmd
  .\mvnw.cmd spring-boot:run
  ```

Hoặc bạn có thể khởi chạy toàn bộ hệ thống (bao gồm cả Backend) trong Docker:
```bash
docker-compose up -d --build
```

## TRUY CẬP ĐỊA CHỈ DỊCH VỤ

Khi ứng dụng chạy thành công, bạn có thể truy cập các đường dẫn sau:

| Dịch vụ | URL / Địa chỉ | Ghi chú |
| :--- | :--- | :--- |
| **Backend REST API** | `http://localhost:8080` | Spring Boot Application |
| **Swagger UI (API Docs)** | `http://localhost:8080/swagger-ui.html` | Tài liệu API tương tác công khai |
| **OpenAPI Spec (JSON)** | `http://localhost:8080/v3/api-docs` | Cấu hình OpenAPI spec |
| **RabbitMQ Management** | `http://localhost:15672` | User/Pass: `guest`/`guest` |
| **PostgreSQL Database** | `localhost:5432` | DB: `afc_db`, User: `afc_user` |
| **Redis Server** | `localhost:6379` | Port mặc định Redis |
| **Frontend Admin App** | `https://github.com/hoangduong19/Afc-system-frontend` | Repository Frontend Next.js |

## KẾT QUẢ THỬ NGHIỆM & BENCHMARK

Hệ thống đã được kiểm thử tải (Benchmark) nhằm đánh giá khả năng xử lý lô dữ liệu chuyến đi lớn từ Cấp 4:

* **Môi trường benchmark:** PostgreSQL 17 + Redis 7 + Spring Boot 3.5 (Java 21).
* **Kết quả Durchsatz (Throughput):**
  * Xử lý thông thường: **~1,200 - 1,500 transactions/sec**.
  * Với **Batch Preload + Redis Cache + JDBC Batching**: Tăng tốc độ xử lý lô lên gấp **~4.5 lần**, giảm chi phí truy vấn cơ sở dữ liệu N+1 về mức $O(1)$.
* **Độ tin cậy:** Kiểm thử ngắt kết nối CSDL đột ngột kích hoạt cơ chế **Exponential Backoff Retry** và **Dead Letter Queue (DLQ)** thành công, đảm bảo tỷ lệ thất thoát thông điệp là **0%**.

## HƯỚNG PHÁT TRIỂN TRONG TƯƠNG LAI

1. **Kiến trúc tầng biên (Edge Gateway & Rate Limiting):** Bổ sung API Gateway đóng vai trò làm điểm vào duy nhất, tích hợp Rate Limiting và Web Application Firewall (WAF).
2. **Mở rộng cơ sở dữ liệu (CQRS / Event Sourcing & Database Sharding):** Tách biệt CSDL Read/Write (CQRS) đối với các truy vấn lịch sử chuyến đi của hành khách, áp dụng Partitioning/Sharding theo thời gian cho các bảng dữ liệu giao dịch khổng lồ.
3. **Mở rộng Microservices:** Tách các Domain nghiệp vụ chính (`Settlement Service`, `Card Lifecycle Service`, `Fare Policy Service`) thành các Microservices độc lập khi quy mô giao dịch toàn thành phố tăng cao.

## TÀI LIỆU THAM KHẢO

1. **Quyết định số 3680/QĐ-UBND** của UBND Thành phố Hà Nội ngày 10/07/2023 về việc ban hành Quy chuẩn kỹ thuật khung cho hệ thống vé điện tử liên thông trong giao thông công cộng.
2. **Quyết định số 3316/QĐ-UBND** của UBND Thành phố Hà Nội về Hướng dẫn tạm thời cơ chế thanh toán, bù trừ và phân bổ doanh thu vé điện tử liên thông.