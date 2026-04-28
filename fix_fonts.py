import sys
import base64
import re

file_path = r'd:\AndroidFinal3\MobileFinalGroup11\app\src\main\res\values\strings.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

def fix_mojibake(match):
    before = match.group(1)
    content = match.group(2)
    after = match.group(3)
    
    try:
        # If content contains weird Windows-1252 mapped UTF-8 artifacts
        if 'Ã' in content or 'ð' in content or 'Æ' in content or 'â' in content:
            # Try to reverse from mojibake to actual UTF-8
            fixed = content.encode('cp1252').decode('utf-8')
            return before + fixed + after
    except Exception as e:
        # print("Could not decode string:", content)
        pass
    return match.group(0)

new_text = re.sub(r'(<string[^>]*>)(.*?)(</string>)', fix_mojibake, text)

# For any remaining ones that cp1252 didn't cleanly catch
replacements = {
    'ðŸ”¥': '🔥',
    'ðŸ †': '🏆',
    'ðŸ“š': '📚',
    'ðŸŽƒ': '🎃',
    'â€”': '—',
}
for k, v in replacements.items():
    new_text = new_text.replace(k, v)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_text)

print("Mojibake fixing complete.")
