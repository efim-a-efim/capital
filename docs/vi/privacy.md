---
layout: default
lang: vi
base: "/vi"
key: "privacy"
title: Chính sách quyền riêng tư
class: doc
---
# Chính sách quyền riêng tư

<p class="meta">Capital cho Android (gói <code>dev.capital</code>) · Nhà phát triển: {{ site.developer }} · Có hiệu lực từ ngày 30 tháng 9 năm 2026</p>

## Tóm tắt

- Capital không có tài khoản người dùng, không phân tích dữ liệu, không quảng cáo, không báo cáo sự cố và không có máy chủ nào do nhà phát triển vận hành. Nhà phát triển không bao giờ nhận được dữ liệu của bạn.
- Hồ sơ tài chính của bạn chỉ được lưu trên thiết bị, trong thư mục do bạn chọn. Bạn có thể mã hóa chúng bằng mật khẩu.
- Lưu lượng mạng duy nhất là các yêu cầu mà ứng dụng gửi, theo lệnh của bạn, đến các nhà cung cấp dữ liệu giá và blockchain mà bạn chọn trong Cài đặt và đến các nhà môi giới có tài khoản bạn kết nối. Các yêu cầu đó chứa địa chỉ ví công khai, hợp đồng token và mã tiền tệ bạn theo dõi, mọi khóa API bạn đã nhập cho nhà cung cấp đó, và với tài khoản môi giới là mã truy cập bạn đã tạo cùng mã tài khoản hoặc mã truy vấn.

## Ứng dụng lưu gì trên thiết bị của bạn

**Trong thư mục bạn chọn.** Hũ, tài sản, địa chỉ ví, mục tiêu, liên kết, tiết kiệm dự kiến, giá đã lưu đệm và các cài đặt gắn với dữ liệu đó. Các tệp ở dạng văn bản thuần trừ khi bạn bật mã hóa (Cài đặt → Bảo mật). Khi bật mã hóa, mọi tệp được mã hóa bằng AES-256-GCM với khóa dẫn xuất từ mật khẩu của bạn bằng Argon2id. Không thể khôi phục mật khẩu.

**Trong bộ nhớ riêng của ứng dụng** (các ứng dụng khác không truy cập được):

| Mục | Mục đích |
|---|---|
| Quyền truy cập thư mục đã chọn | Mở lại thư mục ở lần khởi động tiếp theo |
| Khóa API nhà cung cấp và mã truy cập nhà môi giới mà bạn đã nhập | Chỉ được gửi đến nhà cung cấp hoặc nhà môi giới đã cấp chúng; được mã hóa bằng khóa lưu trong Android Keystore; không nằm trong bản lưu, bản xuất và bản sao lưu của hệ điều hành |
| Cài đặt khóa ứng dụng | Mở khóa thư mục đã mã hóa mà không cần mật khẩu: một bản sao của khóa dữ liệu, được mã hóa bằng khóa dẫn xuất từ PIN của bạn và gắn với Android Keystore. Bản thân PIN không được lưu |
| Lựa chọn ngôn ngữ và giao diện | Tùy chọn giao diện |

Tính năng sao lưu Android và chuyển dữ liệu giữa các thiết bị bị tắt đối với ứng dụng, nên hệ thống không sao chép bất kỳ mục nào ở trên lên Google hay sang thiết bị khác.

## Những gì rời khỏi thiết bị của bạn

Capital chỉ liên hệ với các nhà cung cấp bạn chọn trong Cài đặt, chỉ qua HTTPS, và chỉ khi bạn làm mới hoặc kiểm tra nguồn. Mỗi yêu cầu được trả lời rồi bị loại bỏ; ứng dụng lưu số dư và giá nhận được vào thư mục của bạn, không lưu yêu cầu.

| Dữ liệu được gửi | Gửi cho ai | Lý do |
|---|---|---|
| Địa chỉ ví công khai bạn đã thêm | Nhà cung cấp dữ liệu blockchain được chọn cho chuỗi đó | Đọc số dư và các token mà địa chỉ đang nắm giữ |
| Địa chỉ hợp đồng token và mã định danh tài sản | Nhà cung cấp giá tiền mã hóa bạn đã chọn | Định giá tài sản |
| Mã tiền tệ | Nhà cung cấp tỷ giá tiền pháp định bạn đã chọn | Quy đổi giữa các loại tiền tệ |
| Khóa API bạn đã nhập cho một nhà cung cấp | Chỉ nhà cung cấp đó | Xác thực tài khoản của chính bạn với họ |
| Mã truy cập hoặc khóa API và mã tài khoản hoặc mã truy vấn của một tài khoản môi giới | Chỉ nhà môi giới đó (Interactive Brokers, OANDA, Trading 212, SnapTrade) | Đọc tổng giá trị của tài khoản |

Như với mọi yêu cầu qua Internet, mỗi nhà cung cấp cũng thấy địa chỉ IP của bạn. Các nhà cung cấp độc lập với nhà phát triển và xử lý yêu cầu theo điều khoản và chính sách quyền riêng tư của riêng họ; các liên kết đến đó có trong ứng dụng tại Cài đặt → Nguồn / ghi công:

