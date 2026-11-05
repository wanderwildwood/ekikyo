import sys, json, re
raw = json.load(open(sys.argv[1], encoding='utf-8'))
NAMES = {1:'Khien',2:'Khwăn',3:'Kun',4:'Măng',5:'Hsü',6:'Sung',7:'Sze',8:'Pî',9:'Hsiâo Khû',10:'Lî',11:'Thâi',12:'Phî',
 13:'Thung Zăn',14:'Tâ Yû',15:'Khien',16:'Yü',17:'Sui',18:'Kû',19:'Lin',20:'Kwân',21:'Shih Ho',22:'Pî',23:'Po',24:'Fû',
 25:'Wû Wang',26:'Tâ Khû',27:'Î',28:'Tâ Kwo',29:'Khan',30:'Lî',31:'Hsien',32:'Hăng',33:'Thun',34:'Tâ Kwang',35:'Zin',
 36:'Ming Î',37:'Kia Zăn',38:'Khwei',39:'Kien',40:'Kieh',41:'Sun',42:'Yî',43:'Kwâi',44:'Kâu',45:'Zhui',46:'Shăng',
 47:'Khwăn',48:'Zing',49:'Ko',50:'Ting',51:'Kăn',52:'Kăn',53:'Kien',54:'Kwei Mei',55:'Făng',56:'Lü',57:'Sun',58:'Tui',
 59:'Hwân',60:'Kieh',61:'Kung Fû',62:'Hsiâo Kwo',63:'Kî Zî',64:'Wei Zî'}
