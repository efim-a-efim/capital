---
layout: default
lang: vi
base: "/vi"
key: "manual"
title: Hướng dẫn sử dụng
class: doc
---
# Hướng dẫn sử dụng

<p class="meta">Capital 2.2 · Android 8.0 trở lên</p>

## Ý tưởng

Bạn giữ tiền ở nhiều nơi: tài khoản tiết kiệm, tiền mặt, tài khoản chứng khoán, ví tiền mã hóa. Capital gọi mỗi nơi là một **hũ**. Bạn cần số tiền đó cho nhiều việc: quỹ khẩn cấp, một chuyến đi, một chiếc laptop. Capital gọi mỗi việc là một **mục tiêu**. Bạn liên kết hũ với mục tiêu, và ứng dụng tính xem mỗi mục tiêu đã có đủ bao nhiêu tiền từ những gì bạn đang có hôm nay. Hãy thêm các khoản bạn **dự định** tiết kiệm, ứng dụng sẽ cho biết thêm ngày mỗi mục tiêu hoàn thành.

Ứng dụng không di chuyển bất kỳ khoản tiền nào. Nó chỉ phản ánh những gì bạn sở hữu và tính xem số đó đáp ứng được những gì.

## Lần mở đầu tiên {#first-launch}

1. **Chọn thư mục.** Hãy chọn một thư mục riêng trên thiết bị, ví dụ `Documents/Capital`. Mọi bản ghi đều được ghi vào đây. Thư mục đã có dữ liệu Capital sẽ được mở ngay.
2. **Cài đặt → Tiền tệ mặc định.** Các tổng số và màn hình Tổng quan được hiển thị theo tiền tệ này.
3. Không bắt buộc: trong Cài đặt, nhập **khóa nhà cung cấp** cho những nhà cung cấp cho phép hạn mức cao hơn với khóa miễn phí (Alchemy, TronGrid, TON Center, CoinGecko). Mọi chuỗi và nguồn giá đều có lựa chọn mặc định không cần khóa. Tài khoản môi giới cần một **mã truy cập** từ nhà môi giới, nhập ở thẻ Môi giới; xem [Tài khoản môi giới và ngoại hối]({{ page.base }}/accounts).

## Hũ {#buckets}

Thẻ Hũ → **+**. Đặt tên và chọn tiền tệ cho hũ. Mở hũ để thêm tài sản:

- **Tài sản thủ công**: tên, mã tiền tệ hoặc mã tài sản (EUR, USD, BTC, mã cổ phiếu do bạn tự định giá…) và số lượng. Dùng cho số dư ngân hàng, tiền mặt, bất cứ thứ gì ứng dụng không tự đọc được.
- **Tài sản dạng ví**: chọn chuỗi (BTC, ETH, TON, TRX) và dán một địa chỉ công khai. Khi làm mới, ứng dụng đọc số dư gốc và, với ETH, TON và TRX, cả các token có thể thay thế trên địa chỉ đó.
- **Tài khoản môi giới**: chọn một trong các tài khoản đã thêm ở thẻ Môi giới. Khi làm mới, ứng dụng đọc tổng giá trị của tài khoản theo tiền tệ cơ sở của nó. Một tài khoản chỉ có thể được liên kết với một hũ.

Số tiền có thể dùng dấu chấm hoặc dấu phẩy thập phân, không dùng dấu phân tách nhóm. Mỗi hũ hiển thị số lượng theo đơn vị gốc và giá trị theo tiền tệ mặc định của bạn. Nếu thiếu giá, tổng số được đánh dấu là chưa đầy đủ; giá trị cũ đã lưu đệm vẫn được dùng kèm cảnh báo.

**Token.** Token được nhận diện bằng địa chỉ hợp đồng, không bao giờ bằng tên. Token chỉ được tính khi nguồn giá bạn chọn có niêm yết đúng hợp đồng đó; mọi token khác hiện là *Token không xác định · không tính* và không được cộng vào tổng. Mở trình chỉnh sửa của tài sản dạng ví để lấy danh sách token và tắt những token bạn không muốn.

