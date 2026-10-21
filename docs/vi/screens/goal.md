---
layout: screen
lang: vi
base: "/vi"
key: "screens/goal"
screen: goal
title: Chi tiết mục tiêu
---
# Chi tiết mục tiêu

**Đây là gì.** Một mục tiêu cùng nguồn tiền của nó. Mở bằng cách chạm vào một thẻ trên thẻ [Mục tiêu]({{ page.base }}/screens/goals); **← Tất cả mục tiêu** để quay lại.

**Phần đầu.** Tên và nhãn trạng thái, đã có / mục tiêu, hạn hoàn thành, dòng dự báo và **Còn thiếu**: số tiền mục tiêu trừ đi phần đã có hôm nay.

**Các nút.** **Sửa mục tiêu** để thay đổi tên, tiền tệ, số tiền mục tiêu và hạn hoàn thành. **Lưu trữ** giữ lại mục tiêu mà không tính đến nó; mục tiêu đã lưu trữ hiển thị **Kích hoạt** thay vào đó. **Xóa** xóa mục tiêu cùng các liên kết của nó sau khi xác nhận.

## FIRE {#fire}

Mục tiêu loại **FIRE** (độc lập tài chính, nghỉ hưu sớm) trả lời hai câu hỏi: khi nào tiền có thể bắt đầu chi trả sinh hoạt phí của bạn, và nó đủ dùng trong bao lâu. Đây là dự báo dựa trên số liệu của chính bạn; không đưa ra khuyến nghị nào. Đặt loại bằng **Loại mục tiêu** (**Mục tiêu tiết kiệm** / **FIRE**) trong trình chỉnh sửa.

**Các trường trong trình chỉnh sửa.** Mọi trường ngoại trừ tên và tiền tệ đều có thể để trống; ứng dụng tự tính những gì có thể từ phần còn lại.

- **Ngày sinh · YYYY-MM-DD · tùy chọn**: hiển thị tuổi và đặt khoảng thời gian tính toán theo tuổi thọ.
- **Ngày FIRE · YYYY-MM-DD · để trống để tự tính**: ngày bạn ngừng tiết kiệm và bắt đầu chi tiêu.
- **Tôi có thể đầu tư · để trống để tự tính**: một số tiền **mỗi** **tuần**, **2 tuần**, **tháng**, **năm** hoặc **N ngày** (khi đó hãy nhập **Số ngày**).
- **Mức tăng khoản đầu tư % mỗi năm · để trống = lạm phát**: số tiền bạn có thể đầu tư tăng thêm mỗi năm, ví dụ khi được tăng lương hằng năm. Để trống thì khoản đóng góp giữ nguyên sức mua.
- **Chi tiêu mỗi tháng sau FIRE · để trống để tự tính**.
- **Số tiền cần có sau FIRE · để trống để tự tính**: số tiền FIRE, tức số tiền mục tiêu của mục tiêu.
- **Giả định nâng cao**: **Lạm phát % · trung bình / lạc quan / bi quan**, **Tăng trưởng % · trung bình / lạc quan / bi quan**, **Tỷ lệ rút tiền %**, **Làm việc đến tuổi** (mặc định 65) và **Tuổi thọ · năm**. Mỗi nhãn kết thúc bằng "· hiện {giá trị}", là giá trị đang có hiệu lực khi trường để trống.

Mọi số tiền tính theo tiền hôm nay của tiền tệ mục tiêu. Màn hình cũng hiển thị giá trị tại ngày FIRE theo tiền của tương lai. Số tiền mục tiêu hiện có lấy từ các hũ đã liên kết, như với mọi mục tiêu; [Kế hoạch]({{ page.base }}/screens/plans) không tham gia vào phép tính FIRE.

**Những gì được tự tính.**

- Ngày FIRE: tháng đầu tiên mà từ đó chi tiêu đủ dùng đến hết tuổi thọ (50 năm nếu không có ngày sinh) và, khi có số tiền mục tiêu, số tiền mục tiêu đạt được; không muộn hơn **Làm việc đến tuổi**, nếu không thì *Không đạt được trước* sinh nhật đó. Nếu không có số tiền mục tiêu, số tiền FIRE trở thành khoản mà quãng đời đó cần.
- Khoản đóng góp: từ số tiền mục tiêu và ngày FIRE.
- Số tiền mục tiêu: từ chi tiêu (chi tiêu × 12 ÷ tỷ lệ rút tiền), khi đã có ngày FIRE, hoặc từ ngày FIRE và khoản đóng góp.
- Chi tiêu: từ số tiền mục tiêu (số tiền mục tiêu × tỷ lệ rút tiền ÷ 12).

