/* QUẢN LÝ CÁC SỰ KIỆN GIAO DIỆN CHUNG TOÀN CỤC.
 * - Ví dụ: Bật/Tắt chế độ Nền tối (Dark Mode) bằng cách thêm Class vào body 
 */

// ===========================
// Theme Toggle
// ===========================
document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('themeToggle');
    const iconSun = themeToggle?.querySelector('.icon-sun');
    const iconMoon = themeToggle?.querySelector('.icon-moon');

    // Load saved theme (the data-theme attribute is already set by head.html script)
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    updateThemeIcons(currentTheme);

    themeToggle?.addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme');
        const next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
        updateThemeIcons(next);
    });

    function updateThemeIcons(theme) {
        if (!iconSun || !iconMoon) return;
        if (theme === 'dark') {
            iconSun.style.display = 'none';
            iconMoon.style.display = 'block';
        } else {
            iconSun.style.display = 'block';
            iconMoon.style.display = 'none';
        }
    }

    // ===========================
    // Mobile Menu
    // ===========================
    const mobileToggle = document.getElementById('mobileMenuToggle');
    const mobileMenu = document.getElementById('mobileMenu');

    mobileToggle?.addEventListener('click', () => {
        mobileToggle.classList.toggle('active');
        mobileMenu.classList.toggle('active');
    });
});

