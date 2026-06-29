import os
import wave
import traceback

try:
    wav_path = "test_audio.wav"
    with wave.open(wav_path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(16000)
        w.writeframes(b'\x00' * 16000 * 2) # 1 second of silence
    print("Created test_audio.wav")

    from faster_whisper import WhisperModel
    import opencc
    print("Loading model...")
    model = WhisperModel("small", device="cpu", compute_type="int8")
    print("Transcribing...")
    segments, _ = model.transcribe(wav_path, language="zh", vad_filter=True)
    text_parts = []
    for seg in segments:
        text_parts.append(seg.text)
    recognized_text = "".join(text_parts).strip()
    cc = opencc.OpenCC("t2s")
    recognized_text = cc.convert(recognized_text)
    print(f"Recognized text: '{recognized_text}'")
except Exception as e:
    traceback.print_exc()
