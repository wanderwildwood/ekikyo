import re, sys, os, json
def find_close(s, i):
    depth = 0; j = i
    while j < len(s):
        if s.startswith('{{', j): depth += 1; j += 2; continue
        if s.startswith('}}', j):
            depth -= 1; j += 2
            if depth == 0: return j
            continue
        j += 1
    return len(s)
def split_args(body):
    args = []; depth = 0; cur = ''; k = 0
    while k < len(body):
        if body.startswith('{{', k) or body.startswith('[[', k): depth += 1; cur += body[k:k+2]; k += 2; continue
        if body.startswith('}}', k) or body.startswith(']]', k): depth -= 1; cur += body[k:k+2]; k += 2; continue
        if body[k] == '|' and depth == 0: args.append(cur); cur = ''; k += 1; continue
        cur += body[k]; k += 1
    args.append(cur); return args
DROP = {'dhr','rule','nop','anchor','smallrefs','div end','main other','font-stretch','br','bl','BC','smaller block/s','center block/s','running header','RunningHeader','rh','hws','hwe'}
unknown = set()
def tmpl(name, args):
    n = name.strip()
    pos = [a for a in args if not re.match(r'^\s*[\w-]+\s*=', a)]
    if n in ('sp', 'spw', 'lsp'):
        if n == 'lsp': pos = pos[1:]
        return ''.join(pos)
    if n in ('tooltip', 'SIC', 'nowrap', 'lang', 'c', 'sc', 'asc', 'smaller', 'larger', 'x-larger', 'sm', 'center inline', 'fine', 'small-caps', 'x-smaller'):
        if n == 'lang': return pos[1] if len(pos) > 1 else ''
        return pos[0] if pos else ''
    if n == 'anchor+': return pos[1] if len(pos) > 1 else ''
    if n == 'font-size': return ''
    if n == 'bl/il': return pos[0].upper() if pos else ''
    if n == 'gap' or n == 'nbsp': return ' '
    if n == 'hws': return pos[0]
    if n == 'hwe': return pos[0]
    if n in DROP: return ''
    unknown.add(n); return ''
def expand(s):
    out = ''; i = 0
    while i < len(s):
        if s.startswith('{{', i):
            j = find_close(s, i)
            body = s[i+2:j-2]
            parts = split_args(body)
            name = parts[0]; args = [expand(a) for a in parts[1:]]
            if name.strip() in ('hws', 'hwe'): out += '\x00HW' + args[0] + '\x00'
            else: out += tmpl(name, args)
            i = j; continue
        out += s[i]; i += 1
    return out
def links(s):
    s = re.sub(r'\[\[(?:[^\]|]*\|)?([^\]]*)\]\]', r'\1', s)
    return s
def page_text(raw):
    raw = re.sub(r'<noinclude>.*?</noinclude>', '', raw, flags=re.S)
    if '<section begin="text"' in raw:
        segs = re.findall(r'<section begin="text"\s*/>(.*?)<section end="text"\s*/>', raw, flags=re.S)
        return '\n'.join(segs)
    # no sections: text is before first {{rule}}
    return re.split(r'\{\{rule\}\}', raw)[0]
if __name__ == '__main__':
    d = sys.argv[1]; out = []
    for n in range(91, 162):
        raw = open(os.path.join(d, f'{n:03d}.txt'), encoding='utf-8').read()
        t = page_text(raw)
        t = re.sub(r'\[\[File:[^\]]*\]\]', '', t)
        t = links(expand(t))
        t = re.sub(r"'''?", lambda m: '' if len(m.group(0)) == 2 else '', t)
        out.append((n, t))
    print('unknown templates:', unknown, file=sys.stderr)
    json.dump(out, open(sys.argv[2], 'w'), ensure_ascii=False, indent=0)
