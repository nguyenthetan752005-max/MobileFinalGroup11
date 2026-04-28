import sys
import re

file_path = r'd:\AndroidFinal3\MobileFinalGroup11\app\src\main\res\values\strings.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

def fix_mojibake(match):
    before = match.group(1)
    content = match.group(2)
    after = match.group(3)
    
    try:
        # Revert mojibake logic: 
        # The content has weird latin-1 characters that are actually UTF-8 bytes.
        # Encode back to cp1252 to get the original bytes
        bytes_val = content.encode('cp1252')
        # Decode as utf-8
        fixed = bytes_val.decode('utf-8')
        
        # If it decoded correctly and is different, we found mojibake!
        if fixed != content:
            return before + fixed + after
    except Exception:
        # Either couldn't encode as cp1252 (because it's already true UTF-8 with emojis)
        # or couldn't decode as utf-8 (because it's not mojibake)
        pass
    
    return match.group(0)

new_text = re.sub(r'(<string[^>]*>)(.*?)(</string>)', fix_mojibake, text)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_text)

print("Mojibake aggressive fixing complete.")
