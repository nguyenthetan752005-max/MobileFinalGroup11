/* LOGIC TÌM KIẾM BÀI HỌC VÀ LỌC LEVEL (FRONT-END).
 * - Theo dõi người dùng gõ phím vào ô Search và tự động ẩn/hiện Bài Học tương ứng 
 */

// Accordion Toggle
function toggleSection(headerEl) {
    headerEl.classList.toggle('collapsed');
    const gridEl = headerEl.nextElementSibling;
    
    if (headerEl.classList.contains('collapsed')) {
        gridEl.style.display = 'none';
    } else {
        gridEl.style.display = 'grid';
    }
}

// Search and Filter Logic
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('searchInput');
    const levelFilter = document.getElementById('levelFilter');
    const noResultsMsg = document.getElementById('noResultsMsg');

    function performSearch() {
        const searchText = searchInput.value.toLowerCase().trim();
        const selectedLevel = levelFilter.value;

        const sectionGroups = document.querySelectorAll('.section-group');
        let totalVisibleSections = 0;

        sectionGroups.forEach(sectionGroup => {
            const lessonCards = sectionGroup.querySelectorAll('.lesson-card');
            let hasVisibleLesson = false;

            lessonCards.forEach(card => {
                const titleText = card.querySelector('.lesson-title').textContent.toLowerCase();
                const cardLevel = card.getAttribute('data-level');

                const matchesText = titleText.includes(searchText);
                const matchesLevel = (selectedLevel === 'All levels') || (cardLevel === selectedLevel);

                if (matchesText && matchesLevel) {
                    card.style.display = 'block';
                    hasVisibleLesson = true;
                } else {
                    card.style.display = 'none';
                }
            });

            // Toggle section visibility based on matching lessons
            if (hasVisibleLesson) {
                sectionGroup.style.display = 'block'; 
                totalVisibleSections++;
                
                // Auto-expand section if it was collapsed
                const headerEl = sectionGroup.querySelector('.section-header');
                const gridEl = sectionGroup.querySelector('.lessons-grid');
                if (headerEl.classList.contains('collapsed')) {
                    headerEl.classList.remove('collapsed');
                    gridEl.style.display = 'grid';
                }
            } else {
                sectionGroup.style.display = 'none';
            }
        });

        if (totalVisibleSections === 0) {
            if (noResultsMsg) noResultsMsg.style.display = 'block';
        } else {
            if (noResultsMsg) noResultsMsg.style.display = 'none';
        }
    }

    if(searchInput) searchInput.addEventListener('keyup', performSearch);
    if(levelFilter) levelFilter.addEventListener('change', performSearch);
});
