import sys, time, urllib.request, os
out=sys.argv[1]
for n in range(int(sys.argv[2]), int(sys.argv[3])+1):
    p=os.path.join(out,f"{n:03d}.txt")
    if os.path.exists(p): continue
    url=f"https://en.wikisource.org/w/index.php?title=Page:Sacred_Books_of_the_East_-_Volume_16.djvu/{n}&action=raw"
    req=urllib.request.Request(url,headers={"User-Agent":"LeggeProofreadBot/0.1 (personal offline use)"})
    try:
        d=urllib.request.urlopen(req,timeout=30).read()
    except Exception as e:
        print(n,"ERR",e); d=b""
    open(p,"wb").write(d); time.sleep(1.2)
print("done")
