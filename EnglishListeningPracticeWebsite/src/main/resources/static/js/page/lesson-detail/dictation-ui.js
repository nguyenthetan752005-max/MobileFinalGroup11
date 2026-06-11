/* [MODULE 2: GIAO DIỆN CHÉP CHÍNH TẢ] XỬ LÝ TAB DICTATION.
 * - Gọi API Backend /api/dictation/check để chấm điểm từng từ.
 */

document.addEventListener('DOMContentLoaded', () => {

    // 1. Elements
    const inputEl = document.getElementById('dictationInput');
    const feedbackEl = document.getElementById('feedbackArea');
    const pronunciationArea = document.getElementById('pronunciationArea');
    const pronunciationText = document.getElementById('pronunciationText');
    const hintArea = document.getElementById('hintArea');
    
    const checkBtn = document.getElementById('checkBtn');
    const skipBtn = document.getElementById('skipBtn');
    const nextBtn2 = document.getElementById('nextBtn2');
    const replayBtn = document.getElementById('replayBtn');

    // Lesson completion elements
    const completionScreen = document.getElementById('lessonCompletionScreen');
    const repeatLessonBtn = document.getElementById('repeatLessonBtn');
    
    let dictationStartTime = Date.now(); // TRACKING

    // 2. Helper Functions
    // Shared utilities are in LessonCommonUI

    // 3. State Listeners
    function handleSentenceChange(index, sentence) {
        // Reset Inputs & UI
        if (inputEl) {
            inputEl.value = '';
            inputEl.disabled = false;
        }
        if (feedbackEl) {
            feedbackEl.style.display = 'none';
            feedbackEl.innerHTML = '';
            feedbackEl.className = 'feedback-area';
        }
        const commentsSec = document.getElementById('lessonComments');
        if (commentsSec) commentsSec.style.display = 'none';
        
        if (pronunciationArea) pronunciationArea.style.display = 'none';
        if (nextBtn2) nextBtn2.style.display = 'none';
        if (replayBtn) replayBtn.style.display = 'none';
        if (checkBtn) checkBtn.style.display = '';
        if (skipBtn) skipBtn.style.display = '';

        // Hiển thị Proper Noun Hint từ Backend
        if (hintArea) {
            const nouns = sentence.properNouns || [];
            if (nouns.length > 0) {
                hintArea.innerHTML = '💡 <strong>Hint:</strong> ' + nouns.map(n => '<span class="hint-proper-noun">' + n + '</span>').join(', ');
                hintArea.style.display = 'block';
            } else {
                hintArea.style.display = 'none';
            }
        }
        
        dictationStartTime = Date.now(); // Reset time for new sentence
    }

    document.addEventListener('lesson:sentenceChanged', (e) => {
        handleSentenceChange(e.detail.index, e.detail.sentence);
    });

    // 3. UI Event Bindings
    if (nextBtn2) {
        nextBtn2.addEventListener('click', () => {
            if (window.LessonState.currentIndex < window.LessonState.sentences.length - 1) {
                window.LessonState.next();
            }
        });
    }

    if (replayBtn) {
        replayBtn.addEventListener('click', () => {
            window.LessonState.loadSentence(window.LessonState.currentIndex);
            window.LessonState.play();
        });
    }

    // --- Logic Chấm điểm ---
    function renderHint(hintWords, newHintIndex) {
        return hintWords.map((word, i) => {
            if (word === '***') {
                return '<span class="hint-hidden">***</span>';
            } else if (i === newHintIndex) {
                return '<span class="hint-new">' + word + '</span>';
            } else {
                return '<span class="hint-matched">' + word + '</span>';
            }
        }).join(' ');
    }

    function showCompleted(result, statusClass, statusHtml, shouldSaveProgress = true) {
        feedbackEl.style.display = 'block';
        feedbackEl.className = 'feedback-area ' + statusClass;
        feedbackEl.innerHTML = statusHtml;

        if (result.correctSentence && pronunciationArea && pronunciationText) {
            pronunciationArea.style.display = 'block';
            const words = result.correctSentence.trim().split(/\s+/);
            pronunciationText.innerHTML = words.map(w => '<span class="pron-word">' + w + '</span>').join(' ');
        }

        if (inputEl) inputEl.disabled = true;
        if (checkBtn) checkBtn.style.display = 'none';
        if (skipBtn) skipBtn.style.display = 'none';
        
        // Check if this is the last sentence
        const lastSentence = window.LessonCommonUI.isLastSentence();
        if (nextBtn2) {
            nextBtn2.style.display = lastSentence ? 'none' : '';
        }
        if (replayBtn) replayBtn.style.display = '';

        const commentsSec = document.getElementById('lessonComments');
        if (commentsSec && result.correct) {
            commentsSec.style.display = 'block';
        }

        // --- CHỈ HIỆN COMPLETION SCREEN NẾU LESSON ĐÃ HOÀN THÀNH TOÀN BỘ ---
        window.LessonCommonUI.checkAndDisplayCompletion();

        // --- LƯU TIẾN ĐỘ NGƯỜI DÙNG (chỉ khi shouldSaveProgress = true) ---
        if (shouldSaveProgress) {
            const sentenceId = window.LessonState.sentences[window.LessonState.currentIndex].id;
            window.LessonCommonUI.saveProgressCompleted(sentenceId);
        }
    }

    if (checkBtn && inputEl) {
        checkBtn.addEventListener('click', async () => {
            const userInput = inputEl.value.trim();
            if (!userInput) return;
            const sentenceId = window.LessonState.sentences[window.LessonState.currentIndex].id;
            try {
                const response = await fetch('/api/dictation/check', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ sentenceId: sentenceId, userInput: userInput })
                });
                if (!response.ok) throw new Error('API error');
                const result = await response.json();
                if (result.correct) {
                    // --- TRACK ACTIVE TIME ---
                    const durationSeconds = Math.round((Date.now() - dictationStartTime) / 1000);
                    if (durationSeconds > 0) {
                        fetch('/api/tracking/time', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ durationSeconds: durationSeconds })
                        }).catch(e => console.error("Error logging time", e));
                    }

                    inputEl.value = result.correctSentence;
                    showCompleted(result, 'feedback-correct', '✅ You are correct!');
                } else {
                    feedbackEl.style.display = 'block';
                    feedbackEl.className = 'feedback-area feedback-incorrect';
                    feedbackEl.innerHTML = '<div class="feedback-status"><svg viewBox="0 0 24 24" width="20" height="20" fill="#ea580c" style="margin-bottom: 2px;"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"></path></svg> Incorrect</div>' +
                        '<div class="feedback-hint">' + renderHint(result.hintWords, result.newHintIndex) + '</div>';
                }
            } catch (err) {
                console.error('Dictation check failed:', err);
            }
        });
    }

    if (skipBtn) {
        skipBtn.addEventListener('click', () => {
            // --- TRACK ACTIVE TIME ---
            const durationSeconds = Math.round((Date.now() - dictationStartTime) / 1000);
            if (durationSeconds > 0) {
                fetch('/api/tracking/time', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ durationSeconds: durationSeconds })
                }).catch(e => console.error("Error logging time", e));
            }

            const currentSentence = window.LessonState.sentences[window.LessonState.currentIndex];
            if (currentSentence) {
                // --- LƯU TIẾN ĐỘ SKIPPED ---
                window.LessonCommonUI.saveProgressSkipped(currentSentence.id);

                showCompleted({ correct: false, correctSentence: currentSentence.content }, 'feedback-skipped', 
                    '<strong>Answer:</strong> ' + currentSentence.content, false);
            }
        });
    }

    // 4. Repeat Lesson Button Handler is in LessonCommonUI.

    // 5. Proactive Init
    if (window.LessonState && window.LessonState.sentences.length > 0) {
        const curIdx = window.LessonState.currentIndex;
        handleSentenceChange(curIdx, window.LessonState.sentences[curIdx]);
    }
});