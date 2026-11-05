#!/usr/bin/env python3
"""Cuts Noto Serif CJK TC (SIL OFL 1.1) down to the Chinese characters the app shows: the names
of the 64 hexagrams and the eight trigrams, taken from iching/KingWen.kt. The phone may have no
Chinese font of its own, so the app carries these few glyphs itself.

    PYTHONPATH=<fonttools> python3 tools/make-font.py [/usr/share/fonts/opentype/noto/NotoSerifCJK-Regular.ttc]
"""
import re, sys, pathlib
from fontTools.ttLib import TTCollection
from fontTools import subset

root = pathlib.Path(__file__).resolve().parent.parent
src = sys.argv[1] if len(sys.argv) > 1 else "/usr/share/fonts/opentype/noto/NotoSerifCJK-Regular.ttc"
kt = (root / "app/src/main/kotlin/com/wanderwildwood/ekikyo/iching/KingWen.kt").read_text()
chars = sorted({c for c in "".join(re.findall(r'"([^"]+)"', kt)) if 0x3400 <= ord(c) <= 0x9FFF})
font = next(f for f in TTCollection(src).fonts if "Noto Serif CJK TC" in str(f["name"].getDebugName(1)))
opts = subset.Options(); opts.layout_features = []; opts.name_IDs = ["*"]; opts.notdef_outline = True
s = subset.Subsetter(opts); s.populate(text="".join(chars)); s.subset(font)
out = root / "app/src/main/res/font/noto_serif_tc_hexagrams.otf"
font.save(out)
print(len(chars), "characters ->", out, out.stat().st_size, "bytes")
