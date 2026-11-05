import sys, json, re
sys.path.insert(0, sys.argv[1])
from common import *
wg = json.load(open(sys.argv[2])); wk = json.load(open(sys.argv[3]))
out = {}
for n in range(1, 65):
    h = wg['hexes'][n-1]
    if n <= 32:
        w = wk[n-1]
        paras = [re.sub(r'^\d\. ', '', p) for p in w['paras']]
        src = 'wiki'
    else:
        paras = [join_lines([s for _, s in p]) for p in h['paras']]
        src = 'wg'
    img = join_lines([s for _, s in wg['imgs'][str(n)]])
    out[n] = {'src': src, 'judgment': paras[0], 'lines': paras[1:7], 'all': paras[7] if len(paras) > 7 else None, 'image': img}
json.dump(out, open(sys.argv[4], 'w'), ensure_ascii=False, indent=1)
