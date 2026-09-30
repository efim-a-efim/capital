---
layout: default
lang: vi
base: "/vi"
key: "data-safety"
title: Khai báo an toàn dữ liệu
class: doc
---
# Khai báo an toàn dữ liệu

<p class="meta">Câu trả lời cho biểu mẫu trong Google Play Console (Policy and programs → App content → Data safety (Chính sách và chương trình → Nội dung ứng dụng → An toàn dữ liệu)), kèm lập luận cho từng câu. Đã rà soát theo phiên bản 2.2.1 vào ngày 30 tháng 9 năm 2026. <a href="{{ page.base }}/privacy">Chính sách quyền riêng tư</a> trình bày cùng các sự thật này cho người dùng.</p>

## Cách ứng dụng xử lý dữ liệu

Capital không có backend. Mọi thứ người dùng nhập đều nằm trong một thư mục trên thiết bị. Dữ liệu duy nhất từng rời khỏi thiết bị là những gì ứng dụng gửi, theo lệnh của người dùng, đến các nhà cung cấp dữ liệu bên thứ ba mà người dùng chọn trong Cài đặt: địa chỉ ví công khai, mã định danh hợp đồng token, mã tiền tệ và khóa API người dùng đã nhập cho nhà cung cấp đó (nếu có). Nhà cung cấp trả lời yêu cầu; ứng dụng lưu số dư và giá nhận được trên thiết bị và không giữ bản sao nào của yêu cầu. Không SDK nào trong ứng dụng tự gửi dữ liệu về máy chủ: các thư viện phụ thuộc chỉ gồm AndroidX, Kotlin, OkHttp và Bouncy Castle.

Google Play coi dữ liệu là *được thu thập* khi nó được truyền ra khỏi thiết bị, kể cả khi không có máy chủ nào của nhà phát triển tham gia và việc xử lý chỉ là tạm thời, nên phần khai báo không phải là "không thu thập gì". Đó là một loại dữ liệu duy nhất, được xử lý tạm thời và không bắt buộc.

## Câu trả lời cho biểu mẫu

### Tổng quan

| Câu hỏi | Câu trả lời |
|---|---|
| Does your app collect or share any of the required user data types? (Ứng dụng của bạn có thu thập hoặc chia sẻ loại dữ liệu người dùng bắt buộc nào không?) | **Yes** (Có) |
| Is all of the user data collected by your app encrypted in transit? (Mọi dữ liệu người dùng mà ứng dụng thu thập có được mã hóa khi truyền không?) | **Yes** (Có) — chỉ dùng HTTPS; lưu lượng không mã hóa bị tắt trong manifest |
| Do you provide a way for users to request that their data is deleted? (Bạn có cung cấp cách để người dùng yêu cầu xóa dữ liệu của họ không?) | **Yes** (Có) — không có gì được giữ lại sau khi yêu cầu hoàn tất, điều này đáp ứng quy tắc "xóa trong vòng 90 ngày kể từ khi thu thập" để được gắn huy hiệu. Người dùng xóa dữ liệu trên thiết bị bằng cách xóa thư mục và gỡ cài đặt ứng dụng; xem Chính sách quyền riêng tư. |

### Loại dữ liệu

Chọn đúng một loại.

| Danh mục | Loại dữ liệu | Thu thập | Chia sẻ | Tạm thời | Bắt buộc hay không bắt buộc | Mục đích |
|---|---|---|---|---|---|---|
| Financial info (Thông tin tài chính) | Other financial info (Thông tin tài chính khác) | Yes (Có) | No (Không) | **Yes** (Có) | **Optional** (Không bắt buộc) | App functionality (Chức năng của ứng dụng) |

Phạm vi của loại này: các địa chỉ blockchain công khai mà người dùng theo dõi, các hợp đồng token tìm thấy trên đó và mã tiền tệ của các tài sản của người dùng. Chúng được truyền đến nhà cung cấp dữ liệu do người dùng chọn để lấy số dư và giá, được giữ trong bộ nhớ trong lúc xử lý yêu cầu, rồi bị loại bỏ.

