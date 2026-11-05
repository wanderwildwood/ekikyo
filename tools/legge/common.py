import re
def join_lines(lines, flags=None):
    out = ''
    for s in lines:
        s = s.strip()
        if not s: continue
        if not out: out = s; continue
        if out.endswith('¬'):
            out = out[:-1] + s
        elif re.search(r'[a-z]-$', out) and not out.endswith('--'):
            if flags is not None: flags.append(out.split()[-1] + s.split()[0])
            out = out[:-1] + s          # soft hyphen assumed; reviewed via flags
        else:
            out += ' ' + s
    return tidy(out)
def tidy(s):
    s = s.replace('¬', '')
    s = re.sub(r'\s+', ' ', s)
    s = re.sub(r"([‘“]) ", r'\1', s)
    s = re.sub(r" ([’”])", r'\1', s)
    s = re.sub(r'\s*—\s*', '—', s)
    s = re.sub(r' ([,;:.?!)])', r'\1', s)
    s = re.sub(r'\( ', '(', s)
    return s.strip()
def key(tok):
    t = tok.replace('‘', "'").replace('’', "'").replace('“', '"').replace('”', '"')
    return t
def toks(s):
    # split words and punctuation-attached tokens; separate em-dash
    s = s.replace('—', ' — ')
    return s.split()
