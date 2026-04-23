import pathlib
p = pathlib.Path("src/main/java/com/iesdoctorbalmis/spring/controladores/ZonaPublicaController.java")
text = p.read_text(encoding="utf-8")
count = text.count('@GetMapping("/usuarios/{id}")')
print("occurrences:", count)
if count == 2:
    idx = text.find('@GetMapping("/usuarios/{id}")')
    start = text.rfind('\n', 0, idx) + 1
    brace_open1 = text.index('{', idx)
    brace_open2 = text.index('{', brace_open1 + 1)
    brace_close = text.index('}', brace_open2) + 1
    end = brace_close
    if end < len(text) and text[end] == '\n':
        end += 1
    text = text[:start] + text[end:]
    p.write_text(text, encoding="utf-8")
    print("Fixed duplicate")
else:
    print("No fix needed")
