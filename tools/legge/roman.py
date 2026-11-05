import re
VAL = {'I':1,'V':5,'X':10,'L':50}
def roman(tok):
    if not re.search(r'[IVXLHY]', tok): return None
    t = tok.replace('H','II').replace('Y','V').replace('U','II')
    t = re.sub(r'[l1!|\]]', 'I', t).replace(' ', '')
    if not t or not re.fullmatch(r'(XL|L?X{0,3})(IX|IV|V?I{0,3})', t): return None
    n = 0
    for i, c in enumerate(t):
        v = VAL[c]; n += -v if i+1 < len(t) and VAL[t[i+1]] > v else v
    return n or None
LEAD = re.compile(r'^([IVXLHYl1!|\]]{1,5}(?: ?[IVXLl1!|]{1,3}){0,2}) ?[.,*]\s*(?=\S)')
def lead_roman(s):
    m = LEAD.match(s)
    if not m: return None, 0
    return roman(m.group(1)), m.end()
