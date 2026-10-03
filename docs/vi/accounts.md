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

Capital chỉ kết nối với các giao diện có thông tin xác thực dài hạn: một mã truy cập hoặc khóa bạn tạo một lần và có hiệu lực cho đến khi bạn thu hồi, đến hạn bạn đã chọn, hoặc ít nhất vài tháng (mã T-Invest hết hạn sau ba tháng không dùng, mã ALOR sau một năm). Mọi nhà môi giới dưới đây đều dùng được dù ứng dụng đặt ở ngôn ngữ nào. Hiện được hỗ trợ:

| Nhà môi giới | Giao diện sử dụng | Dữ liệu được đọc |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (chỉ để lấy báo cáo) | Giá trị tài sản ròng của ngày làm việc gần nhất, theo tiền tệ cơ sở của tài khoản |
| [OANDA](#oanda) | v20 REST API, tài khoản thực fxTrade | Giá trị tài sản ròng tại thời điểm làm mới, theo tiền tệ của tài khoản |
| [Trading 212](#trading-212) | Public API, tài khoản Invest và Stocks ISA | Tổng giá trị tài khoản tại thời điểm làm mới, theo tiền tệ chính của tài khoản |
| [SnapTrade](#snaptrade) | SnapTrade Personal, một đơn vị tổng hợp (aggregator) bao phủ nhiều nhà môi giới | Tổng giá trị tài khoản theo báo cáo của nhà môi giới gửi SnapTrade, theo tiền tệ của tài khoản |
| [Alpaca](#alpaca) | Trading API, tài khoản thực | Vốn chủ sở hữu (tiền mặt cộng các vị thế), theo đô la Mỹ |
| [Tradier](#tradier) | Brokerage API | Tổng vốn chủ sở hữu, theo đô la Mỹ |
| [tastytrade](#tastytrade) | Open API với OAuth grant cá nhân | Giá trị thanh lý ròng (net liquidating value), theo đô la Mỹ |
| [Public.com](#public) | Individual API | Tổng giá trị tài khoản, theo đô la Mỹ |
| [eToro](#etoro) | Public API | Số dư của tài khoản đã chọn (với tài khoản giao dịch: tiền mặt cộng các vị thế đã đầu tư), theo tiền tệ của nó |
| [Indexa Capital](#indexa-capital) | REST API, mã chỉ đọc | Tổng danh mục tại ngày định giá gần nhất, theo tiền tệ của tài khoản |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | Tổng giá trị danh mục, theo rúp |
| [ALOR](#alor) | ALOR OpenAPI | Định giá danh mục trên Sàn giao dịch Moscow, theo rúp |
| [Capital.com](#capital-com) | Public API, tài khoản thực | Số dư gồm lãi và lỗ đang mở, theo tiền tệ của tài khoản |
| [Akahu](#akahu) | Ứng dụng cá nhân Akahu, đơn vị tổng hợp của New Zealand | Số dư của tài khoản đã kết nối (Sharesies, Hatch, Kernel, KiwiSaver và các nhà khác), theo tiền tệ của nó |

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

## Kết nối tài khoản trong Capital {#connect}

Các phần dưới đây nêu cách tạo thông tin xác thực ở từng nhà môi giới. Trong Capital, các bước giống nhau cho tất cả:

1. **Môi giới → Thông tin xác thực**: nhấn các nút thông tin xác thực của nhà môi giới và dán từng giá trị.
2. **Môi giới → +**: nhập tên, chọn **Nhà môi giới**, rồi nhấn **Lấy tài khoản** và chọn tài khoản (hoặc nhập mã của nó) và lưu.
3. Mở hũ, **Thêm tài sản**, đặt **Theo dõi** thành **Tài khoản môi giới**, chọn tài khoản và lưu. Nhấn **Làm mới**.

## Alpaca {#alpaca}

Alpaca cấp một key id và một mã bí mật cho mỗi tài khoản; chúng có hiệu lực cho đến khi bạn tạo lại. Chỉ tài khoản thực được đọc: khóa của tài khoản giao dịch thử (paper) không dùng được với API thực.

1. Đăng nhập [bảng điều khiển Alpaca](https://app.alpaca.markets), chuyển sang tài khoản thực của bạn và, trên trang chủ, trong mục **API Keys**, nhấn **Generate New Keys**.
2. Sao chép **API Key ID** và **Secret Key**; mã bí mật chỉ hiển thị một lần.
3. Trong Capital, dán chúng vào **Khóa API: Alpaca** và **Mã bí mật API: Alpaca**, rồi làm theo [Kết nối tài khoản](#connect). **Lấy tài khoản** hiển thị số tài khoản của khóa.

Tài liệu của Alpaca: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

Mã API trong phần cài đặt Tradier của bạn không bao giờ hết hạn.

1. Đăng nhập Tradier và mở [Settings → API Access](https://web.tradier.com/user/api). Sao chép **API Access Token** của tài khoản môi giới (không phải mã sandbox).
2. Trong Capital, dán nó vào **Mã truy cập: Tradier**, rồi làm theo [Kết nối tài khoản](#connect).

Tài liệu của Tradier: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade dùng OAuth grant cá nhân: bạn tạo một ứng dụng cho riêng mình và một grant có mã làm mới không bao giờ hết hạn. Mỗi lần làm mới, Capital đổi nó lấy mã truy cập có hiệu lực 15 phút.

1. Trên [my.tastytrade.com](https://my.tastytrade.com), mở **Manage → My Profile → API → OAuth Applications** và nhấn **+ New OAuth client**. Đặt tên, một redirect URI HTTPS bất kỳ (ví dụ `https://capital.fimych.dev`) và chỉ chọn phạm vi **read**. Lưu và sao chép **Client Secret**; nó chỉ hiển thị một lần.
2. Nhấn **Manage** bên cạnh ứng dụng, rồi **Create Grant**, và sao chép **refresh token**.
3. Trong Capital, dán chúng vào **Mã làm mới: tastytrade** và **Client secret: tastytrade**, rồi làm theo [Kết nối tài khoản](#connect).

Tài liệu của tastytrade: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

API Individual của Public dành cho tài khoản của chính bạn. Khóa bí mật có hiệu lực dài hạn và có thể thu hồi; mỗi lần làm mới, Capital đổi nó lấy mã truy cập có hiệu lực năm phút.

1. Trong ứng dụng web của Public, mở trang **API** trong phần cài đặt và tạo một **khóa bí mật** (secret key).
2. Trong Capital, dán nó vào **Khóa bí mật: Public.com**, rồi làm theo [Kết nối tài khoản](#connect).

Tài liệu của Public: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

Khóa eToro có hiệu lực dài hạn; bạn có thể đặt ngày hết hạn và danh sách IP cho chúng, và có thể để chúng chỉ đọc. Tài khoản eToro của bạn phải được xác minh.

1. Trong eToro, mở **Settings → Trading → API Key Management** và nhấn **Create New Key**. Chọn môi trường **Real**, quyền **Read**, không dùng danh sách IP, và tùy chọn đặt ngày hết hạn. Xác nhận bằng mã SMS.
2. Sao chép **Public API Key** và **User Key**; khóa người dùng chỉ hiển thị một lần.
3. Trong Capital, dán chúng vào **Khóa API công khai: eToro** và **Khóa người dùng: eToro**, rồi làm theo [Kết nối tài khoản](#connect). **Lấy tài khoản** liệt kê các tài khoản giao dịch, tiền mặt và tài khoản eToro khác của bạn.

Tài liệu của eToro: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Mã từ khu vực riêng của Indexa chỉ có quyền đọc. Nó gắn với e-mail, mật khẩu và thiết bị của bạn: sau khi đổi mật khẩu, hãy tạo lại.

1. Trong khu vực riêng của Indexa, mở **Cài đặt người dùng → Ứng dụng** (User settings → Applications) và sao chép mã.
2. Trong Capital, dán nó vào **Mã truy cập: Indexa Capital**, rồi làm theo [Kết nối tài khoản](#connect). Cả tài khoản hưu trí và tài khoản đầu tư đều được liệt kê.

Indexa định giá quỹ mỗi ngày làm việc một lần; ngày quan sát là ngày định giá đó.

Tài liệu của Indexa Capital: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Invest API của T-Bank chấp nhận mã bạn cấp trong phần cài đặt đầu tư. Mã hết hạn sau ba tháng kể từ lần dùng cuối và phải được dùng trong vòng bảy ngày kể từ khi cấp; làm mới mỗi tuần sẽ giữ cho nó còn hiệu lực. Hãy chọn mã **chỉ đọc**.

1. Mở [cài đặt T-Invest](https://www.tbank.ru/invest/settings/) và cấp một **mã T-Invest API** (T-Invest API token) cho sàn giao dịch với quyền truy cập **chỉ đọc** (tất cả tài khoản hoặc một tài khoản). Việc xác nhận giao dịch bằng mã phải được tắt thì mới cấp được. Sao chép mã; nó chỉ hiển thị một lần.
2. Trong Capital, dán nó vào **Mã truy cập: T-Invest**, rồi làm theo [Kết nối tài khoản](#connect).

T-Bank cung cấp API này dưới Russian Trusted Root CA, chứng thư mà Android không có sẵn. Capital tin cậy chứng thư đó chỉ cho địa chỉ T-Invest API (`invest-public-api.tbank.ru`), và không cho kết nối nào khác.

Tài liệu của T-Invest: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR cấp mã làm mới có hiệu lực một năm; mỗi lần làm mới, Capital đổi nó lấy mã truy cập có hiệu lực 30 phút. ALOR không có mã chỉ đọc: mã này có thể giao dịch, còn Capital chỉ đọc.

1. Đăng nhập [cổng nhà phát triển ALOR](https://alor.dev), liên kết tài khoản giao dịch của bạn, mở **API Access Tokens** và nhấn **Create Token**. Sao chép mã làm mới.
2. Trong Capital, dán nó vào **Mã làm mới: ALOR**, rồi làm theo [Kết nối tài khoản](#connect). **Lấy tài khoản** liệt kê các danh mục của tài khoản (thị trường chứng khoán D…, thị trường tiền tệ G…, phái sinh 7500…); hãy thêm mỗi danh mục một tài khoản.

Tài liệu của ALOR: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Khóa Capital.com mặc định có hiệu lực một năm, hoặc đến ngày bạn chọn. Chúng mang quyền giao dịch (Capital.com không có khóa chỉ đọc); Capital chỉ đọc. Khóa có mật khẩu riêng, không phải mật khẩu tài khoản của bạn.

1. Bật xác thực hai yếu tố, rồi mở **Settings → API integrations** và nhấn **Generate API key**. Đặt nhãn và một **mật khẩu tùy chỉnh** (custom password), giữ hoặc đặt ngày hết hạn, và xác nhận bằng mã 2FA. Sao chép khóa; nó chỉ hiển thị một lần.
2. Trong Capital, dán **Khóa API: Capital.com**, e-mail đăng nhập của bạn vào **E-mail đăng nhập: Capital.com** và mật khẩu tùy chỉnh vào **Mật khẩu khóa API: Capital.com**, rồi làm theo [Kết nối tài khoản](#connect). Chỉ tài khoản thực được đọc.

Tài liệu của Capital.com: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) kết nối các ngân hàng, nền tảng đầu tư và quỹ KiwiSaver của New Zealand; một ứng dụng cá nhân miễn phí đọc các tài khoản của chính bạn. Akahu làm mới dữ liệu khoảng một lần mỗi ngày.

1. Đăng ký tại [my.akahu.nz](https://my.akahu.nz) và kết nối các nhà cung cấp của bạn (ví dụ Sharesies, Hatch, Kernel, Simplicity, Milford hoặc quỹ KiwiSaver của bạn).
2. Mở trang **Developers**, chấp nhận điều khoản dành cho nhà phát triển và sao chép **App ID Token** và **User Access Token**.
3. Trong Capital, dán chúng vào **Mã ID ứng dụng: Akahu** và **Mã truy cập người dùng: Akahu**, rồi làm theo [Kết nối tài khoản](#connect).

Tài liệu của Akahu: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Nhà môi giới phổ biến theo thị trường {#by-market}

Cách kết nối các nhà môi giới được dùng nhiều nhất tại các thị trường của những ngôn ngữ Capital hỗ trợ, tính đến tháng 10 năm 2026. *Trực tiếp* nghĩa là có một phần ở trên; *SnapTrade* nghĩa là qua [SnapTrade](#snaptrade); còn lại là lý do không thể đọc được, và số dư có thể được giữ dưới dạng tài sản **Thủ công**.

| Thị trường | Nhà môi giới | Cách kết nối |
|---|---|---|
| Hoa Kỳ | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Trực tiếp |
| Hoa Kỳ | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| Hoa Kỳ | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | Không có API công khai |
| Canada | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Canada | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | Không có API công khai |
| Vương quốc Anh và Ireland | Trading 212, eToro, Interactive Brokers | Trực tiếp |
| Vương quốc Anh và Ireland | AJ Bell | SnapTrade |
| Vương quốc Anh và Ireland | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | Không có API công khai |
| Vương quốc Anh và Ireland | IG | Không thể: mỗi phiên đều cần mật khẩu tài khoản |
| Châu Âu | Indexa Capital (Tây Ban Nha), eToro, Trading 212, Interactive Brokers | Trực tiếp |
| Châu Âu | DEGIRO, BUX | SnapTrade |
| Châu Âu | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | Không có API công khai cho đầu tư |
| Châu Âu | XTB | Không thể: API đã đóng vào tháng 3 năm 2025 |
| Châu Âu | Saxo, comdirect | Không thể: chỉ có mã ngắn hạn hoặc phiên TAN |
| Châu Âu | Bitpanda, Freedom24 | Không thể: API không trả về tổng giá trị tài khoản |
| Nga và Kazakhstan | T-Invest, ALOR | Trực tiếp |
| Nga và Kazakhstan | BCS | Không thể: không có tổng giá trị, và mã hết hạn sau 90 ngày |
| Nga và Kazakhstan | Finam | Chưa hỗ trợ: tiền tệ của giá trị tài khoản không được ghi trong tài liệu |
| Nga và Kazakhstan | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | Không có API công khai, hoặc API không có tổng giá trị |
| Ấn Độ | Zerodha, Upstox | SnapTrade (quy định của SEBI kết thúc phiên API mỗi ngày, nên kết nối cần được gia hạn thường xuyên) |
| Ấn Độ | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | Không thể: quy định của SEBI kết thúc mọi phiên API mỗi ngày |
| Pakistan và Bangladesh | Tất cả các nhà môi giới trên sàn | Không có API công khai |
| Trung Quốc, Hồng Kông và Đài Loan | moomoo | SnapTrade |
| Trung Quốc, Hồng Kông và Đài Loan | Futu, Tiger Brokers, Longbridge | Chưa hỗ trợ: thời hạn khóa hoặc định dạng phản hồi chưa được ghi đầy đủ, hoặc khóa không thể giới hạn ở quyền đọc |
| Trung Quốc, Hồng Kông và Đài Loan | East Money, Huatai, CITIC, Yuanta, Fubon | Không có API web công khai (chỉ có thiết bị đầu cuối máy tính hoặc SDK chứng chỉ) |
| Nhật Bản | OANDA Japan (tài khoản đủ điều kiện dùng API) | Trực tiếp, như OANDA |
| Nhật Bản | SBI Securities, Rakuten Securities, Monex, Matsui | Không có API công khai |
| Úc và New Zealand | CommSec, Stake | SnapTrade |
| Úc và New Zealand | Sharesies, Hatch, Kernel, Simplicity, quỹ KiwiSaver | Akahu (tài khoản New Zealand) |
| Trung Đông và Châu Phi | eToro | Trực tiếp |
| Trung Đông và Châu Phi | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | Không có API công khai cho cá nhân |
| Đông Nam Á | Stockbit, Ajaib, Bibit, IPOT, VPS | Không có API công khai |
| Đông Nam Á | SSI, TCBS, DNSE | Không thể: mã 8 giờ kèm mã dùng một lần, hoặc chỉ có số dư tiền mặt |
| Châu Mỹ Latinh | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | Không có API công khai cho cá nhân, hoặc chỉ đăng nhập bằng mật khẩu |
| Ngoại hối và CFD | OANDA, Capital.com | Trực tiếp |
| Ngoại hối và CFD | Các nhà môi giới MetaTrader (XM, Exness, Pepperstone, IC Markets, Admirals) | Không thể: không có quyền đọc qua HTTPS |
| Ngoại hối và CFD | Các nhà môi giới cTrader, FXCM, Forex.com | Không thể: cần đăng ký ứng dụng, API đã ngừng hỗ trợ hoặc đăng nhập bằng mật khẩu |

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
| *tastytrade từ chối mã làm mới hoặc client secret; hãy tạo grant mới* | Tạo grant mới cho ứng dụng và dán mã làm mới của nó; kiểm tra client secret. |
| *Capital.com không mở được phiên; hãy kiểm tra khóa API, thông tin đăng nhập và mật khẩu khóa* | Khóa, e-mail hoặc mật khẩu tùy chỉnh của khóa bị sai, hoặc khóa đã hết hạn. |
| *Không tìm thấy tài khoản; hãy chọn lại* | Nhà môi giới không còn liệt kê tài khoản này; hãy sửa nó và chọn từ **Lấy tài khoản**. |

Giá trị trước đó vẫn hiển thị sau bất kỳ thông báo nào trong số này, được đánh dấu là cũ.
