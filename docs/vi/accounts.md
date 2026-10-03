---
layout: default
lang: vi
base: "/vi"
key: "accounts"
title: Tài khoản môi giới và ngoại hối
class: doc
---
# Tài khoản môi giới và ngoại hối

Capital có thể đọc tổng giá trị của một tài khoản chứng khoán hoặc ngoại hối giống như cách đọc ví tiền mã hóa. Bạn thêm tài khoản vào một hũ dưới dạng tài sản loại **Tài khoản môi giới**, và mỗi lần làm mới, ứng dụng lấy giá trị tài sản ròng (net asset value) của tài khoản theo tiền tệ cơ sở của nó. Ứng dụng chỉ đọc: nó dùng giao diện báo cáo của nhà môi giới với mã truy cập do chính bạn tạo, không bao giờ đặt, sửa hay hủy lệnh và không bao giờ chuyển tiền.

Capital chỉ kết nối với các giao diện có thông tin xác thực dài hạn: một mã truy cập hoặc khóa bạn tạo một lần và có hiệu lực cho đến khi bạn thu hồi (hoặc, với Interactive Brokers, đến hạn bạn đã chọn, tối đa một năm). Hiện được hỗ trợ:

| Nhà môi giới | Giao diện sử dụng | Dữ liệu được đọc |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (chỉ để lấy báo cáo) | Giá trị tài sản ròng của ngày làm việc gần nhất, theo tiền tệ cơ sở của tài khoản |
| [OANDA](#oanda) | v20 REST API, tài khoản thực fxTrade | Giá trị tài sản ròng tại thời điểm làm mới, theo tiền tệ của tài khoản |
| [Trading 212](#trading-212) | Public API, tài khoản Invest và Stocks ISA | Tổng giá trị tài khoản tại thời điểm làm mới, theo tiền tệ chính của tài khoản |
| [SnapTrade](#snaptrade) | SnapTrade Personal, một đơn vị tổng hợp (aggregator) bao phủ nhiều nhà môi giới | Tổng giá trị tài khoản theo báo cáo của nhà môi giới gửi SnapTrade, theo tiền tệ của tài khoản |

## Trước khi bắt đầu {#before-you-start}

- **Những gì rời khỏi thiết bị.** Mỗi lần làm mới, ứng dụng gửi mã truy cập và mã tài khoản hoặc mã truy vấn của bạn đến nhà môi giới đó qua HTTPS. Nhà môi giới thấy địa chỉ IP của bạn, như với mọi yêu cầu khác.
- **Nơi lưu thông tin xác thực.** Thẻ Môi giới → Thông tin xác thực. Chúng được mã hóa bằng khóa lưu trong Android Keystore, không bao giờ được ghi vào thư mục dữ liệu của bạn và không nằm trong bản xuất hay bản sao lưu của hệ thống. Một bộ thông tin xác thực cho mỗi nhà môi giới dùng được cho mọi tài khoản bạn thêm ở nhà môi giới đó.
- **Những gì được lưu trong thư mục của bạn.** Mã tài khoản, giá trị đọc được gần nhất và thời điểm đọc. Không có gì khác từ nhà môi giới.
- **Giao diện của nhà môi giới có thể thay đổi.** Các bước dưới đây khớp với trang web của các nhà môi giới tính đến tháng 10 năm 2026. Nhà môi giới thỉnh thoảng đổi tên menu và dời các thiết lập, nên khi bạn làm theo, một bước có thể trông hơi khác. Tài liệu của chính nhà môi giới, được liên kết trong từng phần, là nguồn chính thức: nếu một bước ở đây không còn khớp, hãy tìm cùng thuật ngữ đó trên trang của nhà môi giới.

## Interactive Brokers {#interactive-brokers}

Capital dùng **Flex Web Service**, giao diện của Interactive Brokers để lấy các báo cáo đã được cấu hình sẵn. Mã truy cập mà nó dùng chỉ có thể tạo và tải báo cáo; không thể đăng nhập, giao dịch hay rút tiền. Capital yêu cầu phần **Net Asset Value (NAV) Summary in Base** của một Activity Flex Query và lấy tổng của ngày báo cáo mới nhất, nên giá trị là giá đóng cửa của ngày làm việc gần nhất.

### 1. Tạo Flex Query

1. Đăng nhập [Client Portal](https://www.interactivebrokers.com/portal) và mở **Performance & Reports → Flex Queries** (Hiệu suất và báo cáo → Flex Queries; ở một số tài khoản menu này có tên *Reporting*).
2. Trong **Activity Flex Query**, nhấn **+** (Create, tạo mới). Đặt tên cho truy vấn, ví dụ `Capital`.
3. Trong danh sách **Sections** (các phần), bật đúng hai phần và các trường sau (chọn tất cả các trường của một phần cũng được):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. Trong **Delivery Configuration** (cấu hình phân phối), đặt **Format** thành `XML` và **Period** thành `Last Business Day`. Các tùy chọn khác có thể giữ mặc định.
5. Lưu truy vấn, rồi nhấn biểu tượng **i** (thông tin) bên cạnh nó và ghi lại **Query ID**, một con số.

Truy vấn phải bao gồm đúng một tài khoản. Nếu bạn có các tài khoản liên kết hoặc cấu trúc cố vấn, hãy tạo mỗi tài khoản một truy vấn và chỉ chọn tài khoản đó khi tạo.

### 2. Bật Flex Web Service và tạo mã truy cập

1. Trên cùng trang **Flex Queries**, mở **Flex Web Service Configuration**.
2. Bật **Flex Web Service Status** và lưu. Một mã truy cập được tạo ra.
3. Để chọn thời hạn hiệu lực của mã, nhấn **Generate New Token**: từ 6 giờ đến 1 năm. Hãy để trống **Valid for IP address** nếu dùng điện thoại, vì địa chỉ của nó thay đổi. Tạo mã mới sẽ vô hiệu hóa mã trước đó.
4. Sao chép mã truy cập.

### 3. Kết nối trong Capital

1. **Môi giới → Thông tin xác thực → Mã truy cập: Interactive Brokers**, dán mã và lưu.
2. **Môi giới → +**: nhập tên, đặt **Nhà môi giới** thành Interactive Brokers, nhập **Flex Query id** và lưu.
3. Mở hũ, **Thêm tài sản**, đặt **Theo dõi** thành **Tài khoản môi giới**, chọn tài khoản và lưu.
4. Nhấn **Làm mới**. Lần chạy đầu tiên mất tới nửa phút vì báo cáo được tạo theo yêu cầu.

Khi mã truy cập hết hạn, lần làm mới báo *Mã truy cập đã hết hạn; hãy tạo mã mới trong Client Portal*: hãy tạo mã mới và dán vào mục Thông tin xác thực. Interactive Brokers cho phép mỗi giây một yêu cầu báo cáo và mười yêu cầu mỗi phút cho một mã truy cập, mức mà một lần làm mới không bao giờ vượt quá.

Tài liệu của Interactive Brokers: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital gọi **account summary** (tóm tắt tài khoản) của OANDA v20 REST API và lưu NAV của tài khoản (số dư cộng lãi hoặc lỗ chưa thực hiện) theo tiền tệ của tài khoản. Chỉ hỗ trợ tài khoản thực **fxTrade**; tài khoản thực hành không phải là tiền tiết kiệm.

**Mã truy cập cá nhân của OANDA không phải là chỉ đọc.** Nó cấp toàn quyền truy cập API cho mọi tài khoản con của lần đăng nhập của bạn, kể cả giao dịch. Capital chỉ gọi account summary, nhưng bất kỳ ai có được mã đều có thể dùng nó để giao dịch. Hãy coi nó như mật khẩu: chỉ dán vào Capital và thu hồi nó trong cổng thông tin OANDA nếu bạn mất điện thoại.

### 1. Tạo mã truy cập

1. Đăng nhập cổng quản lý tài khoản OANDA fxTrade của bạn.
2. Mở **My Services → Manage API Access** (Dịch vụ của tôi → Quản lý quyền truy cập API; trên cổng cũ: *My Account → My Services → Manage API Access*).
3. Chấp nhận giấy phép API và nhấn **Generate**. Sao chép mã; OANDA không hiển thị lại. Nếu bạn làm mất, hãy thu hồi nó tại đó và tạo mã mới.

### 2. Tìm mã tài khoản

Mã tài khoản v20 có dạng `001-001-1234567-001`, có dấu gạch nối. Nó được liệt kê trong cùng cổng thông tin, bên cạnh từng tài khoản con, và trên nền tảng fxTrade trong phần chi tiết tài khoản.

### 3. Kết nối trong Capital

1. **Môi giới → Thông tin xác thực → Mã truy cập: OANDA**, dán mã và lưu.
2. **Môi giới → +**: nhập tên, đặt **Nhà môi giới** thành OANDA, nhập **Mã tài khoản OANDA** và lưu.
3. Mở hũ, **Thêm tài sản**, đặt **Theo dõi** thành **Tài khoản môi giới**, chọn tài khoản và lưu.
4. Nhấn **Làm mới**.

Tài khoản ký quỹ có NAV âm được báo là lỗi chứ không được tính vào tiền tiết kiệm.

Tài liệu của OANDA: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital gọi **account summary** (tóm tắt tài khoản) của Trading 212 Public API và lưu tổng giá trị tài khoản theo tiền tệ chính của tài khoản. API này bao gồm các tài khoản **Invest** và **Stocks ISA**; một cặp khóa thuộc về một tài khoản, và Capital giữ một cặp khóa, nên chỉ đọc được một tài khoản Trading 212.

### 1. Tạo khóa API

1. Trong ứng dụng hoặc trang web Trading 212, mở menu (**☰**) → **Settings** → **API (Beta)** và chấp nhận cảnh báo rủi ro.
2. Nhấn **Generate API key**. Đặt tên cho khóa, chỉ giữ quyền **Account data** (đọc) và chọn quyền truy cập IP *Unrestricted* (không giới hạn; địa chỉ của điện thoại thay đổi).
3. Gửi đi. Sao chép cả hai giá trị: **API Key** và **API Secret Key**. Mã bí mật chỉ hiển thị một lần; nếu bạn làm mất, hãy xóa khóa và tạo một cặp mới.

### 2. Kết nối trong Capital

1. **Môi giới → Thông tin xác thực → Khóa API: Trading 212** và **Mã bí mật API: Trading 212**, dán từng giá trị.
2. **Môi giới → +**: nhập tên, đặt **Nhà môi giới** thành Trading 212, nhập **Số tài khoản Trading 212** (mã tài khoản hiển thị trong ứng dụng, chỉ gồm chữ số) và lưu.
3. Mở hũ, **Thêm tài sản**, đặt **Theo dõi** thành **Tài khoản môi giới**, chọn tài khoản và lưu.
4. Nhấn **Làm mới**. Trading 212 cho phép một yêu cầu tóm tắt mỗi 5 giây.

Tài liệu của Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) là một đơn vị tổng hợp (aggregator): bạn kết nối tài khoản môi giới với SnapTrade một lần, rồi SnapTrade đọc nó thay bạn. Nó bao phủ nhiều nhà môi giới không có API công khai riêng. Capital dùng **SnapTrade Personal**, gói miễn phí cho tài khoản của chính bạn, với client id và consumer key của riêng bạn. Dữ liệu trên gói này được SnapTrade làm mới khoảng một lần mỗi ngày.

Những gì được gửi: client id của bạn và, dưới dạng chữ ký, không có gì của chính consumer key (các yêu cầu được ký bằng nó). SnapTrade, không phải Capital, giữ kết nối với nhà môi giới của bạn; điều khoản và chính sách quyền riêng tư của SnapTrade áp dụng cho kết nối đó.

### 1. Tạo khóa API

1. Đăng ký tại [bảng điều khiển SnapTrade](https://dashboard.snaptrade.com/signup) và chọn gói **Personal**.
2. Trong bảng điều khiển, tạo một khóa API. Sao chép **client id** và **consumer key**; consumer key chỉ hiển thị một lần.

### 2. Kết nối trong Capital

1. **Môi giới → Thông tin xác thực → Client id: SnapTrade** và **Consumer key: SnapTrade**, dán từng giá trị.
2. **Môi giới → +**: nhập tên và đặt **Nhà môi giới** thành SnapTrade.
3. Nhấn **Kết nối nhà môi giới qua SnapTrade**. SnapTrade Connection Portal mở trong trình duyệt; hãy đăng nhập nhà môi giới của bạn tại đó (liên kết có hiệu lực trong 5 phút). Sau đó quay lại Capital.
4. Nhấn **Lấy tài khoản** và chọn tài khoản; mã của nó điền vào trường **Mã tài khoản SnapTrade**. Lưu.
5. Mở hũ, **Thêm tài sản**, đặt **Theo dõi** thành **Tài khoản môi giới**, chọn tài khoản và lưu, rồi **Làm mới**.

Tài khoản mà SnapTrade chưa đồng bộ xong sẽ báo *SnapTrade chưa có tổng giá trị cho tài khoản này*; hãy làm mới lại sau.

Tài liệu của SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Các nhà môi giới khác {#other-brokers}

Capital chỉ kết nối với các giao diện hoạt động được từ điện thoại qua HTTPS, với mã truy cập bạn có thể tự tạo và chỉ cho phép đọc mà không thể giao dịch. Vì vậy hiện tại không hỗ trợ:

- Tài khoản **MetaTrader 4 và 5**. Mật khẩu nhà đầu tư (investor password) cho quyền chỉ đọc, nhưng chỉ bên trong ứng dụng MetaTrader; các nhà môi giới không công bố giao diện HTTPS nào cho nó.
- Các nhà môi giới có API cần một chương trình chạy trên máy tính (ví dụ cổng Client Portal Web API của Interactive Brokers; Capital dùng Flex Web Service thay thế) hoặc cần đăng ký ứng dụng OAuth.
- Các nhà môi giới có API chỉ cấp mã truy cập ngắn hạn qua OAuth, ví dụ Saxo Bank (mã truy cập có hiệu lực 20 phút; mã 24 giờ của cổng nhà phát triển chỉ dùng được cho môi trường mô phỏng).
- Ngân hàng và nhà môi giới không có API công khai.

Nhiều nhà môi giới trong số này được [SnapTrade](#snaptrade) bao phủ. Nếu không, hãy nhập số dư dưới dạng tài sản **Thủ công** và cập nhật con số khi bạn kiểm tra sao kê. Nếu nhà môi giới của bạn có điểm cuối HTTPS đơn giản dùng mã truy cập để đọc giá trị tài khoản, hãy [mở một issue]({{ site.repo }}/issues) kèm liên kết đến tài liệu của nó. Mỗi nhà môi giới trong Capital là một plugin nhỏ; nhà phát triển có thể thêm plugin mới theo [hướng dẫn viết plugin]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## Thông báo và cách xử lý {#messages}

| Thông báo | Cách xử lý |
|---|---|
| *Interactive Brokers cần thông tin xác thực trên màn hình Môi giới* / *OANDA cần thông tin xác thực …* | Dán mã truy cập, khóa hoặc client id trong mục Thông tin xác thực ở thẻ Môi giới. |
| *Mã truy cập đã hết hạn; hãy tạo mã mới trong Client Portal* | Tạo mã Flex Web Service mới và dán vào. |
| *Mã truy cập không hợp lệ* | Sao chép lại mã; mã mới thay thế mã cũ. |
| *Mã truy cập bị giới hạn cho một địa chỉ IP khác* | Tạo mã không giới hạn IP. |
| *Flex Query id không hợp lệ* | Kiểm tra lại con số; truy vấn phải là một Activity Flex Query của lần đăng nhập này. |
| *Hãy thêm phần Net Asset Value (NAV) Summary in Base với Report Date và Total vào Flex Query* | Sửa truy vấn và thêm phần cùng các trường đó. |
| *Hãy thêm trường Currency của Account Information vào Flex Query* | Sửa truy vấn và thêm trường đó. |
| *Truy vấn trả về N tài khoản; hãy tạo mỗi tài khoản một Flex Query* | Tạo một truy vấn chỉ bao gồm một tài khoản. |
| *Báo cáo chưa sẵn sàng; hãy làm mới lại sau một phút* | Interactive Brokers vẫn đang tạo báo cáo; hãy làm mới lại. |
| *Bị từ chối truy cập; hãy kiểm tra khóa hoặc hạn mức của nhà cung cấp* | Mã truy cập OANDA sai hoặc bị thu hồi, hoặc cặp khóa Trading 212 sai hoặc thiếu quyền Account data. |
| *SnapTrade chưa có tổng giá trị cho tài khoản này; hãy đồng bộ kết nối rồi thử lại* | SnapTrade chưa đồng bộ nhà môi giới; hãy làm mới lại sau. |
| *Chưa có tài khoản nào được kết nối. Hãy kết nối một nhà môi giới qua SnapTrade trước.* | Mở Connection Portal từ trình chỉnh sửa và kết nối một nhà môi giới. |
| *Không hỗ trợ giá trị tài khoản âm …* | Tài khoản đang dư nợ; nó không đóng góp gì vào tiền tiết kiệm của bạn. |

Giá trị trước đó vẫn hiển thị sau bất kỳ thông báo nào trong số này, được đánh dấu là cũ.
