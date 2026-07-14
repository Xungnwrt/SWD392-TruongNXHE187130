# AI Usage Log — J2.S.P0113 Manage Book

**Subject:** LAB221 / SWD  
**Project:** Book Management (Java Swing, N-Layer)  
**AI tool:** Cursor (Composer / Grok assistant)  
**Date:** July 2026

---

## 1. Prompt đã dùng (tóm tắt)

Prompt chính yêu cầu mentor/architect hỗ trợ lab Manage Book theo quy trình:

1. Tổng hợp yêu cầu từ `J2.S.P0113.docx` + `BookManagement_ProjectDoc.docx`
2. Đề xuất kiến trúc N-Layer (N > 3) — **dừng xác nhận**
3. Đề xuất Design Pattern — **dừng xác nhận**
4. Vẽ Class Diagram (Visual Paradigm) + xuất PNG
5. Vẽ Sequence Diagram (Add / Remove) + xuất PNG
6. Code Java Swing đúng kiến trúc + pattern (SQL Server JDBC)
7. Viết `AI_USAGE_LOG.md` (file này)

Ràng buộc: chỉ lấy nghiệp vụ từ ProjectDoc; stack kỹ thuật là **Java + Swing + N-Layer**, không Web/.NET/React.

---

## 2. Các đề xuất AI đưa ra

| Bước | Đề xuất AI | Kết quả |
|---|---|---|
| 1 | Entity Book (6 attrs), 4 chức năng CRUD, actor desktop, ràng buộc Remove → select first | User: **OK Bước 1** |
| 2 | 5 layer: Presentation → Controller → Service → DAO → Infrastructure (+ Model shared) | User: **OK Bước 2** + DB = SQL Server |
| 3 | Pattern: **DAO + Singleton + Observer** (không dùng Factory) | User: **OK Bước 3** |
| 4–5 | Class Diagram + Sequence trong `BookManagement.vpp`, PNG trong `docs/diagrams/` | User: **OK Bước 4/5** |
| 6 | Code Java theo package layer, JDBC SQL Server | **Hoàn tất / đã chạy demo** |

---

## 3. Phần user yêu cầu chỉnh / chốt

- Loại bỏ kiến trúc Web API/.NET/React/Mobile khỏi scope kỹ thuật
- Dừng xác nhận ở Bước 2 và Bước 3 trước khi vẽ diagram / code
- DB: **SQL Server** (chốt trước Bước 6)
- Pattern cuối: DAO + Singleton + Observer
- Cấu hình Windows Authentication (`XTLAP` / `localhost`) + bật TCP/IP port 1433

---

## 4. Lý do chọn pattern cuối cùng

1. **DAO (`IBookDAO` / `BookDAO`)** — tách SQL/JDBC khỏi Service; thể hiện DIP trên Class Diagram.
2. **Singleton (`DBConnectionManager`)** — một điểm cấu hình kết nối SQL Server.
3. **Observer (`BookChangeListener` + `BookService` Subject)** — refresh `JList` sau CRUD; khớp yêu cầu Remove → hiển thị sách đầu tiên.

Không dùng Factory Method vì entity `Book` cố định.

---

## 5. SOLID được áp dụng trong code

| Nguyên tắc | Class / nơi áp dụng |
|---|---|
| **S** | Frame = UI; Controller = sự kiện; Service = rule; DAO = JDBC; DBConnectionManager = connection |
| **O** | Thêm impl `IBookDAO` mới không sửa Service |
| **L** | `BookService` dùng mọi `IBookDAO`; Controller dùng mọi `IBookService` |
| **I** | `IBookService`, `IBookDAO`, `BookChangeListener` nhỏ, đúng việc |
| **D** | Phụ thuộc interface, không phụ thuộc class cụ thể ở Controller/Service |

Presentation **không** gọi DAO/JDBC trực tiếp.

---

## 6. Cách chạy

1. Chạy `sql/schema.sql` trên SQL Server  
2. Kiểm tra `resources/db.properties` (Windows Auth / localhost)  
3. `compile.bat` rồi `run.bat` (hoặc VS Code mở qua `C:\dev\ProjectSWD`)
