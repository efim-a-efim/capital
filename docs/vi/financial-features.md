---
layout: default
lang: vi
base: "/vi"
key: "financial-features"
title: Khai báo tính năng tài chính
class: doc
---
# Khai báo tính năng tài chính

<p class="meta">Câu trả lời cho biểu mẫu trong Google Play Console (Policy and programs → App content → Financial features (Chính sách và chương trình → Nội dung ứng dụng → Tính năng tài chính)), kèm lập luận. Đã rà soát theo phiên bản 2.2.1 vào ngày 30 tháng 9 năm 2026.</p>

## Câu trả lời cho biểu mẫu

**Select all of the financial features the app provides (Chọn tất cả tính năng tài chính mà ứng dụng cung cấp):** **The app does not provide any financial features (Ứng dụng không cung cấp tính năng tài chính nào).**

## Lý do

Capital là ứng dụng theo dõi tiết kiệm cá nhân. Ứng dụng ghi lại những gì người dùng đã sở hữu và cho thấy số tiền tiết kiệm đó tương ứng thế nào với các mục tiêu của chính người dùng. Đối chiếu với từng tính năng trong biểu mẫu:

| Tính năng trong biểu mẫu | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later (Bên cho vay cá nhân trực tiếp, bên hỗ trợ cho vay, khoản vay ngắn hạn trả vào ngày lĩnh lương, hạn mức tín dụng, ứng lương, tài chính vi mô, mua trước trả sau) | Không cho vay dưới bất kỳ hình thức nào |
| Banking (Ngân hàng) | Không có tài khoản, tiền gửi hay quyền truy cập tài khoản. Số dư ngân hàng do người dùng tự nhập |
| Mobile payments and digital wallets, money transfer and wire services (Thanh toán di động và ví điện tử, dịch vụ chuyển tiền và chuyển khoản) | Không thể gửi, nhận hay giữ tiền. Phân bổ cho mục tiêu là phép tính hiển thị trên màn hình; nó không di chuyển gì cả |
| Cryptocurrency wallet (Ví tiền mã hóa) | Đọc số dư của các địa chỉ công khai mà người dùng dán vào. Ứng dụng không bao giờ giữ khóa riêng tư hay cụm từ khôi phục và không thể ký hay phát tán giao dịch, nên nó không phải là ví |
| Cryptocurrency exchange (Sàn giao dịch tiền mã hóa) | Không giao dịch, không định tuyến lệnh, không có kênh nạp tiền pháp định |
| Rewards and incentives, crowdfunding and chit funds, prediction markets (Phần thưởng và ưu đãi, gọi vốn cộng đồng và hụi/họ, thị trường dự đoán) | Không có |
| Credit monitoring and reporting (Theo dõi và báo cáo tín dụng) | Không có |
| Financial advice (Tư vấn tài chính) | Không có. Phần dự báo chỉ là phép tính số học trên các con số của chính người dùng ("Các khoản tiết kiệm dự kiến sẽ hoàn thành mục tiêu này vào …"); nó không khuyến nghị sản phẩm, tài sản hay hành động nào. Công cụ tính tái cân bằng liệt kê các lệnh mua cần thiết để khớp với tỷ lệ phần trăm do chính người dùng đặt |
| Insurance (Bảo hiểm) | Không có |

Ứng dụng cũng không có giao dịch mua trong ứng dụng và không có tính năng trả phí.

## Nếu người đánh giá không đồng ý

Nếu bộ phận đánh giá của Play vẫn xếp ứng dụng vào loại có cung cấp tính năng tài chính, lựa chọn gần nhất là **Other** (Khác) với mô tả sau:

> Ứng dụng theo dõi tiết kiệm cá nhân, chỉ đọc. Người dùng tự nhập số dư hoặc dán địa chỉ blockchain công khai; ứng dụng lấy số dư và giá thị trường từ các nguồn dữ liệu bên thứ ba và cho thấy số tiền tiết kiệm đáp ứng các mục tiêu của chính người dùng đến đâu. Không lưu ký tài sản, không giữ khóa, không giao dịch, không cho vay, không mua bán, không tư vấn.

Các yêu cầu riêng theo quốc gia đối với ứng dụng cho vay cá nhân, cũng như các câu hỏi về tiền mã hóa dành cho Hoa Kỳ, không áp dụng vì không tính năng nào trong số đó được chọn.

## Những thông tin liên quan mà người đánh giá có thể hỏi

- Dữ liệu thị trường đến từ các nhà cung cấp bên thứ ba do người dùng chọn (xem [Chính sách quyền riêng tư]({{ page.base }}/privacy)). Ứng dụng hiển thị tên và trang web của nhà cung cấp trong Cài đặt.
- Truy vấn ví dùng các API blockchain công khai, chỉ đọc.
- Ứng dụng chạy hoàn toàn trên thiết bị và không có máy chủ nào do nhà phát triển vận hành.
