import sys, re, json
sys.path.insert(0, sys.argv[1])
from pages import *
src, ta, tb, aa, ab, out = sys.argv[2], *map(int, sys.argv[3:7]), sys.argv[7]
L = open(src, encoding='utf-8').readlines()
import pages as _p
if 'wg916' in src: _p.EXCLUDE.update({5674, 6967, 7916, 8764, 9823, 12009})
diag = []
def stream(a, b, mode):
    res = []
    for pg in pages(L, a, b):
        t, n = split_page(pg, mode=mode)
        if t and n and len(t) <= 3:
            diag.append(f"short-text-page {pg[0][0]}: " + " | ".join(s[:40] for _, s in t))
        if t and n:
            for ln_, s_ in t[-3:]:
                if norm_len(s_) >= 56: diag.append(f"LASTLINE-CANDIDATE {ln_} ({norm_len(s_)}): {s_}")
        if n and norm_len(n[0][1]) < 62: diag.append(f"borderline-note-start {n[0][0]}: {n[0][1][:60]}")
        res.extend(t)
    return res
HDR = re.compile(r'(?i)^.{0,14}\bThe\b.{1,26}H\s?E\s?X\s?\S?A\s?GRAM\.?\s*$')
NUM = re.compile(r'^([1-7]|i|l|I|\||t|x)\s?[.,:]\s')
def numval(tok):
    return {'i':1,'l':1,'I':1,'|':1,'t':1,'x':1}.get(tok, None) or int(tok)
# --- Text
s = stream(ta, tb, 'text')
hexes = []; cur = None
for ln, line in s:
    if HDR.match(line) and not line.startswith('THE ') and len(line) < 45:
        cur = {'n': len(hexes)+1, 'hdr': line, 'hdrline': ln, 'paras': [[]], 'nums': [0]}
        hexes.append(cur); continue
    if cur is None: continue
    m = NUM.match(line)
    expected = cur['nums'][-1] + 1
    if m and numval(m.group(1)) == expected and expected <= 7:
        cur['paras'].append([]); cur['nums'].append(expected)
        line = line[m.end():]
    cur['paras'][-1].append((ln, line))
# --- Appendix II
from roman import lead_roman
imgs = {}; cur_n = 0; state = 'seek'; lastnum = 0; buf = None
for pg in pages(L, aa, ab):
    for k, (ln, line) in enumerate(pg):
        if norm_len(line) >= 55 and k+1 < len(pg) and norm_len(pg[k+1][1]) >= 59:
            diag.append(f"LASTLINE-CANDIDATE-APP {ln} ({norm_len(line)}): {line}")
        if norm_len(line) >= 59 or re.match(r'^(Paragraph|Line|Lines) [\dil]', line):
            if k: diag.append(f"app-note-start {ln}: {line[:50]}")
            break
        r, _ = lead_roman(line)
        d = NUM.match(line)
        if r is not None and r == cur_n + 1 and (cur_n == 0 or (state == 'lines' and lastnum >= 6)):
            cur_n = r; buf = [(ln, line)]; imgs[cur_n] = buf; state = 'img'; lastnum = 0; continue
        if r is not None and not (d and r == 1):
            diag.append(f"app-roman-note {ln}: {line[:50]}"); break
        if state == 'img' and d and numval(d.group(1)) == 1:
            state = 'lines'; lastnum = 1; continue
        if state == 'lines' and d and numval(d.group(1)) == lastnum + 1:
            lastnum += 1; continue
        if state == 'img': buf.append((ln, line))
json.dump({'hexes': hexes, 'imgs': imgs, 'diag': diag}, open(out, 'w'), ensure_ascii=False, indent=0)
print(src.split('/')[-1], 'hexes', len(hexes), 'imgs', len(imgs))
for h in hexes:
    if h['nums'] != list(range(len(h['nums']))) or len(h['nums']) != (8 if h['n'] <= 2 else 7):
        print('  bad nums', h['n'], h['nums'], h['hdr'])
