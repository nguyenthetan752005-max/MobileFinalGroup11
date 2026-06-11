/* In-Progress Dropdown Handler */
document.addEventListener('DOMContentLoaded', function() {
    const inprogressBtn = document.getElementById('inprogressBtn');
    const inprogressDropdown = document.getElementById('inprogressDropdown');
    const inprogressCount = document.getElementById('inprogressCount');

    if (!inprogressBtn || !inprogressDropdown) return;

    // Get userId from window or session
    const userId = window.CURRENT_USER_ID;
    if (!userId) return;

    let isLoaded = false;

    // Toggle dropdown on click
    inprogressBtn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const parent = inprogressBtn.closest('.inprogress-dropdown');
        const isShowing = parent.classList.contains('show');

        if (isShowing) {
            parent.classList.remove('show');
        } else {
            parent.classList.add('show');
            if (!isLoaded) {
                loadInProgressLessons(userId);
            }
        }
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!inprogressBtn.contains(e.target) && !inprogressDropdown.contains(e.target)) {
            inprogressBtn.closest('.inprogress-dropdown').classList.remove('show');
        }
    });

    // Listen for progress updates to reload data
    document.addEventListener('progress:updated', function(e) {
        // Reload the count and data when progress changes
        if (userId) {
            // Update badge count immediately
            fetch('/api/progress/in-progress?userId=' + userId)
                .then(response => response.json())
                .then(data => {
                    if (inprogressCount) {
                        inprogressCount.textContent = data.length;
                        inprogressCount.style.display = data.length > 0 ? 'inline-block' : 'none';
                    }
                    // If dropdown is open, reload the list too
                    const parent = inprogressBtn.closest('.inprogress-dropdown');
                    if (parent.classList.contains('show') && isLoaded) {
                        renderInProgressLessons(data);
                    } else {
                        // Mark as not loaded so it will refresh next time
                        isLoaded = false;
                    }
                })
                .catch(err => console.error('Error updating count:', err));
        }
    });

    function loadInProgressLessons(userId) {
        inprogressDropdown.innerHTML = '<div class="inprogress-loading">Loading...</div>';

        fetch('/api/progress/in-progress?userId=' + userId)
            .then(response => {
                if (!response.ok) throw new Error('Failed to load');
                return response.json();
            })
            .then(data => {
                isLoaded = true;
                renderInProgressLessons(data);
                // Update badge count
                if (inprogressCount) {
                    inprogressCount.textContent = data.length;
                    inprogressCount.style.display = data.length > 0 ? 'inline-block' : 'none';
                }
            })
            .catch(err => {
                console.error('Error loading in-progress lessons:', err);
                inprogressDropdown.innerHTML = '<div class="inprogress-empty">Failed to load</div>';
            });
    }

    function renderInProgressLessons(lessons) {
        if (lessons.length === 0) {
            inprogressDropdown.innerHTML = '<div class="inprogress-empty">No lessons in progress</div>';
            return;
        }

        let html = '';
        lessons.forEach(function(lesson) {
            const url = lesson.practiceType === 'SPEAKING' 
                ? '/speaking/lesson/' + lesson.lessonId + '?sentenceIndex=' + lesson.firstUncompletedSentenceIndex
                : '/lesson/' + lesson.lessonId + '?sentenceIndex=' + lesson.firstUncompletedSentenceIndex;

            html += '<a href="' + url + '" class="inprogress-item" data-lesson-id="' + lesson.lessonId + '">';
            html += '<div class="inprogress-lesson-title">' + escapeHtml(lesson.lessonTitle) + '</div>';
            html += '<div class="inprogress-lesson-meta">' + escapeHtml(lesson.categoryName) + ' / ' + escapeHtml(lesson.sectionName) + '</div>';
            html += '<div class="inprogress-bar-container">';
            html += '<div class="inprogress-bar">';
            html += '<div class="inprogress-bar-fill" style="width: ' + lesson.progressPercent + '%"></div>';
            html += '</div>';
            html += '<span class="inprogress-percent">' + lesson.progressPercent + '%</span>';
            html += '</div>';
            html += '</a>';
        });

        inprogressDropdown.innerHTML = html;
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // Initial load of count
    fetch('/api/progress/in-progress?userId=' + userId)
        .then(response => response.json())
        .then(data => {
            if (inprogressCount) {
                inprogressCount.textContent = data.length;
                inprogressCount.style.display = data.length > 0 ? 'inline-block' : 'none';
            }
        })
        .catch(err => console.error('Error loading initial count:', err));
});
