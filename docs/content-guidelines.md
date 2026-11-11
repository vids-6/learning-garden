# Content authoring
All seed lessons are machine written and `reviewed: false`, including English. Obtain review by local educators before classroom use. Seed: math, vocab, lifeskills; one unit and two lessons each; five activities per lesson, all in en/hi/mr.

IDs, type, option ordering, correctIndex, pair IDs and numeric answer must be identical across languages. Translate display text only. Run `python3 tools/validate_content.py` and ContentIntegrityTest after edits. Manifest sizes/checksums refer to the exact UTF-8 JSON bytes. Optional demo pack reuses the English math file and is copied into internal storage only after consent.

Images are tiny custom vector resources `ic_math`, `ic_vocab`, `ic_lifeskills`. Content uses these names. Replace with reviewed illustrations if needed; raster assets must be WebP ≤40 KB each. No image loader is used.

Audio is deliberately absent. Expected card files: `audio/{en,hi,mr}/{subject}_u1_l{1,2}_c{1,2}.ogg`. Prompt files: `audio/{en,hi,mr}/{subject}_u1_l{1,2}_a{1..5}.ogg`. Subjects: math, vocab, lifeskills. Future recordings: mono Ogg Vorbis, 16 kHz, ≤32 kbps, with speaker consent. Android MediaPlayer plays assets; missing/corrupt files use TextToSpeech only if a local voice exists. Never launch a voice download. Text stays visible.

Vocabulary means general words in the selected language; this prototype is not an English translation course. Listen-pick always presents a text equivalent for accessibility and missing voices.
