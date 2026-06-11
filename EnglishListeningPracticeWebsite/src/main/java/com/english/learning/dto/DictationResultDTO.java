// DTO TRUYỀN KẾT QUẢ CHẤM CHÍNH TẢ TỪ BACKEND VỀ FRONTEND.
// - Chứa số từ đúng, mảng hint (từng từ tách riêng), chỉ số từ gợi ý mới.

package com.english.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictationResultDTO {

    /** Người dùng đã gõ đúng toàn bộ câu hay chưa */
    private boolean correct;

    /** Số từ đúng liên tiếp tính từ đầu câu */
    private int matchedCount;

    /** Tổng số từ trong câu gốc */
    private int totalWords;

    /** Mảng từ gợi ý: mỗi phần tử là từ gốc hoặc "***"
     *  VD: ["it", "snowed", "***", "***", "***"] */
    private List<String> hintWords;

    /** Chỉ số từ MỚI được gợi ý (tô xanh ở Frontend).
     *  -1 nếu đúng hết hoặc skip. */
    private int newHintIndex;

    /** Câu gốc đầy đủ (chỉ trả về khi đúng hết hoặc skip) */
    private String correctSentence;
}
