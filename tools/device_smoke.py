#!/usr/bin/env python3
"""Fresh-emulator smoke test. Install debug APK first. Uses only Python stdlib + adb.
Creates a fictional learner and verifies a force-stop during first-answer feedback.
"""
import os
import re
import sqlite3
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ADB = os.environ.get("ADB", str(Path.home() / "Library/Android/sdk/platform-tools/adb"))
PACKAGE = "org.rural.learn"

def adb(*args, binary=False):
    return subprocess.check_output([ADB, *args], text=not binary, stderr=subprocess.DEVNULL)

def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/window.xml")
    return ET.fromstring(adb("shell", "cat", "/sdcard/window.xml")).iter("node")

def click(text, timeout=20):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        for node in nodes():
            if node.attrib.get("text") == text:
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.attrib["bounds"]))
                adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
                time.sleep(.4)
                return
        adb("shell", "input", "swipe", "350", "1050", "350", "350", "200")
        time.sleep(.2)
    raise AssertionError(f"Missing UI text: {text}")

def snapshot():
    with tempfile.TemporaryDirectory() as folder:
        for suffix in ("", "-wal", "-shm"):
            try:
                data = adb("exec-out", "run-as", PACKAGE, "cat", "databases/garden.db"+suffix, binary=True)
                Path(folder, "garden.db"+suffix).write_bytes(data)
            except subprocess.CalledProcessError:
                pass
        db = sqlite3.connect(Path(folder, "garden.db"))
        result = {t: db.execute(f"SELECT * FROM {t} ORDER BY 1,2").fetchall()
                  for t in ("LessonSession", "LessonProgress", "XpLedger")}
        db.close()
        return result

def main():
    adb("shell", "settings", "put", "global", "airplane_mode_on", "1")
    adb("shell", "svc", "wifi", "disable")
    adb("shell", "svc", "data", "disable")
    adb("shell", "am", "start", "-W", "-n", PACKAGE+"/.MainActivity")
    click("English")
    click("Your name")
    adb("shell", "input", "text", "Mira")
    adb("shell", "input", "keyevent", "4")
    click("Create learner")
    click("Start lesson")
    click("Let us practise")
    click("4")  # Deliberately incorrect; second attempt must remain available after restart.
    time.sleep(1)
    adb("shell", "am", "force-stop", PACKAGE)
    before = snapshot()
    assert before["LessonSession"] and '"attempt":1' in before["LessonSession"][0][3]
    adb("shell", "am", "start", "-W", "-n", PACKAGE+"/.MainActivity")
    click("Continue lesson")
    assert any(n.attrib.get("text") == "Try once more. You can do it." for n in nodes())
    adb("shell", "am", "force-stop", PACKAGE)
    assert before == snapshot(), "Progress changed during force-stop/restart"
    print("PASS: airplane mode; attempt 1 + pending feedback + XP unchanged after force-stop and resume.")

if __name__ == "__main__":
    main()
