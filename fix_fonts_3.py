import sys

file_path = r'd:\AndroidFinal3\MobileFinalGroup11\app\src\main\res\values\strings.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

import re

# Line 196:
for i in range(len(lines)):
    if 'name="profile_longest_streak"' in lines[i]:
        lines[i] = '    <string name="profile_longest_streak">🏆 Longest streak</string>\n'
    elif 'name="edit_profile_change_name"' in lines[i]:
        lines[i] = '    <string name="edit_profile_change_name">Đổi tên hiển thị</string>\n'
    elif 'name="edit_profile_change_password"' in lines[i]:
        lines[i] = '    <string name="edit_profile_change_password">Đổi mật khẩu</string>\n'

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)

print("Exact row replacements complete.")
