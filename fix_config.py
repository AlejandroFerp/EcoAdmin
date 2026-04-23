import pathlib

p = pathlib.Path('src/main/java/com/iesdoctorbalmis/spring/config/SeguridadConfig.java')
t = p.read_text(encoding='utf-8')

old = '                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")'
new = (
    '                // perfil-transportista is accessible by the owner (TRANSPORTISTA) or ADMIN; fine-grained check in controller\n'
    '                .requestMatchers("/api/usuarios/*/perfil-transportista").authenticated()\n'
    '                .requestMatchers("/api/transportistas/**").authenticated()\n'
    '                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")'
)

count = t.count(old)
print('Occurrences found:', count)
assert count >= 1, 'Pattern not found!'

t2 = t.replace(old, new, 1)
p.write_text(t2, encoding='utf-8')

lines = t2.splitlines()
for i, line in enumerate(lines):
    if 'perfil-transportista' in line or '/api/transportistas' in line or ('api/usuarios' in line and 'ADMIN' in line):
        start = max(0, i-2)
        end = min(len(lines), i+3)
        for j in range(start, end):
            print(f'{j+1:4}: {lines[j]}')
        print('---')

print('Done')
