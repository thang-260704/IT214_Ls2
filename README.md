gi# Bài tập 2: "Bản đồ dẫn đường" với Correlation ID & Tracing

Hệ thống đặt vé xem phim theo mô hình **Choreography Saga**, gồm 3 Spring Boot service (Java 17) giao tiếp qua **Kafka**. Mỗi giao dịch mang **một Correlation ID duy nhất**, được truyền qua **header** của tin nhắn Kafka.

## 1. Mô tả luồng sự kiện & vai trò của Correlation ID

```
Client ──POST /api/bookings──► MovieBookingService (8081)
                                  │ sinh correlationId (UUID)
                                  │ BookingCreated  [header: correlationId]
                                  ▼
                            topic: booking-events
                                  ▼
                        SeatAllocationService (8082)
                                  │ đọc correlationId từ header, giữ ghế
                                  │ SeatConfirmed   [header: correlationId]
                                  ▼
                          topic: seat-confirmed-events
                                  ▼
                            PaymentService (8083)
                                  │ đọc correlationId từ header, thanh toán
                                  │ PaymentResult   [header: correlationId]
                                  ▼
                            topic: payment-events
```

Trong Choreography Saga không có "nhạc trưởng" điều phối, mỗi service tự phản ứng với sự kiện. Nếu không có mã định danh chung, log của các service rời rạc, không biết dòng log nào thuộc cùng một vé (**Event Spaghetti**). Correlation ID đóng vai trò "sợi chỉ xuyên suốt": chỉ cần `grep <correlationId>` trên log của tất cả service là dựng lại toàn bộ hành trình của một vé. Nó cũng là nền tảng để các công cụ tracing (Jaeger/Zipkin) ghép các span thành một trace.

## 2. Vì sao gắn Correlation ID vào header, không phải payload?

| Tiêu chí | Header | Payload |
|---|---|---|
| Thay đổi model nghiệp vụ | Không cần | Phải thêm trường vào mọi event |
| Tách bạch | Metadata kỹ thuật tách khỏi dữ liệu nghiệp vụ | Trộn lẫn hai loại dữ liệu |
| Đọc mà không parse JSON | Có (`record.headers().lastHeader(...)`) | Phải deserialize toàn bộ |
| Tương thích ngược | Consumer cũ vẫn chạy bình thường | Đổi schema có thể gây lỗi |
| Middleware/Interceptor | Dễ tự động hoá (propagate, tracing) | Khó, phải hiểu từng schema |

Kỹ thuật cài đặt:

- **Producer**: `record.headers().add("correlationId", id.getBytes(UTF_8))` trên `ProducerRecord`.
- **Consumer**: `@KafkaListener` nhận `ConsumerRecord<String,String>`, đọc `record.headers().lastHeader("correlationId")` rồi `new String(value, UTF_8)`.
- **Truyền tiếp**: khi service publish sự kiện kế tiếp, nó lại gắn **chính correlationId đã nhận** vào header mới. ID không bao giờ được sinh lại.

## 3. Hướng dẫn cài đặt & chạy

**Yêu cầu:** JDK 17, Maven 3.8+ (hoặc dùng Maven tích hợp trong IntelliJ), Docker (để chạy Kafka).

### Bước 1: Chạy Kafka
```bash
docker compose up -d
```
Kafka chạy ở `localhost:9092` (KRaft, không cần Zookeeper). Các topic được các service **tự tạo khi khởi động**. Nếu muốn tạo thủ công:
```bash
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic booking-events --partitions 1 --replication-factor 1
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic seat-confirmed-events --partitions 1 --replication-factor 1
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic payment-events --partitions 1 --replication-factor 1
```

### Bước 2: Chạy 3 service (mở 3 terminal, hoặc 3 Run Configuration trong IntelliJ)
```bash
mvn -pl payment-service spring-boot:run
mvn -pl seat-allocation-service spring-boot:run
mvn -pl movie-booking-service spring-boot:run
```
Trong IntelliJ: mở `pom.xml` gốc → *Open as Project* → chạy lần lượt `PaymentApplication`, `SeatAllocationApplication`, `MovieBookingApplication`.

### Bước 3: Gửi yêu cầu đặt vé
```bash
curl -X POST http://localhost:8081/api/bookings \
  -H "Content-Type: application/json" \
  -d @booking-request.json
```
(Hoặc mở `movie-booking-service/booking.http` trong IntelliJ và bấm ▶.)

Response trả về `correlationId` (cả trong body lẫn header `X-Correlation-Id`) để tra log.

## 4. Kết quả chạy thử

Log mong đợi với dữ liệu đầu vào (`CIN-2024-789`), correlationId giống hệt nhau ở mọi service:

```
[MovieBookingService] Created booking CIN-2024-789. CorrelationID: 550e8400-e29b-41d4-a716-446655440000
[SeatAllocationService] Received SeatRequest for CIN-2024-789. CorrelationID: 550e8400-e29b-41d4-a716-446655440000
[SeatAllocationService] Seat reserved: A12, A13. CorrelationID: 550e8400-e29b-41d4-a716-446655440000
[PaymentService] Processing Payment for CIN-2024-789. CorrelationID: 550e8400-e29b-41d4-a716-446655440000
[PaymentService] Payment success: 240000 VND. CorrelationID: 550e8400-e29b-41d4-a716-446655440000
```

> **Lưu ý:** UUID thực tế sẽ khác mỗi lần chạy. Hãy thay khối log trên bằng ảnh chụp/log thật của bạn khi nộp bài.

## 5. Cấu trúc dự án

```
movie-ticket-saga/
├── pom.xml                       (parent, Java 17, Spring Boot 3.2.5)
├── docker-compose.yml            (Kafka KRaft)
├── booking-request.json
├── movie-booking-service/        (Producer khởi tạo, port 8081)
├── seat-allocation-service/      (Consumer & Producer, port 8082)
└── payment-service/              (Consumer & Producer kết quả, port 8083)
```

## 6. Mở rộng (tuỳ chọn)

- Thêm `spring-boot-starter-actuator` + Micrometer Tracing / OpenTelemetry để xuất trace sang Jaeger/Zipkin.
- Đưa correlationId vào **MDC** để tự động in ở mọi dòng log.
- Xử lý bù trừ (compensation) khi giữ ghế/thanh toán thất bại.