**Chế độ danh mục đầu tư** (trong cài đặt hũ) coi hũ là một danh mục đầu tư: đặt tỷ trọng mục tiêu cho từng tài sản, xem tỷ trọng thực tế so với mục tiêu, và dùng **Tái cân bằng** để nhận danh sách những gì cần mua với một số tiền nhất định. Lệnh bán chỉ được gợi ý khi bật *Cho phép bán khi tái cân bằng*. Đây chỉ là công cụ tính; nó không thay đổi gì cả.

## Mục tiêu {#goals}

Thẻ Mục tiêu → **+**. Mục tiêu gồm tên, tiền tệ, số tiền mục tiêu và hạn hoàn thành. Mở mục tiêu và chọn **Liên kết hũ** để chỉ định những hũ nào được cấp tiền cho nó, có thể kèm giới hạn: số tiền cố định, phần trăm của hũ hoặc phần trăm của mục tiêu.

Cách phân bổ tiền:

- Mục tiêu có hạn sớm hơn được phân bổ trước. Các mục tiêu cùng ngày được phân bổ theo thứ tự hiển thị; kéo tay nắm để sắp xếp lại.
- Hũ liên kết với nhiều mục tiêu được chia giữa chúng theo các giới hạn, và không bao giờ bị tính hai lần.
- Kết quả hiển thị dưới dạng *đã có / mục tiêu* và *Còn thiếu*. Trên màn hình Tổng quan, bạn thấy tổng số, phần đã phân bổ cho mục tiêu và phần còn lại.

**Nhãn trạng thái.** *Đã đủ tiền* (xanh lá) khi tiền tiết kiệm hôm nay đã đáp ứng đủ mục tiêu. *Sẽ đủ tiền đúng hạn* (xanh lá) khi tiết kiệm dự kiến hoàn thành mục tiêu vào hoặc trước hạn. *Chưa đủ tiền* (vàng) trong các trường hợp còn lại. Dòng chữ bên dưới mục tiêu cho biết khi nào nó hoàn thành hoặc còn thiếu bao nhiêu.

**Lưu trữ** một mục tiêu để giữ lại mà không tính đến nó. Mục tiêu đã lưu trữ được liệt kê ở cuối danh sách.

## Kế hoạch {#plans}

Thẻ Kế hoạch → **+**. Khoản tiết kiệm dự kiến là số tiền bạn định thêm vào một ngày cụ thể, ví dụ phần lương để dành vào cuối mỗi tháng. Kế hoạch không thuộc tiền tiết kiệm của bạn; chúng chỉ kéo dài phần dự báo: "Các khoản tiết kiệm dự kiến sẽ hoàn thành mục tiêu này vào 30 thg 10, 2026 · đúng hạn".

Tiền từ kế hoạch được áp dụng sau các hũ hiện có, cho các mục tiêu theo thứ tự hạn hoàn thành, nên nó chỉ bù vào phần còn thiếu. Khi ngày của một kế hoạch đã qua, kế hoạch chuyển sang mục **Đã lưu trữ** và không còn được tính: hoặc bạn đã chuyển tiền vào một hũ và ứng dụng thấy nó ở đó, hoặc kế hoạch đã không thực hiện. Sửa ngày sang tương lai để kích hoạt lại; xóa nếu kế hoạch không còn cần.

## Môi giới

Thẻ Môi giới → **+**. Tài khoản môi giới là một kết nối chỉ đọc: chọn nhà môi giới (danh sách có tại [Tài khoản môi giới và ngoại hối]({{ page.base }}/accounts)), nhập mã tài khoản hoặc mã truy vấn rồi lưu; mã truy cập hoặc khóa của nhà môi giới chỉ cần nhập một lần trong mục **Thông tin xác thực** ở cùng thẻ và dùng được cho mọi tài khoản của nhà môi giới đó. Thẻ liệt kê từng tài khoản cùng giá trị gần nhất và hũ mà nó được liên kết. **Bỏ qua số dư nhỏ hơn** một số tiền theo tiền tệ mặc định của bạn (mặc định là 1) khiến số dư lẻ được tính là 0 trong hũ. Hãy liên kết tài khoản với một hũ qua **Thêm tài sản → Tài khoản môi giới**; xóa tài sản để hủy liên kết, xóa tài khoản để gỡ kết nối. Các bước thiết lập, kèm liên kết đến tài liệu của chính các nhà môi giới, nằm ở [Tài khoản môi giới và ngoại hối]({{ page.base }}/accounts).

