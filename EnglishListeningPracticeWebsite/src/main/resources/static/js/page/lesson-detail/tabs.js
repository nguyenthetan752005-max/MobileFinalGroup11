/* [MODULE 4: CHUYỂN TAB] LOGIC BẤM CHUYỂN QUA LẠI GIỮA HAI TAB.
 * - Click Dictation -> Ẩn Transcript. Click Transcript -> Ẩn Dictation 
 */

/**
 * js/pages/lesson-detail/tabs.js
 * Handles Dictation vs Transcript tab switching.
 */
document.addEventListener('DOMContentLoaded', () => {

    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            tabBtns.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));
            
            btn.classList.add('active');
            
            const targetPaneId = btn.getAttribute('data-tab');
            const targetPane = document.getElementById(targetPaneId);
            if (targetPane) {
                targetPane.classList.add('active');
            }
        });
    });

});