# (hexagram, field, old, new, expected count). field: J, L1..L6, A (allLines), I (image)
FIX = [
 # --- image names, hexagrams 1-32 (OCR wg916; dli agrees on the words, both garble the names)
 (2,'I','by Khwan.','by Khwăn.',1),(3,'I','form Knn.','form Kun.',1),(4,'I','form Mang.','form Măng.',1),
 (5,'I','forms Hsu.','forms Hsü.',1),(8,'I','form Pi.','form Pî.',1),(9,'I','form Hsiao KkiX.','form Hsiâo Khû.',1),
 (10,'I','form Ll.','form Lî.',1),(11,'I','form Thai.','form Thâi.',1),(12,'I','form Phi.','form Phî.',1),
 (13,'I','form Thung Zan.','form Thung Zăn.',1),(14,'I','form Ta Yi The','form Tâ Yû. The',1),
 (15,'I','form _<Oien.','form Khien.',1),(16,'I','form Y ii.','form Yü.',1),(18,'I','form Kfi.','form Kû.',1),
 (20,'I','form Kwan.','form Kwân.',1),(22,'I','form Pi.','form Pî.',1),(24,'I','form Fu.','form Fû.',1),
 (26,'I','form Ta Kh<x.','form Tâ Khû.',1),(27,'I','form 1.','form Î.',1),(28,'I','forms T 4 Kwo.','forms Tâ Kwo.',1),
 (30,'I','forms Li.','forms Lî.',1),(32,'I','form Hang.','form Hăng.',1),
 # --- names in hexagrams 33-64 (text + image)
 (34,'J','T& Afwang','Tâ Kwang',1),(34,'I','form Ta./Twang.','form Tâ Kwang.',1),
 (35,'J','In 3 in we','In Zin we',1),(35,'I','form 3 i n - The','form Zin. The',1),
 (36,'J','Ming t indicates','Ming Î indicates',1),(36,'L1','Ming I,','Ming Î,',1),(36,'L2','Ming!,','Ming Î,',1),
 (36,'L3','Ming!,','Ming Î,',1),(36,'L4','Ming I,','Ming Î,',1),(36,'L5','by Ming t.','by Ming Î.',1),
 (36,'L5','count of Ki','count of Kî',1),(36,'I','form Ming I.','form Ming Î.',1),
 (37,'J','Kik Z an,','Kia Zăn,',1),(37,'I','form Add Z an.','form Kia Zăn.',1),
 (39,'J','by) K ien advantage','by) Kien advantage',1),(39,'I','form Aden.','form Kien.',1),
 (40,'J','by) An eh advantage','by) Kieh advantage',1),(40,'I','form K\\ eh.','form Kieh.',1),
 (42,'I','form Y i.','form Yî.',1),(42,'J','Yi indicates','Yî indicates',1),
 (43,'J','Kw&i requires','Kwâi requires',1),(43,'I','form Kw&i.','form Kwâi.',1),
 (44,'J','Kiu shows','Kâu shows',1),(44,'I','form Kau.','form Kâu.',1),
 (45,'J','by) 3 hui,','by) Zhui,',1),(45,'I','form 3 hui.','form Zhui.',1),
 (46,'J','Shang indicates','Shăng indicates',1),(46,'I','form Shang.','form Shăng.',1),(46,'L4','mount Khi.','mount Khî.',1),
 (47,'J','by) Khwan there','by) Khwăn there',1),(47,'I','form Khwan.','form Khwăn.',1),
 (48,'J','(Looking at) Bing,','(Looking at) Zing,',1),(48,'I','form 3ing.','form Zing.',1),
 (51,'J',"A'an gives",'Kăn gives',1),(51,'J','hundred 11,','hundred lî,',1),(51,'I','forms Wan.','forms Kăn.',1),
 (52,'I','form Kan.','form Kăn.',1),
 (53,'J','Afien suggests','Kien suggests',1),(53,'I','form -/Tien.','form Kien.',1),
 (54,'L5','(king) Ti-yi,','(king) Tî-yî,',1),
 (55,'J','Fang intimates','Făng intimates',1),(55,'I','form Fang.','form Făng.',1),
 (56,'J','Lii intimates','Lü intimates',1),(56,'I','form Lii.','form Lü.',1),
 (58,'J','T u i intimates','Tui intimates',1),
 (59,'J','HwAn intimates','Hwân intimates',1),(59,'I','form Hwin, The','form Hwân. The',1),
 (60,'J','K ieh intimates','Kieh intimates',1),(60,'I','form An eh.','form Kieh.',1),
 (61,'J',"A'ung Fft (moves",'Kung Fû (moves',1),(61,'I','form iifung Fft.','form Kung Fû.',1),
 (62,'J','HsiAo Kwo indicates','Hsiâo Kwo indicates',1),(62,'I','form Hsiao Kwo.','form Hsiâo Kwo.',1),
 (63,'J','Ki 3 i intimates','Kî Zî intimates',1),(63,'I','form Ki 31.','form Kî Zî.',1),(63,'L3','Kao 3 ung','Kâo Zung',1),
 (64,'J','Wei 3* intimates','Wei Zî intimates',1),(64,'I','form Wei 3 i.','form Wei Zî.',1),
 # --- non-name OCR errors (wg916), decided against dli / context
 (40,'L4','1 Remove','‘Remove',1),(44,'L1','sure t® keep','sure to keep',1),
 (45,'L5','longcontinued','long-continued',1),(62,'L6','selfproduced','self-produced',1),
 (62,'L5','c ivided','divided',1),
 # --- Wikisource section heading carried into the last paragraph of Section I
 (30,'L6',' TEXT. SECTION II.','',1),(56,'L4','saying),‘I','saying), ‘I',1),
]
EXTRA = json.load(open(sys.argv[3])) if len(sys.argv) > 3 else []
FIX += [tuple(x) for x in EXTRA]
ROMAN = re.compile(r'^[IVXLH1l! ]{1,9}\.\s*')
def smart(s):
    s = s.replace('‘', "'").replace('’', "'").replace('“', '"').replace('”', '"')
    s = re.sub(r'\(= ', '(=', s)
    out = []
    for i, c in enumerate(s):
        prev = s[i-1] if i else ' '
        if c == '"':
            out.append('“' if (prev in ' (—[\'‘' or i == 0) else '”')
        elif c == "'":
            out.append('‘' if (prev in ' (—["“' or i == 0) else '’')
        else: out.append(c)
    return ''.join(out)
hexes = []; log = []
for n in range(1, 65):
    h = raw[str(n)]
    f = {'J': h['judgment'], 'I': ROMAN.sub('', h['image'], count=1)}
    for i, l in enumerate(h['lines']): f[f'L{i+1}'] = l
    if h['all']: f['A'] = h['all']
    for (hn, fld, old, new, cnt) in FIX:
        if hn != n: continue
        c = f[fld].count(old)
        if c != cnt: log.append(f"FIX FAILED {hn} {fld} {old!r}: found {c}, expected {cnt}"); continue
        f[fld] = f[fld].replace(old, new)
    f = {k: smart(v) for k, v in f.items()}
    e = {'n': n, 'name': NAMES[n], 'judgment': f['J'], 'lines': [f[f'L{i}'] for i in range(1, 7)]}
    if 'A' in f: e['allLines'] = f['A']
    e['image'] = f['I']
    hexes.append(e)
doc = {'source': 'James Legge, The Yî King, Sacred Books of the East vol. XVI (Oxford, 1882). Public domain.', 'hexagrams': hexes}
json.dump(doc, open(sys.argv[2], 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print('\n'.join(log) or 'all fixes applied'); print('fixes:', len(FIX))