| Dữ liệu | Nhà cung cấp |
|---|---|
| Bitcoin | [Blockstream](https://blockstream.info), [mempool.space](https://mempool.space) |
| Ethereum và token ERC-20 | [PublicNode](https://publicnode.com), [Alchemy](https://www.alchemy.com), [Blockscout](https://www.blockscout.com), [Ethplorer](https://ethplorer.io) |
| TON và jetton | [TON Center](https://toncenter.com), [TonAPI](https://tonapi.io) |
| TRON và token TRC-20 | [TronGrid](https://www.trongrid.io), [PublicNode](https://publicnode.com) |
| Giá tiền mã hóa | [DefiLlama](https://defillama.com), [CoinGecko](https://www.coingecko.com), [CoinPaprika](https://coinpaprika.com) |
| Tỷ giá tiền pháp định | [Frankfurter](https://frankfurter.dev), [Ngân hàng Trung ương châu Âu](https://www.ecb.europa.eu) |
| Tài khoản môi giới | [Interactive Brokers](https://www.interactivebrokers.com), [OANDA](https://www.oanda.com), [Trading 212](https://www.trading212.com), [SnapTrade](https://snaptrade.com) |

Không có gì được gửi đi nơi khác. Không có dữ liệu nào bị bán, chia sẻ cho mục đích quảng cáo hay dùng để lập hồ sơ người dùng. Các truy vấn blockchain công khai tiết lộ rằng ai đó ở địa chỉ IP của bạn quan tâm đến địa chỉ bạn đang theo dõi; hãy dùng VPN nếu điều đó quan trọng với bạn.

## Những điều ứng dụng không bao giờ làm

- Ứng dụng không bao giờ yêu cầu, lưu trữ hay truyền đi khóa riêng tư hoặc cụm từ khôi phục. Ứng dụng không thể ký hay gửi giao dịch.
- Ứng dụng không bao giờ chuyển tiền. Phân bổ cho mục tiêu chỉ là phép tính hiển thị cho bạn, không hơn.
- Ứng dụng không bao giờ gửi lệnh hay chỉ thị nào đến nhà môi giới. Quyền truy cập nhà môi giới chỉ dùng để đọc giá trị tài khoản; xem [Tài khoản môi giới và ngoại hối]({{ page.base }}/accounts).
- Ứng dụng không bao giờ liên hệ với nhà phát triển. Không có dữ liệu đo từ xa, không kiểm tra cập nhật trong ứng dụng, không có thông báo đẩy.

## Quyền

| Quyền | Công dụng |
|---|---|
| Internet | Gửi yêu cầu đến các nhà cung cấp được liệt kê ở trên |
| Truy cập thư mục | Do bạn cấp qua trình chọn thư mục của Android cho thư mục bạn chọn; ứng dụng không thể đọc các thư mục khác |
| Sinh trắc học | Mở khóa bằng vân tay hoặc khuôn mặt qua hộp thoại của chính Android; ứng dụng chỉ nhận kết quả thành công hay thất bại, không bao giờ nhận dữ liệu sinh trắc học |

## Đồng bộ và sao lưu

Capital không tự đồng bộ bất cứ thứ gì. Nếu bạn đặt thư mục dưới một công cụ đồng bộ (Syncthing, Nextcloud, Google Drive, …), điều khoản quyền riêng tư của công cụ đó áp dụng cho các bản sao mà nó tạo ra. Các tệp ở dạng văn bản thuần trừ khi bật mã hóa; những bản sao không mã hóa tạo ra trước khi bạn bật mã hóa vẫn đọc được bởi bất kỳ ai nắm giữ chúng.

**Xuất bản sao lưu** trong Cài đặt ghi ra một tệp duy nhất vào vị trí bạn chọn. Tệp này chứa cùng các bản ghi và chỉ an toàn ngang với vị trí lưu nó.

## Xóa dữ liệu của bạn

Xóa thư mục bạn đã chọn (và mọi bản sao do công cụ đồng bộ tạo ra) rồi gỡ cài đặt ứng dụng. Gỡ cài đặt sẽ xóa bộ nhớ riêng của ứng dụng, bao gồm khóa nhà cung cấp và cài đặt khóa ứng dụng. Nhà phát triển không nắm giữ gì để xóa và không thể thay bạn xóa bất cứ thứ gì. Các nhà cung cấp bạn đã truy vấn có thể lưu nhật ký yêu cầu theo quy định lưu giữ của riêng họ.

## Ủng hộ {#donations}

Ứng dụng (nút hình trái tim trên màn hình Tổng quan) và trang web này hiển thị địa chỉ ví của nhà phát triển để nhận các khoản ủng hộ tự nguyện. Khoản ủng hộ là giao dịch chuyển tiền mà bạn tự thực hiện từ ví của mình đến một trong các địa chỉ đó, theo điều khoản của ví bạn dùng và của mạng lưới bạn sử dụng. Capital không tham gia vào việc này: ứng dụng không xử lý bất kỳ khoản thanh toán nào, không thể biết bạn đã gửi gì hay chưa, không ghi lại bất cứ điều gì về giao dịch đó và không thay đổi gì cả — không tính năng nào được mở khóa hay thay đổi. Mục đích duy nhất của khoản ủng hộ là hỗ trợ nhà phát triển. Như mọi giao dịch blockchain, việc gửi tiền đến một địa chỉ công khai sẽ làm lộ địa chỉ gửi của bạn trên mạng lưới đó.

## Trẻ em

Capital là công cụ tài chính cá nhân dành cho người lớn. Ứng dụng không hướng đến trẻ em dưới 13 tuổi và không cố ý thu thập dữ liệu nào từ các em.

## Thay đổi chính sách này

Phiên bản hiện hành luôn có tại [{{ site.url }}{{ page.base }}/privacy]({{ page.base }}/privacy). Những thay đổi quan trọng được nêu trong ghi chú phát hành của phiên bản đưa ra thay đổi đó.

## Liên hệ

{{ site.developer }} · [{{ site.contact }}](mailto:{{ site.contact }}) · [Trình theo dõi sự cố]({{ site.repo }}/issues)