Nếu cả ngày FIRE, khoản đóng góp và số tiền mục tiêu đều được nhập, ngày được giữ nguyên và màn hình hiển thị phần còn thiếu cùng ngày sớm nhất có thể đạt được. Số tiền mục tiêu và ngày được tính ra sẽ được ghi vào số tiền mục tiêu và hạn hoàn thành của mục tiêu, nên việc sắp xếp, phân bổ, màn hình Tổng quan và Kế hoạch hoạt động như với mọi mục tiêu. Chúng được cập nhật sau mỗi lần chỉnh sửa và mỗi lần làm mới.

**Phần FIRE.** Trước tiên là các giá trị giống nhau ở mọi kịch bản (những gì bạn đã nhập và **Tỷ lệ rút tiền**), mỗi giá trị một dòng. Sau đó là một hàng có thể gập cho mỗi kịch bản, **Trung bình**, **Lạc quan** và **Bi quan**, kèm tỷ lệ tăng trưởng, lạm phát, ngày FIRE được tính ra khi bạn để trống ngày đó, và một huy hiệu: **Đủ tiền** (xanh lá) khi tiền đủ dùng vượt quá tuổi thọ, **tiền sẽ không bao giờ hết 🥳** (xanh lá) khi tiền không bao giờ cạn, **Không đủ tiền** (vàng) khi chưa đạt được ngày FIRE hoặc tiền hết sớm hơn. Chạm vào hàng để mở ra các giá trị khác nhau theo kịch bản:

| Hàng | Ý nghĩa |
|---|---|
| **Ngày FIRE** | Ngày đó, kèm "{N} tuổi" khi đã biết ngày sinh, hoặc *Không đạt được trước <ngày>* |
| **Đóng góp mỗi tháng** | Số tiền phải đầu tư mỗi tháng |
| **Số tiền FIRE · tiền hôm nay** | Số tiền cần có, đúng như trình chỉnh sửa ghi |
| **Số tiền FIRE · tại ngày FIRE** | Cùng số tiền đó sau khi tính lạm phát |
| **Chi tiêu mỗi tháng · tiền hôm nay** | Khoản chi mỗi tháng sau FIRE |
| **Chi tiêu mỗi tháng · tại ngày FIRE** | Cùng khoản chi đó sau khi tính lạm phát |
| **Tiền đủ dùng** | *đến <ngày> · {N} tuổi*, *{N} năm · vượt quá tuổi thọ*, *tiền sẽ không bao giờ hết 🥳*, hoặc *thiếu …* khi chưa đạt được ngày đó |

Bên dưới các hàng gập, dòng "Tỷ lệ: …" nêu nhà cung cấp mà các tỷ lệ đến từ, hoặc *mặc định tích hợp*. Biểu đồ bên dưới cho thấy tiền giảm dần: giá trị của tiền theo tiền hôm nay, từ hôm nay đến khi tiền hết hoặc hết khoảng thời gian tính toán, với ba đường màu, mỗi kịch bản một đường. Đường nét đứt đánh dấu ngày FIRE. Ghi chú bên dưới ghi *Dự báo dựa trên số liệu của chính bạn; không đưa ra khuyến nghị nào.* Trên thẻ [Mục tiêu]({{ page.base }}/screens/goals), mục tiêu hiển thị *FIRE vào <ngày> · {N} tuổi*, hoặc *không đạt được*.

**Cách tính.** Theo từng bước một tháng. Đến ngày FIRE, khoản đóng góp tăng theo **Mức tăng khoản đầu tư % mỗi năm** (theo lạm phát khi để trống, nên giữ nguyên sức mua), và tiền tăng trưởng theo tỷ lệ của kịch bản. Sau ngày FIRE, chi tiêu tăng theo lạm phát và phần còn lại tiếp tục tăng trưởng theo tỷ lệ của kịch bản. Khoảng thời gian tính toán là ngày sinh cộng tuổi thọ (mặc định 95), hoặc 50 năm sau ngày FIRE nếu không có ngày sinh. *Tiền sẽ không bao giờ hết* nghĩa là tiền không bị dùng hết trong 200 năm và vào cuối kỳ có giá trị theo tiền hôm nay ít nhất bằng tại ngày FIRE.

