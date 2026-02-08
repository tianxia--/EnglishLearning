#!/bin/bash

# Configuration
PROJECT_ROOT="/Users/pengfei.chen/Desktop/privateWork"
ANDROID_ASSETS="$PROJECT_ROOT/android-native-app/app/src/main/assets"
SHARED_CONTENT="$PROJECT_ROOT/shared-content"
AUDIO_SOURCE="$PROJECT_ROOT/英音"

echo "🔧 Asset Management Script"
echo "=========================="

# 1. Ensure assets directory exists
echo "1️⃣  Creating assets directory..."
mkdir -p "$ANDROID_ASSETS"

# 2. Copy shared content (Book lessons JSON)
echo "2️⃣  Copying shared content..."
cp -r "$SHARED_CONTENT/book1" "$ANDROID_ASSETS/"
cp -r "$SHARED_CONTENT/book2" "$ANDROID_ASSETS/"
cp -r "$SHARED_CONTENT/book3" "$ANDROID_ASSETS/"
cp -r "$SHARED_CONTENT/book4" "$ANDROID_ASSETS/"
cp "$SHARED_CONTENT/indexed_lessons.json" "$ANDROID_ASSETS/"

# 3. Copy Audio Files (MP3)
# We need to map the source folder structure to the assets structure
# Source: 英音/NCE1/01.mp3 -> Assets: book1/01.mp3 (example)
# Note: This depends on the actual structure of '英音'.
# Let's assume standard structure or flat structure, but we need to verify.
# For now, we'll write a python script to handle the smart copying and updating JSON.

echo "3️⃣  Processing Audio and updating JSON..."

python3 << EOF
import json
import os
import shutil
from pathlib import Path

# Config
assets_dir = Path("$ANDROID_ASSETS")
audio_source_root = Path("$AUDIO_SOURCE")

# Mapping Books to Source Folders
book_mapping = {
    "book1": "新概念英语第1册MP3(英音+LRC）",
    "book2": "新概念英语第2册MP3(英音+歌词LRC）",
    "book3": "新概念英语第3册MP3(英音+歌词LRC）",
    "book4": "新概念英语第4册MP3(英音+歌词LRC）"
}

def find_audio_file(filename, book_id):
    """Find audio file in source directory recursively"""
    # Try direct match in book specific folder first
    if book_id in book_mapping:
        source_subdir = audio_source_root / book_mapping[book_id]
        if source_subdir.exists():
            # Try exact match
            found = list(source_subdir.glob(f"**/{filename}"))
            if found: return found[0]
            
            # Try case insensitive
            for f in source_subdir.glob("**/*"):
                if f.name.lower() == filename.lower():
                    return f
    
    # Fallback: global search (slow)
    for f in audio_source_root.glob(f"**/{filename}"):
        return f
    return None

def process_book(book_id):
    book_dir = assets_dir / book_id
    if not book_dir.exists():
        print(f"Skipping {book_id}, dir not found")
        return

    print(f"Processing {book_id}...")
    
    for json_file in book_dir.glob("*.json"):
        try:
            changed = False
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            # Process Audio
            if 'audioFile' in data:
                original_path = data['audioFile']
                filename = os.path.basename(original_path)
                
                # Check if we need to copy
                dest_audio = book_dir / filename
                if not dest_audio.exists():
                    source_file = find_audio_file(filename, book_id)
                    if source_file:
                        print(f"  Copying audio: {filename}")
                        shutil.copy2(source_file, dest_audio)
                    else:
                        print(f"  ⚠️ Audio not found for: {json_file.name} ({filename})")
                
                # Update JSON to use relative path (just filename)
                if data['audioFile'] != filename:
                    data['audioFile'] = filename
                    changed = True

            # Process LRC
            # Note: LRCs might already be there or need copying too. 
            # Assuming for now they share location logic or are already in shared-content
            if 'lrcFile' in data:
                 original_lrc = data['lrcFile']
                 lrc_filename = os.path.basename(original_lrc)
                 if data['lrcFile'] != lrc_filename:
                     data['lrcFile'] = lrc_filename
                     changed = True

            if changed:
                with open(json_file, 'w', encoding='utf-8') as f:
                    json.dump(data, f, ensure_ascii=False, indent=2)
                # print(f"  Updated JSON: {json_file.name}")

        except Exception as e:
            print(f"Error processing {json_file.name}: {e}")

# Run for all books
for book in ["book1", "book2", "book3", "book4"]:
    process_book(book)

EOF

echo "✅ Assets updated successfully!"
