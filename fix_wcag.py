import os
import glob
import re

layout_dir = 'app/src/main/res/layout'
files_updated = 0
for filepath in glob.glob(os.path.join(layout_dir, '*.xml'), recursive=True):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    def replace_imageview(match):
        block = match.group(0)
        if 'contentDescription' not in block and 'importantForAccessibility' not in block:
            return block.replace('<ImageView', '<ImageView\n        android:importantForAccessibility="no"', 1)
        return block

    new_content = re.sub(r'<ImageView[^>]*>', replace_imageview, content)
    
    if new_content != content:
        print(f'Updated {filepath}')
        files_updated += 1
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
print(f'Total files updated: {files_updated}')
