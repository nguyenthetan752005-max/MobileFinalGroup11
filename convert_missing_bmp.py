import os
import sys
from PIL import Image

sys.stdout.reconfigure(encoding='utf-8')

base_dir = r'D:\umlCuoiKyMau'
categories = ['Activity', 'Class', 'Sequence', 'Use Case']
converted = []

for cat in categories:
    bmp_dir = os.path.join(base_dir, cat, 'bmp')
    png_dir = os.path.join(base_dir, cat, 'png')
    
    search_dirs = [bmp_dir, os.path.join(base_dir, cat)]
    
    for s_dir in search_dirs:
        if not os.path.exists(s_dir): 
            continue
            
        for f in os.listdir(s_dir):
            if f.lower().endswith('.bmp'):
                bmp_path = os.path.join(s_dir, f)
                bname = os.path.splitext(f)[0]
                
                target_dir = png_dir
                if not os.path.exists(target_dir): 
                    os.makedirs(target_dir)
                    
                png_path = os.path.join(target_dir, bname + '.png')
                alt_path = os.path.join(base_dir, cat, bname + '.png')
                
                if not os.path.exists(png_path) and not os.path.exists(alt_path):
                    try:
                        Image.open(bmp_path).save(png_path)
                        converted.append(f"{cat}/png/{bname}.png")
                    except Exception as e: 
                        print(f"Error converting {bname}: {e}")

print("Converted files:")
for c in converted:
    print(" - " + c)
