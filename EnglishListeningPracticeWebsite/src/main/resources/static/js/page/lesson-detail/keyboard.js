/* [MODULE 5: PHÍM TẮT] GẮN SỰ KIỆN BÀN PHÍM VÀO TRÌNH DUYỆT.
 * - Phím Space: Play/Pause. Phím Mũi tên: Chuyển câu. Phím Enter: Check đáp án 
 */

/**
 * js/pages/lesson-detail/keyboard.js
 * Centralized keyboard shortcuts for Dictation/Transcript functionality.
 */
document.addEventListener('DOMContentLoaded', () => {

    const inputEl = document.getElementById('dictationInput');
    const checkBtn = document.getElementById('checkBtn');

    document.addEventListener('keydown', (e) => {
        const tag = e.target.tagName;
        const isEditable = e.target.isContentEditable;
        const isInput = (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || isEditable);

        // Allow Enter on dictation input to submit, but skip all other shortcuts for input elements
        if (isInput && !(e.target === inputEl && e.key === 'Enter')) return;

        switch (e.key) {
            case ' ':
                // If it's an input/textarea, do NOTHING (let user type Space normally)
                if (isInput) return;
                if (e.target !== inputEl) {
                    e.preventDefault(); // Prevent page scroll
                    window.LessonState.togglePlay();
                }
                break;
            case 'ArrowLeft':
                e.preventDefault();
                window.LessonState.prev();
                break;
            case 'ArrowRight':
                e.preventDefault();
                window.LessonState.next();
                break;
            case 'Enter':
                if (e.target === inputEl && checkBtn) {
                    e.preventDefault();
                    checkBtn.click();
                }
                break;
        }
    });

});
