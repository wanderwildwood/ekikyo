EXCLUDE = set()
import re, sys
HEAD = [r'^(\S{1,4}\s+)?THE \S{1,4} ?KING\.?(\s+TEXT\.?)?(\s+\S{1,4})?$', r'^(\S{1,4}\s+)?(SECT\.? ?\S{1,4}\s+)?THE [^a-z]{0,30}[A-Za-z ]{0,6}(HEXAGRAM|hexagram)S?\.?(\s+\S{1,4})?$', r'^(\S{1,4}\s+)?THE APPENDIXES\.?(\s+\S{1,4})?$', r'^(\S{1,4}\s+)?APPENDIX [IVXl1 ]+\.?(\s+\S{1,4})?$',r'^THE \S{1,4} ?KING\.?$', r'^TEXT[.,]?$', r'^SECT\.? ?[IVXl1]+\.?$', r'^SECTION [IVX]+\.?$',
        r'^THE [^a-z]{0,30}[A-Za-z ]{0,6}(HEXAGRAM|hexagram)S?\.?$', r'^[0-9il ]{1,6}$', r'^(?=.*\d|iog|ioi|ioo)[0-9ilIoOgS]{3}$',
        r'^\[\s*1\s*6\s*\]', r'^THE APPENDIXES\.?$', r'^APPENDIX [IVXl1 ]+\.?$', r'^HEX[S]?\.? ?[\d ,\-il]+\.?$']
HEADRE = [re.compile(h) for h in HEAD]
def is_head(s):
    s = s.strip()
    if len(s) < 50 and re.search(r'(?i)the appendixes|^(\S{1,5}\s+)?appendix\s+[ivxl1]+\.?(\s+\S{1,5})?$', s): return True
    if len(s) < 30 and re.match(r'(?i)^(\S{1,5}\s+)?hex[s]?\.?\s*[\dil]+', s): return True
    if len(s) < 50 and re.match(r'^(\S{1,5}\s+)?SECT', s, re.I) and re.search(r'HEXAGRAM|KING|APPENDIX', s, re.I): return True
    if len(s) < 50 and re.match(r'^(\S{1,5}\s+)?SECT\S*\s*\S{1,5}\.?$', s, re.I): return True
    if len(s) < 50 and re.match(r'^(\S{1,5}\s+)?(THE|the)\s.{0,30}(HEXAGRAM|hexagram)S?\.?(\s+\S{1,5}){0,2}$', s): return True
    if len(s) < 40 and re.match(r'^(\S{1,5}\s+)?(THE|the)\s+\S{0,4}\s*(KING|king)\.?', s): return True
    if re.fullmatch(r'[0-9il ]{1,6}', s) and not re.search(r'\d', s): return False
    return any(r.search(s) for r in HEADRE)
def norm_len(s):
    s = re.sub(r'\s+', ' ', s.strip())
    s = re.sub(r'([‘“]) ', r'\1', s); s = re.sub(r' ([’”])', r'\1', s)
    return len(s)
def pages(lines, a, b):
    """yield (page_lines) list of (lineno, text) split at running-head clusters"""
    cur = []; out = []
    for i in range(a, b):
        s = lines[i].rstrip('\n')
        if not s.strip(): continue
        if re.fullmatch(r'(I[il]{2}|I[il]o|Iio|ii[il0-9])', s.strip()):
            if cur: out.append(cur); cur = []
            continue
        if len(s.strip()) <= 6 and not re.search(r'[a-z]{2,}', s):
            if re.fullmatch(r'[0-9IlioOXS! ]{2,6}', s.strip()) and cur: out.append(cur); cur = []
            continue
        if (i+1) in EXCLUDE:
            if cur: cur.append((i+1, '\x00NOTE'))
            continue
        if is_head(s):
            if cur: out.append(cur); cur = []
            continue
        cur.append((i+1, s.strip()))
    if cur: out.append(cur)
    return out
HDR = re.compile(r'(?i)^.{0,14}\bThe\b.{1,26}H\s?E\s?X\s?\S?A\s?GRAM\.?\s*$')
NOTEROM = re.compile(r'^[IVXLl]{1,7}\.\s+')
def split_page(pg, T=59, mode='text'):
    for k, (ln, s) in enumerate(pg):
        if s == '\x00NOTE': return pg[:k], pg[k+1:]
    for k, (ln, s) in enumerate(pg):
        if norm_len(s) >= T: return pg[:k], pg[k:]
        if mode == 'text' and NOTEROM.match(s) and not HDR.match(s): return pg[:k], pg[k:]
    return pg, []