Vì sao **không chia sẻ**: dữ liệu được truyền thẳng từ thiết bị đến nhà cung cấp mà người dùng đã chọn, trong lần làm mới do người dùng khởi động, sau khi ứng dụng đã cho người dùng biết trong Cài đặt nhà cung cấp nào sẽ được truy vấn và rằng yêu cầu sẽ tiết lộ địa chỉ và IP cho nhà cung cấp đó. Đây là trường hợp miễn trừ "hành động do người dùng khởi xướng, khi người dùng có thể dự đoán hợp lý rằng dữ liệu sẽ được chia sẻ". Nhà phát triển không nhận được gì và không có nhà cung cấp dịch vụ nào.

Vì sao **không bắt buộc**: ứng dụng dùng được đầy đủ chỉ với tài sản nhập thủ công. Địa chỉ và khóa API là do người dùng tự chọn nhập.

Khóa API người dùng nhập chỉ được gửi đến nhà cung cấp đã cấp khóa đó. Chúng là thông tin xác thực của người dùng cho chính dịch vụ của nhà cung cấp đó và không được khai báo là một loại dữ liệu người dùng riêng; nếu người đánh giá hỏi, hãy mô tả chúng như trên.

### Các loại **không** thu thập

Mọi danh mục khác đều là "No" (Không): không có vị trí, không có thông tin cá nhân, không có danh bạ, không có tin nhắn, không có ảnh hay video, không có tệp và tài liệu, không có hoạt động trong ứng dụng, không có lịch sử duyệt web, không có thông tin và hiệu suất ứng dụng (không nhật ký sự cố, không dữ liệu chẩn đoán), không có mã nhận dạng thiết bị hay mã nhận dạng khác. Địa chỉ IP đến được nhà cung cấp như một phần của mọi yêu cầu HTTPS và không được ứng dụng dùng vào bất kỳ mục đích nào.

Hồ sơ tài chính của người dùng (tài sản, mục tiêu, kế hoạch) chỉ được xử lý trên thiết bị và nằm ngoài phạm vi của biểu mẫu.

### Biện pháp bảo mật

| Mục | Câu trả lời |
|---|---|
| Independent security review (MASA) (Đánh giá bảo mật độc lập) | No (Không) |
| Committed to follow the Families policy (Cam kết tuân thủ chính sách Gia đình) | No (Không) (không phải ứng dụng dành cho trẻ em) |

## Các khai báo liên quan trên trang App content (Nội dung ứng dụng)

| Khai báo | Câu trả lời |
|---|---|
| Privacy policy URL (URL chính sách quyền riêng tư) | `{{ site.url }}/privacy` |
| Ads (Quảng cáo) | Không, ứng dụng không chứa quảng cáo |
| App access (Quyền truy cập ứng dụng) | Mọi chức năng đều dùng được mà không cần quyền truy cập đặc biệt. Không cần đăng nhập. Khóa API nhà cung cấp là không bắt buộc; mọi nhà cung cấp đều có lựa chọn mặc định không cần khóa. |
| Content rating (IARC) (Phân loại nội dung) | Bảng câu hỏi Utility / productivity (Tiện ích / năng suất); không có bạo lực, nội dung tình dục, cờ bạc, chất bị kiểm soát, tương tác giữa người dùng hay chia sẻ vị trí. Kết quả dự kiến: Everyone / PEGI 3. |
| Target audience and content (Đối tượng mục tiêu và nội dung) | Từ 18 tuổi trở lên (công cụ tài chính cá nhân; không thiết kế cho trẻ em) |
| News app (Ứng dụng tin tức) | Không |
| COVID-19 contact tracing and status (Truy vết tiếp xúc và tình trạng COVID-19) | Không |
| Data safety (An toàn dữ liệu) | Như trên |
| Government app (Ứng dụng của chính phủ) | Không |
| Financial features (Tính năng tài chính) | Xem [Khai báo tính năng tài chính]({{ page.base }}/financial-features) |
| Health apps (Ứng dụng sức khỏe) | Không có tính năng sức khỏe |

## Cần cập nhật gì khi ứng dụng thay đổi

Hãy kiểm tra lại trang này khi một bản phát hành bổ sung tính năng phân tích dữ liệu, báo cáo sự cố, tài khoản, máy chủ do nhà phát triển vận hành, SDK mới có truy cập mạng, hoặc chia sẻ dữ liệu trên thiết bị với ứng dụng khác. Bất kỳ thay đổi nào trong số đó cũng làm thay đổi biểu mẫu.
