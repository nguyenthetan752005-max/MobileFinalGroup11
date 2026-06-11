/* [SHARED MODULE: LESSON UI] 
 * Xử lý các logic UI dùng chung cho Dictation và Speaking.
 */

window.LessonCommonUI = {
    isLastSentence: function() {
        if (!window.LessonState || !window.LessonState.sentences) return false;
        return window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
    },

    isLessonFullyCompleted: function() {
        if (!window.LessonState || !window.LessonState.sentences) return false;
        const sentences = window.LessonState.sentences;
        const progressMap = window.USER_PROGRESS_MAP || {};
        
        for (let i = 0; i < sentences.length; i++) {
            const sentenceId = sentences[i].id;
            const status = progressMap[sentenceId];
            if (status !== 'COMPLETED') {
                return false;
            }
        }
        return true;
    },

    showLessonCompletion: function() {
        const completionScreen = document.getElementById('lessonCompletionScreen');
        if (completionScreen) {
            completionScreen.style.display = 'flex';
        }
    },

    hideLessonCompletion: function() {
        const completionScreen = document.getElementById('lessonCompletionScreen');
        if (completionScreen) {
            completionScreen.style.display = 'none';
        }
    },

    saveProgressCompleted: function(sentenceId, onSuccess) {
        const userId = window.CURRENT_USER_ID;
        if (userId && sentenceId) {
            const formData = new URLSearchParams();
            formData.append('userId', userId);
            formData.append('sentenceId', sentenceId);

            fetch('/progress/complete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            })
            .then(r => r.json())
            .then(data => {
                window.USER_PROGRESS_MAP[sentenceId] = 'COMPLETED';
                if (typeof window.updateStatusBadge === 'function') {
                    window.updateStatusBadge(sentenceId);
                } else {
                    document.dispatchEvent(new CustomEvent('progress:updated', { detail: { sentenceId, status: 'COMPLETED' } }));
                }
                if (onSuccess) onSuccess(data);
            })
            .catch(err => console.error("Failed to save progress:", err));
        }
    },

    saveProgressSkipped: function(sentenceId, onSuccess) {
        const userId = window.CURRENT_USER_ID;
        if (userId && sentenceId) {
            const formData = new URLSearchParams();
            formData.append('userId', userId);
            formData.append('sentenceId', sentenceId);

            fetch('/progress/skip', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            })
            .then(r => r.json())
            .then(data => {
                window.USER_PROGRESS_MAP[sentenceId] = 'SKIPPED';
                document.dispatchEvent(new CustomEvent('progress:updated', { 
                    detail: { sentenceId: sentenceId, status: 'SKIPPED' } 
                }));
                if (onSuccess) onSuccess(data);
            })
            .catch(err => console.error("Failed to save progress on skip:", err));
        }
    },

    checkAndDisplayCompletion: function() {
        if (this.isLastSentence() && this.isLessonFullyCompleted()) {
            setTimeout(() => {
                this.showLessonCompletion();
            }, 1500); // Delay 1.5s để user thấy feedback trước
        }
    },

    initCommonEvents: function() {
        const repeatLessonBtn = document.getElementById('repeatLessonBtn');
        if (repeatLessonBtn) {
            repeatLessonBtn.addEventListener('click', () => {
                this.hideLessonCompletion();
                window.LessonState.loadSentence(0);
            });
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    window.LessonCommonUI.initCommonEvents();
});
