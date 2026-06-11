/* [MODULE 6: VIDEO ENGINE] XỬ LÝ YOUTUBE IFRAME PLAYER.
 * - Chỉ hoạt động khi LESSON_TYPE === 'VIDEO'
 * - Dùng 1 YouTube player duy nhất, di chuyển DOM giữa Dictation ↔ Transcript tab
 * - Start Dictation flow, Video size, Hide video
 * - Play/Pause button trên dictation panel
 * - seekTo(startTime) khi sentenceChanged, auto-pause tại endTime
 */

(function () {
    if (typeof LESSON_TYPE === 'undefined' || LESSON_TYPE !== 'VIDEO') return;
    if (typeof YOUTUBE_VIDEO_ID === 'undefined' || !YOUTUBE_VIDEO_ID) return;

    let player = null;
    let timeUpdateInterval = null;
    let currentSentenceEndTime = null;
    let isMuted = false;
    let isPlayerReady = false;
    let dictationStarted = false;
    let syncingSentenceFromPlayback = false;

    function isTranscriptTabActive() {
        const transcriptTab = document.getElementById('transcript-tab');
        return !!(transcriptTab && transcriptTab.classList.contains('active'));
    }

    function findSentenceIndexByTime(currentTime) {
        const sentences = (window.LessonState && window.LessonState.sentences) ? window.LessonState.sentences : [];
        if (!sentences.length) return -1;

        // Pick the sentence where startTime <= currentTime < nextStartTime.
        for (let i = 0; i < sentences.length; i++) {
            const sentence = sentences[i];
            if (sentence.startTime == null) continue;

            const nextSentence = sentences[i + 1];
            const nextStart = nextSentence && nextSentence.startTime != null ? nextSentence.startTime : Number.POSITIVE_INFINITY;

            if (currentTime >= sentence.startTime && currentTime < nextStart) {
                return i;
            }
        }

        // If playback time is before the first timed sentence, keep index 0.
        const firstTimed = sentences.findIndex(s => s.startTime != null);
        if (firstTimed !== -1 && currentTime < sentences[firstTimed].startTime) {
            return firstTimed;
        }

        // If playback time is after all timed sentences, keep the last timed sentence.
        for (let i = sentences.length - 1; i >= 0; i--) {
            if (sentences[i].startTime != null) return i;
        }

        return -1;
    }

    function syncSentenceByPlaybackTime(currentTime) {
        if (!window.LessonState) return;
        const indexByTime = findSentenceIndexByTime(currentTime);
        if (indexByTime < 0 || indexByTime === window.LessonState.currentIndex) return;

        console.warn(`[!. ENGINE WARNING] ⚠️ Tự động đồng bộ UI từ câu ${window.LessonState.currentIndex} sang câu ${indexByTime} do thời gian video đang là ${currentTime}s`);

        const sentence = window.LessonState.sentences[indexByTime];
        if (!sentence) return;

        window.LessonState.currentIndex = indexByTime;
        syncingSentenceFromPlayback = true;
        document.dispatchEvent(new CustomEvent('lesson:sentenceChanged', {
            detail: { index: indexByTime, sentence: sentence }
        }));
    }

    window.onYouTubeIframeAPIReady = function () {
        player = new YT.Player('youtubePlayer', {
            videoId: YOUTUBE_VIDEO_ID,
            height: '100%',
            width: '100%',
            playerVars: {
                'autoplay': 0,
                'controls': 1,
                'rel': 0,
                'modestbranding': 1,
                'playsinline': 1,
                'enablejsapi': 1,
                'origin': window.location.origin
            },
            events: {
                'onReady': onPlayerReady,
                'onStateChange': onPlayerStateChange
            }
        });
    };

    function onPlayerReady() {
        isPlayerReady = true;
        console.log('YouTube Player ready.');
        
        // Master polling loop to guarantee sync even if `onPlayerStateChange` events are blocked
        // by Chrome's generic localhost cross-origin CORS policy.
        setInterval(() => {
            if (!player || !isPlayerReady) return;
            
            let state;
            try {
                state = player.getPlayerState();
            } catch(e) { return; }

            const wasPlaying = window.LessonState && window.LessonState.isPlaying;
            const isPlayingNow = (state === YT.PlayerState.PLAYING);
            
            if (isPlayingNow !== wasPlaying) {
                if (window.LessonState) {
                    window.LessonState.isPlaying = isPlayingNow;
                }
                document.dispatchEvent(new CustomEvent('lesson:playState', { detail: { isPlaying: isPlayingNow } }));
                
                if (isPlayingNow) {
                    startTimeTracking();
                } else {
                    stopTimeTracking();
                    if (state === YT.PlayerState.ENDED) {
                        document.dispatchEvent(new CustomEvent('lesson:ended'));
                    }
                }
            }
        }, 150);
    }

    function refreshPlayerRendering() {
        if (!player || !isPlayerReady) return;

        let state;
        try { state = player.getPlayerState(); } catch(e) { state = -1; }
        const wasPlaying = state === YT.PlayerState.PLAYING;
        const currentTime = player.getCurrentTime();

        try {
            player.setSize('100%', '100%');
        } catch (err) {
            console.warn('player.setSize failed:', err);
        }

        // Seek to current time to force frame redraw after DOM/container changes.
        if (!isNaN(currentTime) && currentTime > 0) {
            player.seekTo(currentTime, true);
        }

        if (!wasPlaying) {
            setTimeout(() => {
                if (!player || !isPlayerReady) return;
                try {
                    if (player.getPlayerState() === YT.PlayerState.PLAYING) {
                        player.pauseVideo();
                    }
                } catch(e) {}
            }, 80);
        }
    }

    function onPlayerStateChange(event) {
        // Disabled out of necessity. Fallback polling loop handles this to bypass CORS blocking.
    }

    function startTimeTracking() {
        stopTimeTracking();
        timeUpdateInterval = setInterval(() => {
            if (!player || !isPlayerReady) return;
            const currentTime = player.getCurrentTime();
            const duration = player.getDuration();

            // We only stop at endTime if:
            // 1. We are in Dictation tab (and dictation has started)
            // 2. OR we are in Transcript tab and the Repeat checkbox is checked.
            let shouldStop = false;
            let shouldRepeat = false;

            if (isTranscriptTabActive()) {
                const repeatCheckbox = document.getElementById('repeatCheckbox');
                if (repeatCheckbox && repeatCheckbox.checked) {
                    shouldStop = true;
                    shouldRepeat = true;
                }
            } else {
                if (dictationStarted) {
                    shouldStop = true;
                }
            }

            // Absolute source of truth for boundaries:
            if (window.LessonState && window.LessonState.sentences && window.LessonState.sentences.length > 0) {
                const currentSentence = window.LessonState.sentences[window.LessonState.currentIndex];
                if (currentSentence && currentSentence.endTime != null) {
                    if (shouldStop && currentTime >= currentSentence.endTime) {
                        if (window.lastRepeatSeekTime && Date.now() - window.lastRepeatSeekTime < 1000) {
                            return; 
                        }

                        if (shouldRepeat && currentSentence.startTime != null && currentTime < currentSentence.endTime + 1.0) {
                            window.lastRepeatSeekTime = Date.now();
                            player.seekTo(currentSentence.startTime, true);
                            if (player.getPlayerState() !== YT.PlayerState.PLAYING) {
                                player.playVideo();
                            }
                            return;
                        } else if (!shouldRepeat || currentTime >= currentSentence.endTime + 1.0) {
                            // If it drifted way past endTime (e.g user dragged seek bar far ahead), we don't snap them back infinitely.
                            // But if they didn't drag it, we pause if dictation mode
                            if (!shouldRepeat) {
                                player.pauseVideo();
                                document.dispatchEvent(new CustomEvent('lesson:ended'));
                                return;
                            }
                        }
                    }
                }
            }

            // Follow transcript by playback time when not dictating, or when in transcript tab
            if (!dictationStarted || isTranscriptTabActive()) {
                syncSentenceByPlaybackTime(currentTime);
            }

            if (duration > 0) {
                document.dispatchEvent(new CustomEvent('lesson:timeUpdate', {
                    detail: {
                        currentTime: currentTime,
                        duration: duration,
                        percent: (currentTime / duration) * 100,
                        timeStr: window.LessonState.formatTime(currentTime) + ' / ' + window.LessonState.formatTime(duration)
                    }
                }));
            }
        }, 250);
    }

    function stopTimeTracking() {
        if (timeUpdateInterval) {
            clearInterval(timeUpdateInterval);
            timeUpdateInterval = null;
        }
    }

    // --- Lắng nghe sự kiện từ state.js ---

    document.addEventListener('lesson:sentenceChanged', (e) => {
        if (!player || !isPlayerReady) return;
        const sentence = e.detail.sentence;
        if (syncingSentenceFromPlayback) {
            syncingSentenceFromPlayback = false;
            currentSentenceEndTime = sentence.endTime != null ? sentence.endTime : null;
            return;
        }
        if (sentence.startTime != null) {
            currentSentenceEndTime = sentence.endTime;
            player.seekTo(sentence.startTime, true);
        }
    });

    document.addEventListener('video:togglePlay', () => {
        if (!player || !isPlayerReady) return;
        const state = player.getPlayerState();
        if (state === YT.PlayerState.PLAYING) {
            player.pauseVideo();
        } else {
            const sentence = window.LessonState.sentences[window.LessonState.currentIndex];
            currentSentenceEndTime = sentence.endTime;
            
            // Nếu video đang dừng lại ở cuối câu (hoặc đã hết video), 
            // bấm Play sẽ phát lại từ đầu câu (giống với audio).
            const currentTime = player.getCurrentTime();
            if (sentence.endTime != null && currentTime >= sentence.endTime - 0.2) {
                if (sentence.startTime != null) {
                    player.seekTo(sentence.startTime, true);
                }
            } else if (state === YT.PlayerState.ENDED) {
                if (sentence.startTime != null) {
                    player.seekTo(sentence.startTime, true);
                } else {
                    player.seekTo(0, true);
                }
            }
            
            player.playVideo();
        }
    });

    document.addEventListener('video:play', () => {
        if (!player || !isPlayerReady) return;
        const sentence = window.LessonState.sentences[window.LessonState.currentIndex];
        currentSentenceEndTime = sentence.endTime;
        if (sentence.startTime != null) {
            player.seekTo(sentence.startTime, true);
        }
        player.playVideo();
    });

    document.addEventListener('video:pause', () => {
        if (!player || !isPlayerReady) return;
        player.pauseVideo();
    });

    document.addEventListener('video:setSpeed', (e) => {
        if (!player || !isPlayerReady) return;
        player.setPlaybackRate(e.detail.speed);
    });

    document.addEventListener('video:toggleMute', () => {
        if (!player || !isPlayerReady) return;
        isMuted = !isMuted;
        if (isMuted) { player.mute(); } else { player.unMute(); }
        document.dispatchEvent(new CustomEvent('lesson:muteChanged', { detail: { muted: isMuted } }));
    });

    document.addEventListener('video:seekRatio', (e) => {
        if (!player || !isPlayerReady) return;
        const duration = player.getDuration();
        if (duration > 0) {
            player.seekTo(e.detail.ratio * duration, true);
        }
    });

    // Custom event: seekTo a specific time (used by transcript-ui)
    document.addEventListener('video:seekToTime', (e) => {
        if (!player || !isPlayerReady) return;
        currentSentenceEndTime = e.detail.endTime || null;
        player.seekTo(e.detail.startTime, true);
        player.playVideo();
    });

    document.addEventListener('video:startDictationMode', () => {
        dictationStarted = true;
    });

    document.addEventListener('video:movePlayerToContainer', (e) => {
        if (!player || !isPlayerReady) return;
        const containerId = e.detail && e.detail.containerId;
        if (!containerId) return;

        const targetContainer = document.getElementById(containerId);
        if (!targetContainer) return;

        const iframe = player.getIframe();
        if (iframe && iframe.parentElement !== targetContainer) {
            targetContainer.appendChild(iframe);
        }
    });

    document.addEventListener('video:containerUpdated', () => {
        refreshPlayerRendering();
    });
})();
