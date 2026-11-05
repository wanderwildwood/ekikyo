import re, sys, json
p = json.load(open(sys.argv[1], encoding='utf-8'))
txt = ''
for n, t in p:
    t = re.sub(r'</?(div|span|br|p)[^>]*>', '\n', t)
    t = re.sub(r'\n(?=\d\. |[IVXL]+\. The )', '\n\n', t)
    t = t.strip('\n ')
    if not t: continue
    if txt.endswith('\x00') and t.startswith('\x00HW'):
        txt = txt[:-1]; txt = re.sub(r'\x00HW([^\x00]*)$', r'\1', txt)
        t = re.sub(r'^\x00HW([^\x00]*)\x00', r'\1', t); txt += t; continue
    if re.match(r'^(\d\. |[IVXL]+\. The .*Hexagram)', t): txt += '\n\n' + t
    else: txt += ' ' + t
txt = txt.replace('\x00HW', '').replace('\x00', '')
paras = [re.sub(r'\s+', ' ', x).strip() for x in re.split(r'\n\s*\n', txt)]
paras = [x for x in paras if x and not re.match(r'^(THE YÎ KING\.|TEXT\. SECTION|Explanation of the|SECTION II)', x)]
hexes = []; cur = None
for x in paras:
    m = re.match(r'^([IVXL]+)\. The (.*) Hexagram\.$', x)
    if m:
        cur = {'n': len(hexes)+1, 'name': m.group(2), 'paras': []}; hexes.append(cur); continue
    cur['paras'].append(x)
for h in hexes:
    nums = [re.match(r'^(\d)\. ', x).group(1) if re.match(r'^(\d)\. ', x) else '-' for x in h['paras']]
    print(h['n'], h['name'], ''.join(nums))
json.dump(hexes, open(sys.argv[2], 'w'), ensure_ascii=False, indent=1)
