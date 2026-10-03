---
layout: screen
lang: vi
base: "/vi"
key: "screens/brokers"
screen: brokers
title: Môi giới
---
# Môi giới

**Đây là gì.** Các kết nối chỉ đọc với tài khoản chứng khoán và ngoại hối của bạn. Một tài khoản môi giới gồm nhà môi giới, mã tài khoản hoặc mã truy vấn và thông tin xác thực của nhà môi giới; khi làm mới, ứng dụng đọc tổng giá trị của tài khoản theo tiền tệ cơ sở của nó. Các tài khoản nằm trên màn hình này, không nằm trong hũ: hũ chỉ liên kết với một tài khoản, và hũ vẫn là nơi tiền tiết kiệm của bạn được tính.

**Mỗi hàng hiển thị gì.** Tên tài khoản, nhà môi giới và mã, giá trị đọc gần nhất theo tiền tệ của tài khoản và theo tiền tệ mặc định của bạn, thời điểm quan sát và thời điểm lấy dữ liệu, cùng hũ mà nó được liên kết: **Đã liên kết với …** mở hũ đó, *Chưa liên kết với hũ nào* nghĩa là chưa có gì tính tài khoản này. **Sửa** thay đổi tên, nhà môi giới hoặc mã; **Xóa** gỡ tài khoản và, nếu nó đã được liên kết, cả tài sản liên kết với nó.

**Thêm tài khoản.** Nhấn **+**, nhập tên, chọn nhà môi giới và nhập mã mà nhà môi giới đó dùng: Flex Query id cho Interactive Brokers, mã tài khoản cho OANDA, số tài khoản cho Trading 212. Với SnapTrade, nhấn **Kết nối nhà môi giới qua SnapTrade**, quay lại, nhấn **Lấy tài khoản** và chọn một tài khoản. Lưu. Tiền tệ và giá trị xuất hiện sau lần làm mới tiếp theo.

**Bỏ qua số dư nhỏ hơn.** Đánh dấu ô này và nhập một số tiền theo tiền tệ mặc định của bạn (mặc định là 1) để loại bỏ các khoản lẻ khỏi tiền tiết kiệm: khi giá trị của tài khoản, quy đổi theo tỷ giá đã lưu, thấp hơn số tiền đó, tài sản liên kết được tính là 0 và hàng hiển thị *Tính là 0: dưới …*. Giá trị thực vẫn hiển thị trên màn hình này. Nếu không có tỷ giá cho tiền tệ của tài khoản thì không có gì bị bỏ qua.

**Liên kết vào một hũ.** Mở hũ, nhấn **Thêm tài sản**, đặt **Theo dõi** thành **Tài khoản môi giới** và chọn tài khoản; để trống tên để dùng tên của tài khoản. Một tài khoản chỉ nằm trong một hũ tại một thời điểm. **Sửa / chuyển** trên tài sản sẽ chuyển nó sang hũ khác; xóa tài sản sẽ hủy liên kết tài khoản mà không xóa nó.

**Thông tin xác thực.** Mã truy cập hoặc khóa của mỗi nhà môi giới được hỗ trợ (Interactive Brokers, OANDA, Trading 212, SnapTrade); một bộ cho mỗi nhà môi giới dùng được cho mọi tài khoản của nhà đó. Chúng được mã hóa bằng khóa lưu trong Android Keystore, không bao giờ được ghi vào thư mục dữ liệu, không nằm trong bản xuất và bản sao lưu của hệ thống, và chỉ được gửi đến nhà môi giới đã cấp chúng. **Hướng dẫn thiết lập tài khoản môi giới** mở [Tài khoản môi giới và ngoại hối]({{ page.base }}/accounts), nơi liệt kê các bước cho từng nhà môi giới.

**Làm mới.** Biểu tượng làm mới trên màn hình này đọc mọi tài khoản; nút làm mới trên một hũ chỉ đọc các tài khoản được liên kết vào hũ đó. Tài khoản không đọc được sẽ giữ giá trị cuối cùng và hiện thông báo của nhà môi giới dưới hàng của nó. Ứng dụng chỉ đọc: không bao giờ đặt lệnh hay chuyển tiền.
