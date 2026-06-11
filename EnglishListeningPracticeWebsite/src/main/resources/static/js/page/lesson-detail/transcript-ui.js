/* [MODULE 3: GIAO DIỆN TOÀN VĂN] XỬ LÝ TAB TRANSCRIPT.
 * - Hỗ trợ cả AUDIO và VIDEO lessons
 * - Auto-scroll theo câu đang đọc
 * - Click nút Play nhỏ cạnh từng câu → phát audio hoặc seekTo video
 */

document.addEventListener('DOMContentLoaded', () => {

    const isVideo = (typeof LESSON_TYPE !== 'undefined') && LESSON_TYPE === 'VIDEO';

    const timeDisplay = document.getElementById('transcriptTimeDisplay');
    const progressFill = document.getElementById('transcriptProgressFill');
    const progressWrapper = document.getElementById('transcriptProgressWrapper');
    const volumeBtn = document.getElementById('transcriptVolumeBtn');
    
    const sentenceTextEl = document.getElementById('sentenceText');
    const transcriptSentenceNum = document.getElementById('transcriptSentenceNum');
    const prevBtn = document.getElementById('transcriptPrevBtn');
    const nextBtn = document.getElementById('transcriptNextBtn');
    const transcriptItems = document.querySelectorAll('.transcript-item');
    const repeatCheckbox = document.getElementById('repeatCheckbox');
    const transcriptTab = document.getElementById('transcript-tab');

    function updateTranscriptPlayButtons() {
        transcriptItems.forEach((item) => {
            const playSmallBtn = item.querySelector('.play-small-btn');
            const index = parseInt(item.dataset.index);
            if (!playSmallBtn || isNaN(index)) return;

            const isActivePlaying = index === window.LessonState.currentIndex && window.LessonState.isPlaying;
            playSmallBtn.innerHTML = isActivePlaying ? window.LessonState.pauseSvg : window.LessonState.playSvg;
        });
    }

    function updateTranscriptNextButton() {
        if (!nextBtn) return;
        const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
        nextBtn.style.display = isLastSentence ? 'none' : '';
    }

    function isTranscriptTabActive() {
        return transcriptTab && transcriptTab.classList.contains('active');
    }

    function playCurrentSentence() {
        if (window.LessonState.isVideo) {
            const sentence = window.LessonState.sentences[window.LessonState.currentIndex];
            if (sentence && sentence.startTime != null) {
                document.dispatchEvent(new CustomEvent('video:seekToTime', {
                    detail: { startTime: sentence.startTime, endTime: sentence.endTime }
                }));
            } else {
                window.LessonState.play();
            }
            return;
        }
        window.LessonState.play();
    }

    // --- State Listeners ---
    function handleSentenceChange(index, sentence) {
        if (sentenceTextEl) sentenceTextEl.textContent = sentence.content || '';
        if (transcriptSentenceNum) transcriptSentenceNum.textContent = index + 1;

        // Reset audio UI (chỉ cho AUDIO mode)
        if (!isVideo) {
            if (progressFill) progressFill.style.width = '0%';
            if (timeDisplay) timeDisplay.textContent = '0:00 / 0:00';
        }

        // Highlight active item
        transcriptItems.forEach(item => item.classList.remove('active'));
        if (transcriptItems[index]) {
            transcriptItems[index].classList.add('active');

            // Auto scroll
            const autoScrollCheckbox = document.getElementById('autoScrollCheckbox');
            if (autoScrollCheckbox && autoScrollCheckbox.checked) {
                transcriptItems[index].scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
        updateTranscriptPlayButtons();
        updateTranscriptNextButton();
    }

    document.addEventListener('lesson:sentenceChanged', (e) => {
        handleSentenceChange(e.detail.index, e.detail.sentence);
    });

    // Cập nhật ngay lập tức nếu LessonState đã sẵn sàng
    if (window.LessonState && window.LessonState.sentences && window.LessonState.sentences.length > 0) {
        const curIdx = window.LessonState.currentIndex;
        handleSentenceChange(curIdx, window.LessonState.sentences[curIdx]);
    }

    document.addEventListener('lesson:playState', (e) => {
        updateTranscriptPlayButtons();
    });

    // --- Audio-only UI updates ---
    if (!isVideo) {
        document.addEventListener('lesson:timeUpdate', (e) => {
            if (progressFill) progressFill.style.width = e.detail.percent + '%';
            if (timeDisplay) timeDisplay.textContent = e.detail.timeStr;
        });

        document.addEventListener('lesson:ended', () => {
            if (repeatCheckbox && repeatCheckbox.checked) {
                const audio = document.getElementById('mainAudio');
                if (audio) {
                    audio.currentTime = 0;
                    audio.play();
                }
            }
        });

        document.addEventListener('lesson:muteChanged', (e) => {
            if (volumeBtn) {
                const icon = e.detail.muted ? 'fa-volume-mute' : 'fa-volume-up';
                volumeBtn.innerHTML = '<i class="fas ' + icon + '"></i>';
            }
        });
    }

    // Transcript flow: repeat current or auto-play next sentence
    document.addEventListener('lesson:ended', () => {
        if (!isTranscriptTabActive()) return;

        updateTranscriptPlayButtons();

        if (repeatCheckbox && repeatCheckbox.checked) {
            playCurrentSentence();
            return;
        }

        const nextIndex = window.LessonState.currentIndex + 1;
        if (nextIndex < window.LessonState.sentences.length) {
            window.LessonState.loadSentence(nextIndex);
            playCurrentSentence();
        }
    });

    // --- UI Event Bindings (Audio mode only) ---
    if (!isVideo) {
        if (volumeBtn) volumeBtn.addEventListener('click', () => window.LessonState.toggleMute());

        if (progressWrapper) {
            progressWrapper.addEventListener('click', (e) => {
                const rect = progressWrapper.getBoundingClientRect();
                window.LessonState.seekRatio((e.clientX - rect.left) / rect.width);
            });
        }
    }

    // --- Navigation (cả Audio và Video) ---
    if (prevBtn) prevBtn.addEventListener('click', () => {
        window.LessonState.prev();
        window.LessonState.play();
    });

    if (nextBtn) nextBtn.addEventListener('click', () => {
        window.LessonState.next();
        window.LessonState.play();
    });

    // --- List item row clicks (play sentence from start) ---
    transcriptItems.forEach((item) => {
        const index = parseInt(item.dataset.index);
        if (isNaN(index)) return;

        item.style.cursor = 'pointer';

        // Chỉ xử lý khi click vào nút play nhỏ cạnh câu
        const playSmallBtn = item.querySelector('.play-small-btn');
        if (playSmallBtn) {
            playSmallBtn.addEventListener('click', (e) => {
                e.stopPropagation(); // Ngăn sự kiện lan ra toàn dòng item
                if (window.LessonState.currentIndex === index) {
                    window.LessonState.togglePlay();
                } else {
                    window.LessonState.loadSentence(index);
                    window.LessonState.play();
                }
            });
        }

        item.addEventListener('click', () => {
            if (window.LessonState.currentIndex === index) return;
            // Chỉ load câu khi click vào dòng (không tự phát để tránh văng video nếu đang pause)
            window.LessonState.loadSentence(index);
        });
    });

    updateTranscriptPlayButtons();
    updateTranscriptNextButton();

});
