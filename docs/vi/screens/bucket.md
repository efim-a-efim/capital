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
- **Tài khoản môi giới**: chọn nhà môi giới (Interactive Brokers, OANDA, Trading 212, SnapTrade) và nhập mã tài khoản hoặc mã truy vấn; với SnapTrade, **Lấy tài khoản** liệt kê các tài khoản đã kết nối để bạn chọn. Khi làm mới, ứng dụng đọc tổng giá trị của tài khoản theo tiền tệ cơ sở của tài khoản; mã truy cập được nhập trong Cài đặt → Tài khoản môi giới. **Hướng dẫn thiết lập tài khoản môi giới** mở [Tài khoản môi giới và ngoại hối]({{ page.base }}/accounts), nơi liệt kê các bước cho từng nhà môi giới.

**Token và "không tính".** Token được nhận diện bằng địa chỉ hợp đồng. Token chỉ được tính khi nguồn giá của bạn có niêm yết đúng hợp đồng đó; nếu không, nó được liệt kê là *Token không xác định · không tính* và không được cộng vào tổng. Nhờ vậy, một đồng "USDT" giả được airdrop vào ví sẽ không lọt vào tiền tiết kiệm của bạn.

**Chế độ danh mục đầu tư.** Khi bật, màn hình có thêm một bảng với giá trị, tỷ trọng thực tế, tỷ trọng mục tiêu và chênh lệch cho từng tài sản, cùng nút **Tái cân bằng** để nhập một số tiền và nhận danh sách những gì cần mua. Lệnh bán chỉ xuất hiện khi bật *Cho phép bán khi tái cân bằng*. Không có giao dịch nào được thực hiện.
