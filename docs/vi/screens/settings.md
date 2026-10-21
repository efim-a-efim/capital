---
layout: screen
lang: vi
base: "/vi"
key: "screens/settings"
screen: settings
title: Cài đặt
---
# Cài đặt

**Đây là gì.** Mọi thứ không phải là bản ghi: tùy chọn, nguồn dữ liệu, bảo mật và thư mục. Mở bằng biểu tượng bánh răng trên thanh trên cùng; trên màn hình rộng, đây là một thẻ trong thanh điều hướng bên.

**Thông báo.** Một thanh màu vàng ở đầu màn hình cho biết khi các giá trị đã lưu đệm bị cũ (hãy làm mới khi có mạng) hoặc khi định giá chưa đầy đủ do thiếu số dư hoặc tỷ giá. Thanh này chỉ xuất hiện ở đây.

**Tùy chọn.** Tiền tệ mặc định và giao diện (Hệ thống, Sáng, Tối). Tiền tệ mặc định dùng để định giá màn hình Tổng quan và được đề xuất cho hũ và mục tiêu mới; các bản ghi hiện có giữ nguyên tiền tệ của chúng. **Ngôn ngữ** đổi ngôn ngữ giao diện ngay lập tức; *Mặc định hệ thống* theo ngôn ngữ của thiết bị.

**Nhà cung cấp dữ liệu miễn phí.** Mỗi loại dữ liệu một dòng, kèm nhà cung cấp đang dùng: số dư BTC, ETH, TON, TRX; danh sách token ETH, TON, TRX; giá tiền mã hóa; tỷ giá tiền pháp định; **Lạm phát** (**World Bank**, **ECB**, **OECD**, **Tắt**) và **Tăng trưởng thị trường** (**World Bank**, **Yahoo Finance · không chính thức**, **Tắt**), dùng cho [mục tiêu FIRE]({{ page.base }}/screens/goal#fire) và giữ nguyên mặc định tích hợp khi **Tắt**; **Lịch sử tỷ giá** (**Frankfurter + DefiLlama**, **Tắt**) điền lịch sử tỷ giá hằng ngày của mọi tài sản bạn nắm giữ, ngược về ngày cũ nhất trong dữ liệu của bạn, để có thể tính giá trị trong quá khứ. Chạm vào một dòng để chọn nhà cung cấp khác, chuyển tra cứu token của một chuỗi sang **Tắt**, hoặc nhập khóa API không bắt buộc. Khóa được lưu mã hóa trên thiết bị và chỉ được gửi đến nhà cung cấp đã cấp khóa đó. **Kiểm tra nguồn / làm mới danh mục** truy vấn từng nhà cung cấp với những tài sản bạn thực sự nắm giữ và báo cáo những gì thất bại. Không nhà cung cấp nào bị âm thầm thay thế.

**Môi giới.** Các tài khoản môi giới và thông tin xác thực của chúng có màn hình riêng: [Môi giới]({{ page.base }}/screens/brokers).

**Tự động cập nhật tỷ giá.** **Tắt**, **Mỗi 24 giờ**, **Mỗi 12 giờ**, **Mỗi 6 giờ**, **Mỗi 3 giờ** hoặc **Mỗi 1 giờ**. Khi bật, một tác vụ nền tải tỷ giá hiện tại của mọi tài sản bạn nắm giữ theo chu kỳ đó, với các nhà cung cấp đã chọn ở trên, và lưu các mẫu vào lịch sử tỷ giá. Tác vụ không bao giờ động đến thư mục dữ liệu: lần tới khi bạn mở ứng dụng hoặc làm mới, các mẫu được gộp vào giá đã lưu đệm, nên tổng giá trị bắt đầu từ tỷ giá mới nhất, kể cả khi ngoại tuyến. Cần kết nối mạng và tốn một chút pin và dữ liệu. Lần chạy gần nhất hiển thị dưới **Giá và độ cập nhật** là *Lần cập nhật nền gần nhất*.

**Giá và độ cập nhật.** Mọi giá đã lưu đệm, cùng mọi chuỗi lạm phát và tăng trưởng đã tải, kèm nguồn, thời điểm ghi nhận và thời điểm lấy về. Giá cũ vẫn được dùng và được đánh dấu bằng thông báo ở đầu màn hình này.

**Bảo mật.** **Mã hóa** mã hóa mọi tệp trong thư mục bằng mật khẩu; tắt mã hóa sẽ giải mã chúng. Khi bật mã hóa, bạn có thể **Đặt PIN**, bật **Dùng sinh trắc học**, chọn **Khóa sau thời gian ở chế độ nền** và **Đổi mật khẩu**. Không thể khôi phục mật khẩu. Nhập sai PIN mười lần sẽ xóa PIN; mật khẩu luôn dùng được. Xem [Bảo mật]({{ page.base }}/manual#security).

**Lưu trữ.** Thư mục hiện tại và phiên bản của nó. **Kết nối lại / mở thư mục** chạy lại trình chọn thư mục; **Tải lại tệp cục bộ** đọc lại thư mục, ví dụ sau khi công cụ đồng bộ đã chuyển về các thay đổi; **Xuất bản sao lưu** ghi ra một tệp di động duy nhất (không mã hóa khi mã hóa đang tắt, và được ghi rõ như vậy); **Khôi phục bản sao lưu** kiểm tra tính hợp lệ của tệp trước khi thay thế các bản ghi và giữ lại các bản lưu hiện có.

**Nguồn / ghi công.** Liên kết đến trang web của từng nhà cung cấp.

**Pháp lý.** Liên kết đến [Chính sách quyền riêng tư]({{ page.base }}/privacy), các bản khai [An toàn dữ liệu]({{ page.base }}/data-safety) và [Tính năng tài chính]({{ page.base }}/financial-features) trên trang web này, theo ngôn ngữ giao diện. Phiên bản ứng dụng và số bản dựng ở cuối trang.
