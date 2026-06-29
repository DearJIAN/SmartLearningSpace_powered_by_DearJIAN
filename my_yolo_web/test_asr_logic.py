import os
import io
import wave
import subprocess
import tempfile
import uuid

# create webm
wav_path = "test_input.wav"
with wave.open(wav_path, "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(16000)
    w.writeframes(b'\x00' * 16000 * 2)

webm_path = "test_input.webm"
subprocess.run(["ffmpeg", "-y", "-i", wav_path, "-c:a", "libopus", webm_path], capture_output=True)

try:
    with tempfile.TemporaryDirectory() as tmpdir:
        test_wav_path = os.path.join(tmpdir, f"{uuid.uuid4()}.wav")

        subprocess.run([
            "ffmpeg", "-y", "-i", webm_path,
            "-ar", "16000", "-ac", "1", test_wav_path
        ], check=True, capture_output=True, text=True)
        print("FFMPEG conversion succeeded")

        # Fallback to faster-whisper
        from faster_whisper import WhisperModel
        import opencc
        print("Loading WhisperModel...")
        model = WhisperModel("small", device="cpu", compute_type="int8")
        print("Transcribing...")
        segments, _ = model.transcribe(test_wav_path, language="zh", vad_filter=True)
        text_parts = []
        for seg in segments:
            text_parts.append(seg.text)
        recognized_text = "".join(text_parts).strip()
        print("Whisper recognized:", repr(recognized_text))
        cc = opencc.OpenCC("t2s")
        recognized_text = cc.convert(recognized_text)
        print("Final:", repr(recognized_text))

except Exception as e:
    import traceback
    traceback.print_exc()

