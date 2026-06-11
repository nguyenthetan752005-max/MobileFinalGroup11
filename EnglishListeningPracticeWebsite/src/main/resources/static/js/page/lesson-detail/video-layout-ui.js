/* [MODULE: VIDEO LAYOUT UI]
 * - Chỉ xử lý UI/layout cho trang VIDEO dictation
 * - Không chứa logic player lõi (để trong video-engine.js)
 */

(function() {
    if (typeof LESSON_TYPE === 'undefined' || LESSON_TYPE !== 'VIDEO') return;

    document.addEventListener('DOMContentLoaded', () => {
        function notifyVideoContainerUpdated() {
            requestAnimationFrame(() => {
                document.dispatchEvent(new CustomEvent('video:containerUpdated'));
            });
        }

        const startBtn = document.getElementById('startDictationBtn');
        const startPanel = document.getElementById('startDictationPanel');
        const activePanel = document.getElementById('dictationActivePanel');

        if (startBtn && startPanel && activePanel) {
            startBtn.addEventListener('click', () => {
                document.dispatchEvent(new CustomEvent('video:startDictationMode'));
                startPanel.style.display = 'none';
                activePanel.style.display = 'block';
                if (window.LessonState && window.LessonState.sentences.length > 0) {
                    window.LessonState.loadSentence(0);
                }
            });
        }

        const videoDictPlayBtn = document.getElementById('videoDictPlayBtn');
        if (videoDictPlayBtn) {
            videoDictPlayBtn.addEventListener('click', () => {
                document.dispatchEvent(new CustomEvent('video:togglePlay'));
            });

            document.addEventListener('lesson:playState', (e) => {
                videoDictPlayBtn.innerHTML = e.detail.isPlaying
                    ? (window.LessonState ? window.LessonState.pauseSvg : '||')
                    : (window.LessonState ? window.LessonState.playSvg : 'Play');
            });
        }

        const sizeSelect = document.getElementById('videoSizeSelect');
        const videoWrapper = document.getElementById('dictationVideoWrapper');
        const videoSplit = document.querySelector('.video-dictation-split');

        function applyVideoSizeLayout(sizeValue) {
            if (!videoWrapper) return;
            videoWrapper.classList.remove('video-size-small', 'video-size-normal', 'video-size-large');
            videoWrapper.classList.add('video-size-' + sizeValue);

            if (videoSplit) {
                videoSplit.classList.remove('video-layout-small', 'video-layout-normal', 'video-layout-large');
                videoSplit.classList.add('video-layout-' + sizeValue);
            }

            notifyVideoContainerUpdated();
        }

        if (sizeSelect && videoWrapper) {
            sizeSelect.addEventListener('change', () => {
                applyVideoSizeLayout(sizeSelect.value);
            });

            applyVideoSizeLayout(sizeSelect.value || 'normal');
        }

        const hideBtn = document.getElementById('hideVideoBtn');
        const videoLeftCol = document.querySelector('.video-left-column');
        if (hideBtn && videoLeftCol) {
            const showVideoBtn = document.createElement('button');
            showVideoBtn.className = 'hide-video-btn show-video-btn';
            showVideoBtn.textContent = 'Show video';
            showVideoBtn.style.display = 'none';
            showVideoBtn.style.marginBottom = '12px';

            if (videoSplit) {
                videoSplit.insertBefore(showVideoBtn, videoSplit.firstChild);
            }

            let hidden = false;

            function updateVideoVisibility() {
                videoLeftCol.style.display = hidden ? 'none' : '';
                hideBtn.style.display = hidden ? 'none' : '';
                showVideoBtn.style.display = hidden ? 'inline-flex' : 'none';
                if (!hidden) notifyVideoContainerUpdated();
            }

            hideBtn.addEventListener('click', () => {
                hidden = !hidden;
                updateVideoVisibility();
            });

            showVideoBtn.addEventListener('click', () => {
                hidden = false;
                updateVideoVisibility();
            });
        }

        const tabBtns = document.querySelectorAll('.tab-btn');
        if (tabBtns.length) {
            tabBtns.forEach(btn => {
                btn.addEventListener('click', () => {
                    const tabId = btn.getAttribute('data-tab');
                    const targetContainerId = tabId === 'transcript-tab'
                        ? 'transcriptVideoWrapper'
                        : 'dictationVideoWrapper';
                    document.dispatchEvent(new CustomEvent('video:movePlayerToContainer', {
                        detail: { containerId: targetContainerId }
                    }));
                    notifyVideoContainerUpdated();
                });
            });
        }

        window.addEventListener('resize', notifyVideoContainerUpdated);
    });
})();