## Làm mới

Biểu tượng làm mới ở trên cùng tải lại mọi số dư ví, giá trị tài khoản môi giới và giá. Có thể làm mới riêng từng hũ. Ứng dụng tự làm mới một lần khi được mở mới; khi quay lại từ chế độ nền, ứng dụng chỉ tải lại các tệp cục bộ. Làm mới cần có Internet; nếu không có, các giá trị trước đó được giữ lại và đánh dấu là cũ.

## Bảo mật {#security}

Cài đặt → Bảo mật.

- **Mã hóa** mã hóa mọi tệp trong thư mục, kể cả các phiên bản cũ, bằng mật khẩu. Tắt mã hóa sẽ giải mã chúng. **Không thể khôi phục mật khẩu**: mất mật khẩu nghĩa là dữ liệu không thể mở được nữa. Các bản sao lưu không mã hóa tạo ra trước khi bạn bật mã hóa vẫn đọc được; ứng dụng cảnh báo về chúng nhưng không thể xóa chúng.
- **PIN** và **sinh trắc học** khả dụng khi bật mã hóa. *Dùng mật khẩu* luôn có trên màn hình PIN. Sau 10 lần nhập sai PIN, PIN bị xóa và chỉ còn mật khẩu dùng được. Nhập sai không bao giờ xóa dữ liệu.
- Khi bật mã hóa, ảnh chụp màn hình và bản xem trước trong danh sách ứng dụng gần đây bị chặn.

## Đồng bộ, sao lưu, khôi phục {#sync-backup-recovery}

Capital ghi các tệp bản lưu có ID phiên bản và tổng kiểm vào thư mục của bạn và không bao giờ tự đồng bộ. Hãy đặt thư mục dưới sự quản lý của bất kỳ công cụ đồng bộ nào bạn đang dùng. Nếu hai thiết bị chỉnh sửa cùng lúc, ứng dụng hiển thị màn hình xung đột để bạn chọn một phiên bản; cả hai bản gốc vẫn còn trên đĩa.

- **Xuất bản sao lưu** (Cài đặt) ghi ra một tệp di động duy nhất. **Khôi phục** kiểm tra tính hợp lệ của tệp trước khi thay đổi bất cứ thứ gì.
- Nếu lưu thất bại, các chỉnh sửa của bạn được giữ trong bộ nhớ, kèm *Thử lưu lại* và *Lưu bản sao vào thư mục*.
- Nếu mất quyền truy cập thư mục, hãy kết nối lại đúng thư mục đó.
- Tệp do phiên bản ứng dụng mới hơn ghi sẽ bị phiên bản cũ hơn từ chối; hãy cập nhật ứng dụng.

## Ngôn ngữ {#language}

Ứng dụng khởi động bằng ngôn ngữ của thiết bị nếu đó là một trong 15 ngôn ngữ được hỗ trợ, nếu không thì bằng tiếng Anh. Đổi ngôn ngữ trong Cài đặt → Ngôn ngữ.

## Cài đặt ngoài Google Play

Tải APK từ [bản phát hành mới nhất]({{ site.repo }}/releases/latest) và mở nó; cho phép cài đặt từ nguồn đó khi Android hỏi. Mọi bản phát hành đều được ký bằng cùng một khóa, nên phiên bản mới cài đè lên phiên bản cũ và giữ nguyên cài đặt của bạn. Việc cập nhật hay gỡ cài đặt không bao giờ động đến thư mục chứa dữ liệu của bạn.
