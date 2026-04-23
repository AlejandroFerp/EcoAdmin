import urllib.request, urllib.parse, json, http.cookiejar
jar = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
data = urllib.parse.urlencode({'username':'admin@ecoadmin.com','password':'admin123'}).encode()
opener.open('http://localhost:8080/login', data)
print('Logged in, cookies:', [c.name for c in jar])
r1 = opener.open('http://localhost:8080/api/usuarios'); t1 = r1.read().decode(); print('usuarios status:', r1.status, '| first 200:', t1[:200])

# El CookieJar de Python rechaza enviar cookies a localhost (dominio sin punto)
# Extraemos el JSESSIONID y lo inyectamos manualmente
jsid = next((c.value for c in jar if c.name == 'JSESSIONID'), None)
print('JSESSIONID:', jsid[:20] if jsid else 'MISSING')
req2 = urllib.request.Request('http://localhost:8080/api/usuarios')
req2.add_header('Cookie', f'JSESSIONID={jsid}')
r2 = urllib.request.urlopen(req2); t2 = r2.read().decode()
print('api/usuarios:', r2.status, t2[:300])
