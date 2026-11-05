import sys, json, re
from collections import Counter
doc = json.load(open(sys.argv[1], encoding='utf-8'))
fail = 0; info = []
KNOWN_PAREN = {'4.line2', '12.image', '13.image'}
def bad(msg):
    global fail; fail += 1; print('FAIL:', msg)
H = doc.get('hexagrams', [])
if not doc.get('source', '').startswith('James Legge'): bad('source field')
if len(H) != 64: bad(f'{len(H)} entries, expected 64')
if [h.get('n') for h in H] != list(range(1, 65)): bad('n is not 1..64 in order')
POS = {1: ['first'], 2: ['second'], 3: ['third'], 4: ['fourth'], 5: ['fifth'], 6: ['sixth', 'topmost']}
NOTE_PHRASES = ['The Text under each hexagram', 'is the reverse of', 'Khang-hsi', 'Khang-hsî', 'editors', 'Appendix', 'APPENDIX',
                'Treatise', 'Paragraph', 'P. Regis', 'Kû Hsî', 'Kh Hsi', 'Symbolism', 'commentators', 'Line 1', 'line 2 ', 'Wăn says', 'the Thwan']
HEADS = [r'THE Y', r'\bKING\b', r'\bTEXT\b', r'\bSECT\b', r'\bHEX\b', r'HEXAGRAM', r'Hexagram\.', r'\[\s*16\s*\]', r'Explanation of the']
for h in H:
    n = h['n']
    if not isinstance(h.get('name'), str) or not h['name']: bad(f'{n}: no name')
    if len(h.get('lines', [])) != 6: bad(f'{n}: {len(h.get("lines", []))} lines')
    if ('allLines' in h) != (n in (1, 2)): bad(f'{n}: allLines presence wrong')
    if set(h) - {'n', 'name', 'judgment', 'lines', 'allLines', 'image'}: bad(f'{n}: extra keys {set(h)}')
    for i, l in enumerate(h.get('lines', []), 1):
        if not any(w in l.lower() for w in POS.get(i, [])):
            print(f'EXCEPTION (position word) {n}.{i}: {l[:70]}')
        if re.match(r'^\s*\d', l): bad(f'{n}.{i}: leading number')
    fields = [('judgment', h.get('judgment', '')), ('image', h.get('image', ''))] + [(f'line{i}', l) for i, l in enumerate(h.get('lines', []), 1)]
    if 'allLines' in h: fields.append(('allLines', h['allLines']))
    for f, s in fields:
        lab = f'{n}.{f}'
        if not s or len(s) < 20: bad(f'{lab}: empty/short')
        if '¬' in s: bad(f'{lab}: contains ¬')
        if re.search(r'\d', s): bad(f'{lab}: digit(s) {re.findall(r".{0,15}\d+.{0,10}", s)}')
        if re.search(r"[\"']", s): bad(f'{lab}: straight quote')
        if '  ' in s or re.search(r' [,;:.?!)]', s) or re.search(r'\( ', s): bad(f'{lab}: spacing')
        if s != s.strip(): bad(f'{lab}: untrimmed')
        for p in NOTE_PHRASES:
            if p in s: bad(f'{lab}: note phrase {p!r}')
        for r in HEADS:
            if re.search(r, s): bad(f'{lab}: running head/header pattern {r!r}')
        if not re.search(r'[.!?][’”)]?$', s): bad(f'{lab}: does not end with sentence punctuation: …{s[-30:]}')
        if not re.match(r'^[A-ZÎ(‘“]', s): bad(f'{lab}: odd start: {s[:30]}')
        if s.count('“') != s.count('”'): bad(f'{lab}: unbalanced double quotes')
        opens = s.count('‘'); closes = len(re.findall(r'’(?![a-z])', s))
        if opens != closes: bad(f'{lab}: unbalanced single quotes ({opens} open, {closes} close)')
        if s.count('(') != s.count(')'):
            if lab in KNOWN_PAREN: print(f'KNOWN (printed so in 1882; both OCRs agree): {lab} unbalanced parentheses')
            else: bad(f'{lab}: unbalanced parentheses')
        if re.search(r'[^\x20-\x7e‘’“”—âîûăüÎÂÛ]', s): bad(f'{lab}: unexpected char {set(re.findall(r"[^\x20-\x7e‘’“”—âîûăüÎÂÛ]", s))}')
        if re.search(r'[a-z]-[ ]|[ ]-[a-z]', s): bad(f'{lab}: broken hyphenation')
        if f == 'image' and re.match(r'^[IVXL]+\.', s): bad(f'{lab}: roman numeral left')
        if f == 'image' and re.search(r'(?<![a-z])[1-7]\. ', s): bad(f'{lab}: line-symbolism leak')
print('entries:', len(H), '| lines total:', sum(len(h.get('lines', [])) for h in H), '| allLines on:', [h['n'] for h in H if 'allLines' in h])
print('RESULT:', 'PASS' if fail == 0 else f'FAIL ({fail} problems)')
sys.exit(1 if fail else 0)
