import requests
import wave
import subprocess

# Generate a silent wav file
wav_path = "test_client.wav"
with wave.open(wav_path, "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(16000)
    w.writeframes(b'\x00' * 16000 * 2)

# Convert to webm to simulate browser
webm_path = "test_client.webm"
subprocess.run(["ffmpeg", "-y", "-i", wav_path, "-c:a", "libopus", webm_path], capture_output=True)

# Start server as a background process? No, we will just start it and test it.
# Actually, the user might be running the server themselves.
