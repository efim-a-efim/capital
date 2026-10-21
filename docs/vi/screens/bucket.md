---
layout: screen
lang: vi
base: "/vi"
key: "screens/bucket"
screen: bucket
title: Chi tiết hũ
---
# Chi tiết hũ

**Đây là gì.** Một hũ cùng các tài sản của nó. Mở bằng cách chạm vào một thẻ trên thẻ [Hũ]({{ page.base }}/screens/buckets); **← Tất cả các hũ** để quay lại.

**Phần đầu.** Giá trị của hũ, tiếp theo là hai con số chỉ có ý nghĩa khi đi cùng nhau: **Đã phân bổ**, phần mà các mục tiêu đã liên kết nhận, và **Khả dụng**, phần còn lại. **Sửa hũ** mở phần tên, tiền tệ và các công tắc danh mục đầu tư. **Xóa hũ** xóa hũ cùng các tài sản của nó sau khi xác nhận.

**Tài sản.** Mỗi tài sản hiển thị tên, giá trị theo tiền tệ của hũ, cách theo dõi (*Thủ công*, *Ví* hoặc *Tài khoản môi giới*), số lượng theo đơn vị gốc, thời điểm giá trị được ghi nhận và thời điểm lấy về gần nhất. **Sửa / chuyển** để thay đổi tài sản hoặc chuyển nó sang hũ khác; **Xóa** để xóa nó.

**Thêm tài sản** mở trình chỉnh sửa tài sản:

- **Thủ công**: tên, mã tiền tệ hoặc mã tài sản và số lượng. Dùng cho bất cứ thứ gì ứng dụng không tự đọc được.
- **Ví**: chọn chuỗi (BTC, ETH, TON, TRX) và dán một địa chỉ công khai. Khi làm mới, ứng dụng đọc số dư gốc và, trên ETH, TON và TRX, cả các token có thể thay thế trên địa chỉ đó. Mở lại trình chỉnh sửa và nhấn **Lấy token** để xem chúng và tắt những token bạn không muốn tính vào.
- **Tài khoản môi giới**: chọn một trong các tài khoản đã thêm ở màn hình [Môi giới]({{ page.base }}/screens/brokers); để trống tên để dùng tên của tài khoản. Khi làm mới, ứng dụng đọc tổng giá trị của tài khoản theo tiền tệ cơ sở của tài khoản. Một tài khoản chỉ có thể được liên kết với một hũ; xóa tài sản sẽ hủy liên kết mà không xóa tài khoản.

**Token và "không tính".** Token được nhận diện bằng địa chỉ hợp đồng. Token chỉ được tính khi nguồn giá của bạn có niêm yết đúng hợp đồng đó; nếu không, nó được liệt kê là *Token không xác định · không tính* và không được cộng vào tổng. Nhờ vậy, một đồng "USDT" giả được airdrop vào ví sẽ không lọt vào tiền tiết kiệm của bạn.

**PnL đầu tư.** Bật trong **Sửa hũ** (ô **PnL đầu tư**). Từ đó, mỗi lần làm mới, ứng dụng ghi lại số lượng của từng tài sản và các khoản nạp, rút của các tài khoản môi giới của hũ, và màn hình có thêm phần **Đầu tư**:

- Năm dòng, **Toàn thời gian**, **Năm qua**, **Tháng qua**, **Tuần qua** và **Ngày qua**, mỗi dòng có lãi hoặc lỗ theo tiền tệ của hũ và tỷ suất sinh lời có trọng số theo thời gian tính bằng phần trăm, màu xanh khi dương và màu đỏ khi âm. Một dòng hiển thị *từ <ngày>* thay cho số khi lịch sử bắt đầu sau khi kỳ đó đã bắt đầu.
- Biểu đồ **Giá trị** của hũ và biểu đồ **Lãi và lỗ** của kết quả lũy kế, đều theo tiền tệ của hũ. Các nút **Tất cả**, **1Y**, **1M**, **1W** và **1D** đưa cả hai biểu đồ về khung thời gian đó; đường PnL bắt đầu lại từ 0 ở đầu khung.
- *Từ <ngày> · bằng <tiền tệ>* cho biết lịch sử bắt đầu từ khi nào. *Một số ngày thiếu tỷ giá; các giá trị đó dùng tỷ giá đã biết gần nhất* xuất hiện khi lịch sử tỷ giá có khoảng trống.

**Cách tính.** Lãi hoặc lỗ = chênh lệch giá trị trừ đi tiền nạp cộng với tiền rút. Tỷ lệ phần trăm có trọng số theo thời gian: lợi suất của từng ngày được nhân nối tiếp, nên thời điểm bạn nạp tiền không làm nó thay đổi. Tiền nạp và rút lấy từ nhà môi giới đối với các tài khoản đã liên kết ([nhà môi giới nào]({{ page.base }}/accounts#pnl)); với tài sản thủ công và ví, một thay đổi về số lượng được tính là nạp hoặc rút theo tỷ giá của ngày đó, không bao giờ là lãi. Giá trị trong quá khứ dùng lịch sử tỷ giá hằng ngày (**Cài đặt → Lịch sử tỷ giá**). Bật công tắc sẽ dựng lịch sử từ các bản lưu đã có trong thư mục của bạn và từ các nhà môi giới (*Đang dựng lịch sử PnL…*, rồi *Lịch sử PnL đã sẵn sàng: N ngày*); cho đến lần làm mới đầu tiên, phần này hiển thị *Chưa có lịch sử*.

**Chế độ danh mục đầu tư.** Khi bật, màn hình có thêm một bảng với giá trị, tỷ trọng thực tế, tỷ trọng mục tiêu và chênh lệch cho từng tài sản, cùng nút **Tái cân bằng** để nhập một số tiền và nhận danh sách những gì cần mua. Lệnh bán chỉ xuất hiện khi bật *Cho phép bán khi tái cân bằng*. Không có giao dịch nào được thực hiện.
