# Semiconductor Inventory Management System

Đây là một ứng dụng web quản lý linh kiện bán dẫn (semiconductor inventory management) xây dựng theo kiến trúc Jakarta EE, sử dụng EJB, JPA và JSF. Ứng dụng hỗ trợ quản lý linh kiện, nhà cung cấp, giao dịch nhập/xuất kho, báo cáo và theo dõi tồn kho.

## Ứng dụng dùng để làm gì?

Trang web này được dùng để:

- Quản lý danh sách linh kiện bán dẫn
- Theo dõi số lượng tồn kho và cảnh báo khi sắp hết hàng
- Quản lý nhà cung cấp
- Ghi nhận các giao dịch kho
- Xem báo cáo và thống kê hoạt động
- Kiểm tra, test các chức năng EJB và Message-Driven Bean

## Công nghệ sử dụng

- **Java**
- **Jakarta EE 10**
- **EJB**
- **JPA**
- **JSF / Facelets**
- **HTML**
- **CSS**
- **JavaScript**
- **Apache Ant**
- **GlassFish 7 / Payara 6**
- **Microsoft SQL Server**

## Cấu trúc dự án

- `Semiconductor_Inventory_Management_System-ejb`: phần nghiệp vụ, entity, EJB
- `Semiconductor_Inventory_Management_System-war`: phần giao diện web JSF
- `dist/`: file build để deploy

## Chạy dự án

- Cần Java JDK 17+ hoặc 21
- Cần Apache Ant
- Cần GlassFish 7 hoặc Payara 6
- Cần cấu hình JDBC driver cho SQL Server

Build:

```bash
ant clean dist
```

Sau khi deploy, ứng dụng có thể truy cập qua URL mặc định của module web.

## Ghi chú

Dự án này phù hợp cho bài tập/đồ án về quản lý kho linh kiện với kiến trúc Java EE tách biệt giữa tầng giao diện và tầng nghiệp vụ.
