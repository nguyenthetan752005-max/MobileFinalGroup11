// Profile JS Logic
function triggerAvatarPicker() {
    if (avatarInput) {
        avatarInput.click();
    }
}

function toggleEditName() {
    var displayBox = document.getElementById('nameDisplayBox');
    var editForm = document.getElementById('editNameForm');
    if (displayBox.style.display === 'none') {
        displayBox.style.display = 'flex';
        editForm.style.display = 'none';
    } else {
        displayBox.style.display = 'none';
        editForm.style.display = 'flex';
    }
}

// Cropper Logic
let cropper;
const avatarInput = document.getElementById('avatarInput');
const cropperModal = document.getElementById('cropperModal');
const imageToCrop = document.getElementById('imageToCrop');
const avatarUploadEndpoint = document.body?.dataset?.uploadEndpoint || '/profile/update-avatar';

if (avatarInput) {
    avatarInput.addEventListener('change', function (e) {
        const files = e.target.files;
        if (files && files.length > 0) {
            const reader = new FileReader();
            reader.onload = function (event) {
                imageToCrop.src = event.target.result;
                cropperModal.style.display = 'flex';
                if (cropper) {
                    cropper.destroy();
                }
                cropper = new Cropper(imageToCrop, {
                    aspectRatio: 1,      // Cố định tỉ lệ 1:1 cho hình tròn
                    viewMode: 1,         // Không cho phép vùng chọn vượt ra ngoài ảnh
                    dragMode: 'move',    // Cho phép kéo ảnh để căn chỉnh
                    autoCropArea: 0.8,   // Vùng chọn mặc định chiếm 80% ảnh
                    restore: false,
                    guides: false,       // Tắt lưới kẻ
                    center: true,        // Hiển thị tâm
                    highlight: false,    // Tắt hiệu ứng highlight mặc định
                    cropBoxMovable: false,    // Giữ cố định khung tròn
                    cropBoxResizable: false,  // Không cho bóp méo khung tròn
                    toggleDragModeOnDblclick: false,
                    zoomable: true,      // BẬT tính năng zoom
                    wheelZoomRatio: 0.1  // Tốc độ zoom khi cuộn chuột
                });
            };
            reader.readAsDataURL(files[0]);
        }
    });
}

function closeCropper() {
    if (cropperModal) {
        cropperModal.style.display = 'none';
    }
    if (avatarInput) {
        avatarInput.value = '';
    }
    if (cropper) {
        cropper.destroy();
        cropper = null;
    }
}

function uploadAvatar() {
    if (!cropper) return;
    const uploadBtn = document.getElementById('uploadBtn');
    uploadBtn.innerText = 'Đang tải lên...';
    uploadBtn.disabled = true;

    // Lấy ảnh từ canvas với kích thước chuẩn
    cropper.getCroppedCanvas({
        width: 400,
        height: 400,
        imageSmoothingEnabled: true,
        imageSmoothingQuality: 'high',
    }).toBlob(function (blob) {
        const formData = new FormData();
        formData.append('avatar', blob, 'avatar.png');

        fetch(avatarUploadEndpoint, {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    location.reload();
                } else {
                    alert('Lỗi: ' + data.message);
                    uploadBtn.innerText = 'Lưu';
                    uploadBtn.disabled = false;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Đã xảy ra lỗi khi tải lên ảnh.');
                uploadBtn.innerText = 'Lưu';
                uploadBtn.disabled = false;
            });
    }, 'image/png');
}
