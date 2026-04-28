import sys

file_path = r'd:\AndroidFinal3\MobileFinalGroup11\app\src\main\res\values\strings.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

mapping = {
    'name="avatar_picker_title"': '    <string name="avatar_picker_title">Chọn ảnh đại diện</string>\n',
    'name="avatar_option_gallery"': '    <string name="avatar_option_gallery">Chọn từ thư viện</string>\n',
    'name="avatar_camera_permission_denied"': '    <string name="avatar_camera_permission_denied">Bạn cần cấp quyền camera để chụp ảnh.</string>\n',
    'name="avatar_saved"': '    <string name="avatar_saved">Đã lưu ảnh đại diện.</string>\n'
}

for i in range(len(lines)):
    for key, val in mapping.items():
        if key in lines[i]:
            lines[i] = val

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print("Final avatar string replacements complete.")
