// Reset Password JS Logic
const resetPasswordForm = document.getElementById('resetPasswordForm');
if (resetPasswordForm) {
    resetPasswordForm.addEventListener('submit', function(e) {
        var password = document.getElementById('password').value;
        var confirmPassword = document.getElementById('confirmPassword').value;
        var mismatchAlert = document.getElementById('passwordMismatch');

        if (password !== confirmPassword) {
            e.preventDefault();
            mismatchAlert.style.display = 'block';
        } else {
            mismatchAlert.style.display = 'none';
        }
    });
}
