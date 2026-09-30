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

**Nguồn tiền.** Các hũ được phép cấp tiền cho mục tiêu này. **Liên kết hũ** thêm một hũ kèm giới hạn đóng góp:

- **Tự động — tối đa bằng phần còn thiếu**: hũ cấp bất cứ số tiền nào mục tiêu còn thiếu, sau khi các mục tiêu sớm hơn đã nhận phần của chúng.
- **Số tiền cố định theo tiền tệ của mục tiêu**.
- **% của hũ**: tối đa bằng tỷ lệ đó trên giá trị của hũ.
- **% của mục tiêu**: tối đa bằng tỷ lệ đó trên số tiền mục tiêu.

Phần xem trước trong trình chỉnh sửa cho biết liên kết sẽ đóng góp bao nhiêu hôm nay. Giới hạn là mức trần: thứ tự mục tiêu, tiền tiết kiệm khả dụng và các liên kết khác có thể làm giảm khoản đóng góp. Mỗi nguồn trong danh sách cho biết hiện nó đóng góp bao nhiêu và vì sao không đóng góp thêm; **Sửa liên kết** để thay đổi giới hạn, **Ngắt liên kết** để xóa liên kết.

**Tiết kiệm dự kiến.** Các kế hoạch đến được mục tiêu này, mỗi kế hoạch kèm số tiền mà phần dự báo gán cho nó. Kế hoạch đã được các mục tiêu sớm hơn dùng hết sẽ không xuất hiện ở đây.

**Đọc phần dự báo.** "Các khoản tiết kiệm dự kiến sẽ hoàn thành mục tiêu này vào 20 thg 12, 2026 · đúng hạn" nghĩa là tổng các kế hoạch tính đến ngày đó đáp ứng phần còn thiếu trước hạn. "Tiết kiệm dự kiến đáp ứng tối đa … · còn thiếu …" nghĩa là chưa đáp ứng được; hãy thêm kế hoạch, dời ngày hoặc giảm số tiền mục tiêu.
