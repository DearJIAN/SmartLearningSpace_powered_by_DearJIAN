import os
import io
try:
    from faster_whisper import WhisperModel
    import opencc
    print("Dependencies are installed.")
except Exception as e:
    print(f"Error importing dependencies: {e}")
