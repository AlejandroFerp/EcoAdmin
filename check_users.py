path = r'src/main/resources/templates/users.html'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

old = '+ \'  <button class=
"
btn-icon
"
 title=
"
Editar
"
 onclick=
"
abrirEditar(\' + u.id + \')
"
>'
print('old =', repr(old))
print('found:', old in content)
