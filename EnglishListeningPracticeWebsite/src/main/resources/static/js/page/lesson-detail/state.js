/* [MODULE 1: TRÁI TIM HỆ THỐNG] QUẢN LÝ BIẾN TOÀN CỤC & AUDIO/VIDEO.
 * - Khởi tạo <audio> hoặc YouTube Player tùy theo LESSON_TYPE
 * - Quản lý Play, Pause, Chuyển câu 
 * - Dùng event-driven (phát tín hiệu) tới các UI khác thay vì dính chặt code 
 */

// --- 1. Global State Definition (Available immediately for other scripts) ---
const sentencesList = (typeof SENTENCES !== 'undefined') ? SENTENCES : [];
sentencesList.sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0));

// Merge derived audioUrl from SENTENCE_AUDIO_URLS map (built server-side from folder_name + file_name + imagekit endpoint)
if (typeof SENTENCE_AUDIO_URLS !== 'undefined' && SENTENCE_AUDIO_URLS) {
    sentencesList.forEach(s => {
        if (SENTENCE_AUDIO_URLS[s.id]) {
            s.audioUrl = SENTENCE_AUDIO_URLS[s.id];
        }
    });
}

const isVideo = (typeof LESSON_TYPE !== 'undefined') && LESSON_TYPE === 'VIDEO';

// Get initial sentence index from URL parameter (passed from backend)
const initialIndex = (typeof INITIAL_SENTENCE_INDEX !== 'undefined') ? INITIAL_SENTENCE_INDEX : 0;

window.LessonState = {
    sentences: sentencesList,
    currentIndex: initialIndex,
    isPlaying: false,
    isVideo: isVideo,
    playSvg: '<svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><polygon points="5,3 19,12 5,21"></polygon></svg>',
    pauseSvg: '<svg viewBox="0 0 24 24" width="16" height="16" fill="#007bff"><rect x="3" y="14" width="3" height="7"/><rect x="10" y="6" width="3" height="15"/><rect x="17" y="10" width="3" height="11"/></svg>',

    formatTime: function (seconds) {
        if (isNaN(seconds)) return '0:00';
        const m = Math.floor(seconds / 60);
        const s = Math.floor(seconds % 60);
        return m + ':' + (s < 10 ? '0' : '') + s;
    },

    loadSentence: function (index) {
        if (index < 0 || index >= this.sentences.length) return;
        this.currentIndex = index;
        const sentence = this.sentences[this.currentIndex];

        const audio = document.getElementById('mainAudio');
        if (!this.isVideo && audio) {
            audio.src = sentence.audioUrl || '';
            audio.load();
        }

        document.dispatchEvent(new CustomEvent('lesson:sentenceChanged', {
            detail: { index: this.currentIndex, sentence: sentence }
        }));
    },

    togglePlay: function () {
        if (this.isVideo) {
            document.dispatchEvent(new CustomEvent('video:togglePlay'));
            return;
        }
        const audio = document.getElementById('mainAudio');
        if (!audio || !audio.src) return;
        if (audio.paused) {
            audio.play();
        } else {
            audio.pause();
        }
    },

    play: function () {
        if (this.isVideo) {
            document.dispatchEvent(new CustomEvent('video:play'));
            return;
        }
        const audio = document.getElementById('mainAudio');
        if (audio && audio.src) audio.play();
    },
    pause: function () {
        if (this.isVideo) {
            document.dispatchEvent(new CustomEvent('video:pause'));
            return;
        }
        const audio = document.getElementById('mainAudio');
        if (audio) audio.pause();
    },

    next: function () { this.loadSentence(this.currentIndex + 1); },
    prev: function () { this.loadSentence(this.currentIndex - 1); },

    setSpeed: function (speed) {
        if (this.isVideo) {
            document.dispatchEvent(new CustomEvent('video:setSpeed', { detail: { speed: parseFloat(speed) } }));
            return;
        }
        const audio = document.getElementById('mainAudio');
        if (audio) audio.playbackRate = parseFloat(speed);
    },
    toggleMute: function () {
        if (this.isVideo) {
            document.dispatchEvent(new CustomEvent('video:toggleMute'));
            return;
        }
        const audio = document.getElementById('mainAudio');
        if (audio) {
            audio.muted = !audio.muted;
            document.dispatchEvent(new CustomEvent('lesson:muteChanged', { detail: { muted: audio.muted } }));
        }
    },
    seekRatio: function (ratio) {
        if (this.isVideo) {
            document.dispatchEvent(new CustomEvent('video:seekRatio', { detail: { ratio: ratio } }));
            return;
        }
        const audio = document.getElementById('mainAudio');
        if (audio && audio.duration) audio.currentTime = ratio * audio.duration;
    }
};

// --- 2. Event Listeners & Initialization ---
document.addEventListener('DOMContentLoaded', () => {

    const audio = document.getElementById('mainAudio');

    // --- Audio Event Listeners (chỉ khi là AUDIO lesson) ---
    if (!window.LessonState.isVideo && audio) {
        audio.addEventListener('play', () => {
            window.LessonState.isPlaying = true;
            document.dispatchEvent(new CustomEvent('lesson:playState', { detail: { isPlaying: true } }));
        });

        audio.addEventListener('pause', () => {
            window.LessonState.isPlaying = false;
            document.dispatchEvent(new CustomEvent('lesson:playState', { detail: { isPlaying: false } }));
        });

        audio.addEventListener('timeupdate', () => {
            if (audio.duration) {
                document.dispatchEvent(new CustomEvent('lesson:timeUpdate', {
                    detail: {
                        currentTime: audio.currentTime,
                        duration: audio.duration,
                        percent: (audio.currentTime / audio.duration) * 100,
                        timeStr: window.LessonState.formatTime(audio.currentTime) + ' / ' + window.LessonState.formatTime(audio.duration)
                    }
                }));
            }
        });

        audio.addEventListener('ended', () => {
            document.dispatchEvent(new CustomEvent('lesson:ended'));
        });
    }

    // Init first sentence
    if (window.LessonState.sentences.length > 0) {
        // Use INITIAL_SENTENCE_INDEX if provided, otherwise default to 0
        const startIndex = (typeof INITIAL_SENTENCE_INDEX !== 'undefined') ? INITIAL_SENTENCE_INDEX : 0;
        const validIndex = Math.max(0, Math.min(startIndex, window.LessonState.sentences.length - 1));
        
        // Delay 1 chút để các listener ở UI scripts kịp đăng ký
        setTimeout(() => {
            window.LessonState.loadSentence(validIndex);
        }, 50);
    }
});
