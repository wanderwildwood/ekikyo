#!/usr/bin/env python3
"""Cuts Noto Serif (SIL OFL 1.1) down to the characters the app sets in it: Legge's text, the
English strings and the pinyin, plus printable ASCII. Regular and italic.

    PYTHONPATH=<fonttools> python3 tools/make-book-font.py [/usr/share/fonts/truetype/noto]
"""
import json, re, sys, pathlib
from fontTools.ttLib import TTFont
from fontTools import subset

root = pathlib.Path(__file__).resolve().parent.parent
src = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "/usr/share/fonts/truetype/noto")
main = root / "app/src/main"
text = (main / "assets/legge.json").read_text(encoding="utf-8")
text += (main / "res/values/strings.xml").read_text(encoding="utf-8")
text += (main / "kotlin/com/wanderwildwood/ekikyo/iching/KingWen.kt").read_text(encoding="utf-8")
chars = {c for c in text if ord(c) < 0x2E80} | {chr(c) for c in range(0x20, 0x7F)} | set("’‘“”–—…·→○×")
chars = "".join(sorted(chars))
for style, out in (("Regular", "noto_serif_book.ttf"), ("Italic", "noto_serif_book_italic.ttf")):
    font = TTFont(src / f"NotoSerif-{style}.ttf")
    opts = subset.Options(); opts.name_IDs = ["*"]; opts.notdef_outline = True
    opts.layout_features = ["kern", "liga", "ccmp", "mark", "mkmk"]
    s = subset.Subsetter(opts); s.populate(text=chars); s.subset(font)
    path = main / "res/font" / out
    font.save(path)
    print(style, len(chars), "characters ->", path.name, path.stat().st_size, "bytes")
