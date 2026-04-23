import pathlib

p = pathlib.Path('src/main/resources/templates/users.html')
t = p.read_text(encoding='utf-8')

lt = chr(60)
gt = chr(62)
sl = chr(47)
dq = chr(34)

# Build SVG icon for profile (person outline)
svg = (lt + 'svg class=' + dq + 'w-4 h-4' + dq + ' fill=' + dq + 'none' + dq +
       ' viewBox=' + dq + '0 0 24 24' + dq + ' stroke=' + dq + 'currentColor' + dq +
       ' stroke-width=' + dq + '2' + dq + gt +
       lt + 'path stroke-linecap=' + dq + 'round' + dq + ' stroke-linejoin=' + dq + 'round' + dq +
       ' d=' + dq + 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' + dq +
       sl + gt + lt + sl + 'svg' + gt)

# Build the profile button line to insert
link_open = (lt + 'a href=' + dq + sl + 'usuarios' + sl + dq + ' + u.id + ' +
             dq + dq + ' class=' + dq + 'btn-icon' + dq +
             ' title=' + dq + 'Ver perfil transportista' + dq + gt)
link_close = lt + sl + 'a' + gt

insert_line = ("                + (u.rol === 'TRANSPORTISTA' ? '  " +
               link_open + "' + svg_icon + '" + link_close + "' : '')\n")

# Replace svg_icon placeholder with actual svg
insert_line = insert_line.replace('svg_icon', svg)

# Marker: the edit button line that follows
marker = "                + '  " + lt + "button class=" + dq + "btn-icon" + dq + " title=" + dq + "Editar" + dq

if marker not in t:
    # Try without leading spaces
    marker2 = "btn-icon\" title=\"Editar\" onclick=\"abrirEditar"
    print('trying marker2, found:', marker2 in t)
    # Find context
    idx = t.find('abrirEditar')
    if idx >= 0:
        print('context around abrirEditar:', repr(t[max(0,idx-60):idx+20]))
else:
    t2 = t.replace(marker, insert_line + marker, 1)
    p.write_text(t2, encoding='utf-8')
    print('OK - profile button inserted before edit button')