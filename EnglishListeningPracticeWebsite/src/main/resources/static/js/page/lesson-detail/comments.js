/* [MODULE: COMMENTS] KẾT NỐI COMMENT SECTION VỚI API BACKEND.
 * - Load comments theo sentenceId khi chuyển câu.
 * - Gửi comment mới, reply, vote like/dislike.
 */

document.addEventListener('DOMContentLoaded', () => {
    const commentsList = document.getElementById('commentsList');
    const commentCount = document.getElementById('commentCount');
    const writeBtn = document.getElementById('writeCommentBtn');
    const commentsSection = document.getElementById('lessonComments');
    const CURRENT_USER_ID = window.CURRENT_USER_ID || null;

    let currentSentenceId = null;

    // Hiển thị comment section khi có sentenceId
    document.addEventListener('lesson:sentenceChanged', (e) => {
        const sentence = e.detail.sentence;
        if (sentence && sentence.id) {
            currentSentenceId = sentence.id;
            loadComments(currentSentenceId);
            if (commentsSection) commentsSection.style.display = '';
        }
    });

    function loadComments(sentenceId) {
        fetch(`/sentence/${sentenceId}/comments`)
            .then(r => r.json())
            .then(comments => {
                renderComments(comments);
            })
            .catch(err => {
                console.error('Error loading comments:', err);
                if (commentsList) commentsList.innerHTML = '<p style="text-align:center;color:#999;padding:20px;">Không thể tải bình luận.</p>';
            });
    }

    function renderComments(comments) {
        if (!commentsList) return;
        if (commentCount) commentCount.textContent = comments.length;

        if (comments.length === 0) {
            commentsList.innerHTML = '<p style="text-align:center;color:var(--text-muted,#6b7280);padding:20px 0;">Chưa có bình luận nào cho câu này. Hãy là người đầu tiên!</p>';
            return;
        }

        commentsList.innerHTML = comments.map(c => renderCommentHtml(c, false)).join('');
        // Load replies for each top-level comment
        comments.forEach(c => {
            loadReplies(c.id);
        });
    }

    function renderCommentHtml(c, isReply) {
        const userName = c.user ? c.user.username : 'Unknown';
        const avatar = c.user && c.user.avatarUrl
            ? `<img src="${c.user.avatarUrl}" class="comment-avatar ${isReply ? 'small' : ''}" alt="Avatar" style="object-fit:cover;">`
            : `<div class="comment-avatar ${isReply ? 'small' : ''}">${userName.charAt(0).toUpperCase()}</div>`;
        const timeAgo = formatTimeAgo(c.createdAt);
        const likes = c.likeCount || 0;
        const dislikes = c.dislikeCount || 0;
        const isOwner = CURRENT_USER_ID && c.user && c.user.id === CURRENT_USER_ID;
        const isDeleted = c.content === 'comment has been deleted by author';

        const contentHtml = isDeleted
            ? `<div class="comment-text" style="font-style:italic;color:var(--text-muted,#999);">comment has been deleted by author</div>`
            : `<div class="comment-text" id="comment-text-${c.id}">${escapeHtml(c.content)}</div>`;

        const actionsHtml = isDeleted ? '' : `
                <div class="comment-actions">
                    <button class="comment-action-btn ${isReply ? 'small' : ''}" onclick="window.CommentModule.vote(${c.id}, true)">
                        <i class="fas fa-thumbs-up"></i> <span id="like-${c.id}">${likes}</span>
                    </button>
                    <button class="comment-action-btn ${isReply ? 'small' : ''}" onclick="window.CommentModule.vote(${c.id}, false)">
                        <i class="fas fa-thumbs-down"></i> <span id="dislike-${c.id}">${dislikes}</span>
                    </button>
                    ${!isReply ? `<button class="comment-action-btn" onclick="window.CommentModule.showReplyForm(${c.id})"><i class="fas fa-reply"></i> Reply</button>` : ''}
                    ${isOwner ? `<button class="comment-action-btn" onclick="window.CommentModule.showEditForm(${c.id})"><i class="fas fa-pencil-alt"></i> Edit</button>` : ''}
                    ${isOwner ? `<button class="comment-action-btn" onclick="window.CommentModule.deleteComment(${c.id})" style="color:#f44336;"><i class="fas fa-trash"></i> Delete</button>` : ''}
                </div>`;

        return `
        <div class="comment-item ${isReply ? 'reply' : ''}" data-comment-id="${c.id}">
            ${avatar}
            <div class="comment-body">
                <div class="comment-meta">
                    <strong>${userName}</strong>
                    <span class="comment-time">${timeAgo}</span>
                </div>
                ${contentHtml}
                ${actionsHtml}
                <div id="edit-form-${c.id}" style="display:none;margin-top:10px;">
                    <textarea id="edit-input-${c.id}" rows="2" style="width:100%;padding:8px;border:1px solid var(--border-color,#ddd);border-radius:6px;resize:vertical;font-family:inherit;background:var(--bg-card,#fff);color:var(--text-primary,#333);"></textarea>
                    <div style="margin-top:6px;display:flex;gap:8px;">
                        <button class="comment-action-btn" style="color:var(--primary);" onclick="window.CommentModule.submitEdit(${c.id})">Lưu</button>
                        <button class="comment-action-btn" onclick="document.getElementById('edit-form-${c.id}').style.display='none';">Hủy</button>
                    </div>
                </div>
                <div id="reply-form-${c.id}" style="display:none;margin-top:10px;">
                    <textarea id="reply-input-${c.id}" placeholder="Viết trả lời..." rows="2" style="width:100%;padding:8px;border:1px solid var(--border-color,#ddd);border-radius:6px;resize:vertical;font-family:inherit;background:var(--bg-card,#fff);color:var(--text-primary,#333);"></textarea>
                    <div style="margin-top:6px;display:flex;gap:8px;">
                        <button class="comment-action-btn" style="color:var(--primary);" onclick="window.CommentModule.submitReply(${c.id})">Gửi</button>
                        <button class="comment-action-btn" onclick="document.getElementById('reply-form-${c.id}').style.display='none';">Hủy</button>
                    </div>
                </div>
                <div id="replies-${c.id}" class="comment-replies"></div>
            </div>
        </div>`;
    }

    function loadReplies(commentId) {
        fetch(`/comment/${commentId}/replies`)
            .then(r => r.json())
            .then(replies => {
                const container = document.getElementById(`replies-${commentId}`);
                if (container && replies.length > 0) {
                    container.innerHTML = replies.map(r => renderCommentHtml(r, true)).join('');
                }
            });
    }

    // === Public API (window.CommentModule) ===
    window.CommentModule = {
        vote(commentId, isLike) {
            fetch(`/comment/${commentId}/vote`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `isLike=${isLike}`
            })
            .then(r => {
                if (r.status === 401) { alert('Bạn cần đăng nhập để vote!'); return; }
                if (r.ok && currentSentenceId) loadComments(currentSentenceId);
            })
            .catch(err => console.error('Error voting:', err));
        },

        showReplyForm(commentId) {
            const form = document.getElementById(`reply-form-${commentId}`);
            if (form) form.style.display = form.style.display === 'none' ? '' : 'none';
        },

        showEditForm(commentId) {
            const form = document.getElementById(`edit-form-${commentId}`);
            const textEl = document.getElementById(`comment-text-${commentId}`);
            const input = document.getElementById(`edit-input-${commentId}`);
            if (form && input && textEl) {
                // Strip '[author edited comment]' suffix for editing
                let currentText = textEl.textContent.replace(/ \[author edited comment\]$/g, '');
                input.value = currentText;
                form.style.display = form.style.display === 'none' ? '' : 'none';
            }
        },

        submitEdit(commentId) {
            const input = document.getElementById(`edit-input-${commentId}`);
            const content = input ? input.value.trim() : '';
            if (!content) return;

            const formData = new URLSearchParams();
            formData.append('content', content);

            fetch(`/comment/${commentId}`, { method: 'PUT', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: formData })
                .then(r => {
                    if (r.status === 401) { alert('Bạn cần đăng nhập!'); return; }
                    if (r.ok && currentSentenceId) loadComments(currentSentenceId);
                })
                .catch(err => console.error('Error editing comment:', err));
        },

        submitReply(parentId) {
            const input = document.getElementById(`reply-input-${parentId}`);
            const content = input ? input.value.trim() : '';
            if (!content || !currentSentenceId) return;

            const formData = new URLSearchParams();
            formData.append('sentenceId', currentSentenceId);
            formData.append('content', content);
            formData.append('parentId', parentId);

            fetch('/comment', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: formData })
                .then(r => {
                    if (r.status === 401) { alert('Bạn cần đăng nhập để bình luận!'); return; }
                    if (r.ok) loadComments(currentSentenceId);
                })
                .catch(err => console.error('Error submitting reply:', err));
        },

        submitComment(content) {
            if (!content || !currentSentenceId) return;
            const formData = new URLSearchParams();
            formData.append('sentenceId', currentSentenceId);
            formData.append('content', content);

            fetch('/comment', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: formData })
                .then(r => {
                    if (r.status === 401) { alert('Bạn cần đăng nhập để bình luận!'); return; }
                    if (r.ok) {
                        const form = document.getElementById('newCommentForm');
                        if (form) form.remove();
                        loadComments(currentSentenceId);
                    }
                })
                .catch(err => console.error('Error submitting comment:', err));
        },

        deleteComment(commentId) {
            if (!confirm('Bạn có chắc muốn xóa bình luận này?')) return;
            fetch(`/comment/${commentId}`, { method: 'DELETE' })
                .then(r => {
                    if (r.status === 401) { alert('Bạn cần đăng nhập!'); return; }
                    if (r.ok && currentSentenceId) loadComments(currentSentenceId);
                })
                .catch(err => console.error('Error deleting comment:', err));
        }
    };

    // Write Comment button
    if (writeBtn) {
        writeBtn.addEventListener('click', () => {
            let existing = document.getElementById('newCommentForm');
            if (existing) { existing.remove(); return; }
            const form = document.createElement('div');
            form.id = 'newCommentForm';
            form.style.marginTop = '12px';
            form.innerHTML = `
                <textarea id="newCommentInput" placeholder="Viết bình luận..." rows="3" style="width:100%;padding:10px;border:1px solid var(--border-color,#ddd);border-radius:8px;resize:vertical;font-family:inherit;background:var(--bg-card,#fff);color:var(--text-primary,#333);"></textarea>
                <div style="margin-top:8px;display:flex;gap:8px;">
                    <button onclick="const t=document.getElementById('newCommentInput');window.CommentModule.submitComment(t.value.trim());t.value='';" style="padding:8px 16px;background:var(--primary,#e67300);color:white;border:none;border-radius:6px;cursor:pointer;font-weight:600;">Gửi</button>
                    <button onclick="document.getElementById('newCommentForm').remove();" style="padding:8px 16px;background:var(--bg-card,#eee);border:1px solid var(--border-color,#ddd);border-radius:6px;cursor:pointer;">Hủy</button>
                </div>`;
            writeBtn.parentNode.insertBefore(form, writeBtn.nextSibling);
        });
    }

    // Helpers
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function formatTimeAgo(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const now = new Date();
        const diffMs = now - date;
        const diffMin = Math.floor(diffMs / 60000);
        if (diffMin < 1) return 'Vừa xong';
        if (diffMin < 60) return `${diffMin} phút trước`;
        const diffHrs = Math.floor(diffMin / 60);
        if (diffHrs < 24) return `${diffHrs} giờ trước`;
        const diffDays = Math.floor(diffHrs / 24);
        if (diffDays < 30) return `${diffDays} ngày trước`;
        return date.toLocaleDateString('vi-VN');
    }

    // Init if already loaded
    if (window.LessonState && window.LessonState.sentences && window.LessonState.sentences.length > 0) {
        const idx = window.LessonState.currentIndex || 0;
        const s = window.LessonState.sentences[idx];
        if (s && s.id) {
            currentSentenceId = s.id;
            loadComments(s.id);
            if (commentsSection) commentsSection.style.display = '';
        }
    }
});