**Mặc định và nguồn.** Khi không có giá trị ghi đè hay tỷ lệ tải về, ứng dụng dùng tăng trưởng 7 % (lạc quan 10 %, bi quan 4 %), lạm phát 3 % (2 %, 5 %) và tỷ lệ rút tiền 4 %. Chúng dựa trên UBS Global Investment Returns Yearbook 2026 (toàn thế giới, 1900–2024: cổ phiếu 9,8 % mỗi năm danh nghĩa, trái phiếu 4,6 %, lạm phát 2,9 %; 7 % xấp xỉ danh mục 60/40), mục tiêu lạm phát 2 % của các ngân hàng trung ương, cùng Bengen (1994) và nghiên cứu Trinity (1998) cho quy tắc 4 %. Xem thêm [FIRE]({{ page.base }}/manual#fire) trong hướng dẫn sử dụng.

**Tỷ lệ trực tiếp.** **Làm mới** tải lạm phát và tăng trưởng thị trường cho tiền tệ của mọi mục tiêu FIRE, từ các nhà cung cấp chọn trong [Cài đặt]({{ page.base }}/screens/settings). Thứ tự ưu tiên: giá trị bạn ghi đè trong **Giả định nâng cao**, rồi giá trị tải về, rồi mặc định. Nếu tải thất bại, chuỗi trước đó được giữ lại và lỗi được hiển thị. Cả hai nguồn tăng trưởng đều là chỉ số giá không tính cổ tức, nên đánh giá thấp tổng lợi suất.

**Thông báo.**

| Thông báo | Cần làm gì |
|---|---|
| *Hãy nhập số tiền mục tiêu, khoản chi mỗi tháng, hoặc cả ngày FIRE lẫn khoản đóng góp* | Điền một trong ba mục; nếu thiếu thì không tính được gì. |
| *Hãy nhập ngày FIRE hoặc khoản đóng góp* | Đã biết số tiền mục tiêu nhưng chưa có ngày lẫn khoản đóng góp; hãy nhập một trong hai. |
| *Ngày FIRE đã qua* | Chọn một ngày trong tương lai. |
| *Ngày sinh nằm trong tương lai* | Sửa lại ngày sinh. |
| *Tỷ lệ rút tiền phải lớn hơn không* | Nhập tỷ lệ rút tiền lớn hơn 0 trong **Giả định nâng cao**, hoặc xóa trống để dùng mặc định. |
| *Không đạt được trước <ngày>* | Với khoản đóng góp và các tỷ lệ này, số tiền mục tiêu không đạt được trước ngày đó (cuối khoảng thời gian tính toán); hãy tăng khoản đóng góp, giảm số tiền mục tiêu hoặc chi tiêu, hoặc chọn kịch bản khác. |
| *Không có dữ liệu tỷ lệ cho <tiền tệ>* | Nhà cung cấp lạm phát hoặc tăng trưởng không có chuỗi dữ liệu cho tiền tệ này; hãy chọn nhà cung cấp khác hoặc nhập các tỷ lệ trong **Giả định nâng cao**. |
| *ECB không hỗ trợ <tiền tệ>* | ECB chỉ cung cấp lạm phát của khu vực đồng euro; hãy chọn nhà cung cấp **Lạm phát** khác trong Cài đặt. |
| *OECD không hỗ trợ <tiền tệ>* | OECD không có chuỗi dữ liệu cho tiền tệ này (ví dụ RUB); hãy chọn nhà cung cấp **Lạm phát** khác trong Cài đặt. |

**Nguồn tiền.** Các hũ được phép cấp tiền cho mục tiêu này. **Liên kết hũ** thêm một hũ kèm giới hạn đóng góp:

- **Tự động — tối đa bằng phần còn thiếu**: hũ cấp bất cứ số tiền nào mục tiêu còn thiếu, sau khi các mục tiêu sớm hơn đã nhận phần của chúng.
- **Số tiền cố định theo tiền tệ của mục tiêu**.
- **% của hũ**: tối đa bằng tỷ lệ đó trên giá trị của hũ.
- **% của mục tiêu**: tối đa bằng tỷ lệ đó trên số tiền mục tiêu.

Phần xem trước trong trình chỉnh sửa cho biết liên kết sẽ đóng góp bao nhiêu hôm nay. Giới hạn là mức trần: thứ tự mục tiêu, tiền tiết kiệm khả dụng và các liên kết khác có thể làm giảm khoản đóng góp. Mỗi nguồn trong danh sách cho biết hiện nó đóng góp bao nhiêu và vì sao không đóng góp thêm; **Sửa liên kết** để thay đổi giới hạn, **Ngắt liên kết** để xóa liên kết.

**Tiết kiệm dự kiến.** Các kế hoạch đến được mục tiêu này, mỗi kế hoạch kèm số tiền mà phần dự báo gán cho nó. Kế hoạch đã được các mục tiêu sớm hơn dùng hết sẽ không xuất hiện ở đây.

**Đọc phần dự báo.** "Các khoản tiết kiệm dự kiến sẽ hoàn thành mục tiêu này vào 20 thg 12, 2026 · đúng hạn" nghĩa là tổng các kế hoạch tính đến ngày đó đáp ứng phần còn thiếu trước hạn. "Tiết kiệm dự kiến đáp ứng tối đa … · còn thiếu …" nghĩa là chưa đáp ứng được; hãy thêm kế hoạch, dời ngày hoặc giảm số tiền mục tiêu.
