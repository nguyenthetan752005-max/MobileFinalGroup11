/* [MODULE: SPEAKING UI] XỬ LÝ GIAO DIỆN LUYỆN NÓI.
 * Hiển thị 2 kết quả: Best (điểm cao nhất) và Current (lần mới nhất).
 */

document.addEventListener('DOMContentLoaded', () => {

    // 1. Elements
    const refTextEl = document.getElementById('speakingRefText');
    const hintArea = document.getElementById('hintArea');

    const bestResultArea = document.getElementById('bestResultArea');
    const bestResultBody = document.getElementById('bestResultBody');
    const currentResultArea = document.getElementById('currentResultArea');
    const currentResultBody = document.getElementById('currentResultBody');

    const checkBtn = document.getElementById('checkBtn');
    const skipBtn = document.getElementById('skipBtn');
    const nextBtn2 = document.getElementById('nextBtn2');
    const replayBtn = document.getElementById('replayBtn');

    const recordBtn = document.getElementById('recordBtn');
    const recordLabel = document.querySelector('.speaking-record-label');

    // Mặc định ẩn nút Check vì Speaking dùng RecordBtn để check luôn
    if (checkBtn) checkBtn.style.display = 'none';

    // Biến cho Recording
    let mediaRecorder;
    let isRecording = false;
    let speakingStartTime = Date.now(); // TRACKING

    // 2. State Listeners
    function handleSentenceChange(index, sentence) {
        // Hiển thị câu mẫu
        if (refTextEl) {
            refTextEl.textContent = sentence.content || '';
        }

        // Reset UI
        resetResultAreas();
        if (nextBtn2) nextBtn2.style.display = 'none';
        if (replayBtn) replayBtn.style.display = 'none';
        if (skipBtn) skipBtn.style.display = '';

        // Reset Record button
        isRecording = false;
        if (recordBtn) {
            recordBtn.innerHTML = '<i class="fas fa-microphone"></i>';
            recordBtn.classList.remove('recording');
        }
        if (recordLabel) recordLabel.textContent = 'Tap to speak';

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

        // Load saved results (nếu đã đăng nhập)
        if (window.CURRENT_USER_ID && sentence.id) {
            loadSavedResults(sentence.id);
        }
        
        speakingStartTime = Date.now(); // Reset time for new sentence
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

    if (skipBtn) {
        skipBtn.addEventListener('click', () => {
            // --- SAVE PROGRESS: SKIPPED ---
            const currentSentence = window.LessonState.sentences[window.LessonState.currentIndex];
            if (currentSentence && currentSentence.id) {
                window.LessonCommonUI.saveProgressSkipped(currentSentence.id);
            }

            if (skipBtn) skipBtn.style.display = 'none';
            const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
            if (nextBtn2) nextBtn2.style.display = isLastSentence ? 'none' : '';
            if (replayBtn) replayBtn.style.display = '';
        });
    }

    // --- Xử lý Audio Recording ---
    if (recordBtn) {
        recordBtn.addEventListener('click', async () => {
            if (!isRecording) {
                try {
                    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

                    // Sử dụng RecordRTC thay cho MediaRecorder mặc định
                    mediaRecorder = new RecordRTC(stream, {
                        type: 'audio',
                        mimeType: 'audio/wav',
                        recorderType: RecordRTC.StereoAudioRecorder,
                        desiredSampRate: 16000,
                        numberOfAudioChannels: 1
                    });

                    mediaRecorder.startRecording();
                    isRecording = true;
                    recordBtn.innerHTML = '<i class="fas fa-stop"></i>';
                    recordBtn.classList.add('recording');
                    if (recordLabel) recordLabel.textContent = 'Recording... Tap to stop';

                    if (currentResultBody) {
                        currentResultBody.innerHTML = '<span class="speaking-listening-text">Listening...</span>';
                        currentResultBody.className = 'speaking-result-card-body';
                    }

                } catch (err) {
                    console.error("Lỗi Microphone:", err);
                    alert("Không thể truy cập Microphone! Vui lòng kiểm tra quyền truy cập.");
                }
            } else {
                // Dừng ghi âm
                mediaRecorder.stopRecording(function () {
                    isRecording = false;
                    recordBtn.innerHTML = '<i class="fas fa-microphone"></i>';
                    recordBtn.classList.remove('recording');
                    if (recordLabel) recordLabel.textContent = 'Processing...';

                    let audioBlob = mediaRecorder.getBlob();
                    console.log("Đã tạo file WAV, kích thước:", audioBlob.size, "bytes");

                    sendAudioToBackend(audioBlob);

                    // Tắt mic
                    mediaRecorder.stream.getTracks().forEach(track => track.stop());
                });
            }
        });
    }

    // --- Reset Result Areas ---
    function resetResultAreas() {
        if (bestResultArea) bestResultArea.style.display = 'none';
        if (bestResultBody) bestResultBody.innerHTML = '';
        if (currentResultBody) {
            currentResultBody.innerHTML = 'Your results will appear here';
            currentResultBody.className = 'speaking-result-card-body speaking-result-placeholder';
        }
    }

    // --- Load Saved Results when switching sentences ---
    function loadSavedResults(sentenceId) {
        fetch('/api/speaking/results?sentenceId=' + sentenceId)
            .then(response => response.json())
            .then(data => {
                if (data) {
                    displaySavedResults(data);
                }
            })
            .catch(err => console.error('Error loading saved results:', err));
    }

    function displaySavedResults(data) {
        // Hiển thị Best
        if (data.bestResult) {
            renderBestResult(data.bestResult);
        }

        // Hiển thị Current
        if (data.transcribedText) {
            renderCurrentResult(data);
        }
    }

    // --- Gửi Audio lên Spring Backend ---
    function sendAudioToBackend(audioBlob) {
        // --- TRACK ACTIVE TIME ---
        const durationSeconds = Math.round((Date.now() - speakingStartTime) / 1000);
        if (durationSeconds > 0) {
            fetch('/api/tracking/time', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ durationSeconds: durationSeconds })
            }).catch(e => console.error("Error logging time", e));
        }

        const formData = new FormData();
        formData.append('audio', audioBlob, 'speaking-audio.wav');
        const referenceText = refTextEl ? refTextEl.textContent : '';
        formData.append('referenceText', referenceText);

        // Gửi thêm sentenceId
        const currentSentence = window.LessonState.sentences[window.LessonState.currentIndex];
        if (currentSentence && currentSentence.id) {
            formData.append('sentenceId', currentSentence.id);
        }

        if (currentResultBody) {
            currentResultBody.innerHTML = '<div class="spinner"></div> Evaluating your pronunciation...';
            currentResultBody.className = 'speaking-result-card-body';
        }

        fetch('/api/speaking/evaluate', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) throw new Error("Network response was not ok");
                return response.json();
            })
            .then(data => {
                displayEvaluationResult(data);
            })
            .catch(error => {
                console.error('Error evaluating speaking:', error);
                if (currentResultBody) {
                    currentResultBody.innerHTML = '<span style="color:red">Error evaluating audio. Please try again.</span>';
                }
                if (recordLabel) recordLabel.textContent = 'Tap to speak again';
            });
    }

    // --- Display Evaluation Result (After Recording) ---
    const PASS_THRESHOLD = Number(window.SPEAKING_PASS_THRESHOLD || 70);

    // Score >= PASS_THRESHOLD → COMPLETED, else → IN_PROGRESS (mirrors dictation logic)
    function displayEvaluationResult(data) {
        // Hiển thị Current Result
        renderCurrentResult(data);

        // Hiển thị / Cập nhật Best Result
        if (data.bestResult) {
            renderBestResult(data.bestResult);
        }

        // --- SAVE PROGRESS based on score ---
        const sentenceId = window.LessonState.sentences[window.LessonState.currentIndex]?.id;
        if (sentenceId && data.score != null) {
            if (data.score >= PASS_THRESHOLD) {
                window.LessonCommonUI.saveProgressCompleted(sentenceId);
            } else {
                // Score below pass threshold: mark as IN_PROGRESS
                const userId = window.CURRENT_USER_ID;
                if (userId) {
                    const formData = new URLSearchParams();
                    formData.append('userId', userId);
                    formData.append('sentenceId', sentenceId);
                    fetch('/progress/update', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: formData
                    }).then(() => {
                        window.USER_PROGRESS_MAP[sentenceId] = 'IN_PROGRESS';
                        document.dispatchEvent(new CustomEvent('progress:updated', { detail: { sentenceId, status: 'IN_PROGRESS' } }));
                    }).catch(e => console.error('Failed to update progress:', e));
                }
            }
        }

        if (recordLabel) recordLabel.textContent = 'Tap to try again';
        if (skipBtn) skipBtn.style.display = 'none';
        const isLastSentence = window.LessonState.currentIndex >= window.LessonState.sentences.length - 1;
        if (nextBtn2) nextBtn2.style.display = isLastSentence ? 'none' : '';
        if (replayBtn) replayBtn.style.display = '';

        // Check lesson completion
        window.LessonCommonUI.checkAndDisplayCompletion();
    }

    // --- Render Functions ---
    function renderCurrentResult(data) {
        if (!currentResultBody) return;

        let colorClass = data.score >= 80 ? 'score-success' : (data.score >= 50 ? 'score-warning' : 'score-danger');

        let html = `
            <div class="speaking-result-you-said">
                <strong>You said:</strong> <span>${data.transcribedText || '(Nothing detected)'}</span>
            </div>
            <div class="speaking-result-score ${colorClass}">
                Score: ${data.score}%
            </div>
            <div class="speaking-result-feedback">${data.feedback || ''}</div>
        `;

        // Audio playback nếu có URL
        if (data.audioUrl) {
            html += `
                <div class="speaking-result-audio">
                    <audio controls src="${data.audioUrl}" preload="none"></audio>
                </div>
            `;
        }

        currentResultBody.innerHTML = html;
        currentResultBody.className = 'speaking-result-card-body';
    }

    function renderBestResult(best) {
        if (!bestResultBody || !bestResultArea) return;

        let colorClass = best.score >= 80 ? 'score-success' : (best.score >= 50 ? 'score-warning' : 'score-danger');

        let html = `
            <div class="speaking-result-you-said">
                <strong>You said:</strong> <span>${best.transcribedText || '(Nothing detected)'}</span>
            </div>
            <div class="speaking-result-score ${colorClass}">
                Score: ${best.score}%
            </div>
            <div class="speaking-result-feedback">${best.feedback || ''}</div>
        `;

        if (best.audioUrl) {
            html += `
                <div class="speaking-result-audio">
                    <audio controls src="${best.audioUrl}" preload="none"></audio>
                </div>
            `;
        }

        bestResultBody.innerHTML = html;
        bestResultArea.style.display = '';
    }

    // 4. Proactive Init
    if (window.LessonState && window.LessonState.sentences && window.LessonState.sentences.length > 0) {
        const curIdx = window.LessonState.currentIndex || 0;
        handleSentenceChange(curIdx, window.LessonState.sentences[curIdx]);
    }
});
