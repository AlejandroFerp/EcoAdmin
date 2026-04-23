BOM = b'\xef\xbb\xbf'
files = [
    'src/main/java/com/iesdoctorbalmis/spring/modelo/PerfilTransportista.java',
    'src/main/java/com/iesdoctorbalmis/spring/repository/PerfilTransportistaRepository.java',
    'src/main/java/com/iesdoctorbalmis/spring/servicios/TarifaValidator.java',
    'src/main/java/com/iesdoctorbalmis/spring/servicios/PerfilTransportistaService.java',
    'src/main/java/com/iesdoctorbalmis/spring/controladores/PerfilTransportistaController.java',
    'src/test/java/com/iesdoctorbalmis/spring/TarifaValidatorTest.java',
    'src/main/java/com/iesdoctorbalmis/spring/controladores/ZonaPublicaController.java',
]
for f in files:
    try:
        data = open(f, 'rb').read()
        if data.startswith(BOM):
            open(f, 'wb').write(data[3:])
            print('BOM stripped: ' + f)
        else:
            print('No BOM:       ' + f)
    except Exception as e:
        print('ERROR ' + f + ': ' + str(e))