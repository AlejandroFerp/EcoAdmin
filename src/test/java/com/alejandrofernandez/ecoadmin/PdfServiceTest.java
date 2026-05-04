package com.alejandrofernandez.ecoadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.alejandrofernandez.ecoadmin.modelo.Centro;
import com.alejandrofernandez.ecoadmin.modelo.Direccion;
import com.alejandrofernandez.ecoadmin.modelo.ListaLer;
import com.alejandrofernandez.ecoadmin.modelo.Residuo;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.Usuario;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoTraslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.Rol;
import com.alejandrofernandez.ecoadmin.repository.CentroRepository;
import com.alejandrofernandez.ecoadmin.repository.DireccionRepository;
import com.alejandrofernandez.ecoadmin.repository.ListaLerRepository;
import com.alejandrofernandez.ecoadmin.repository.ResiduoRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;
import com.alejandrofernandez.ecoadmin.repository.UsuarioRepository;
import com.alejandrofernandez.ecoadmin.servicios.PdfService;

@SpringBootTest
@Transactional
class PdfServiceTest {

    @Autowired private PdfService pdfService;
    @Autowired private TrasladoRepository trasladoRepo;
    @Autowired private ResiduoRepository residuoRepo;
    @Autowired private CentroRepository centroRepo;
    @Autowired private DireccionRepository direccionRepo;
    @Autowired private ListaLerRepository listaLerRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    private Traslado crearTrasladoCompleto() {
        Usuario trans = usuarioRepo.save(new Usuario("Trans PDF", "trans-pdf@test.com", "pass", Rol.TRANSPORTISTA));
        Direccion dir = direccionRepo.save(new Direccion("C/ PDF", "Alicante", "03001", "Alicante", "Espana"));
        Usuario gestor = usuarioRepo.save(new Usuario("Gestor PDF", "gestor-pdf@test.com", "pass", Rol.GESTOR));
        Centro cp = centroRepo.save(new Centro(gestor, "Productor PDF", "PRODUCTOR", dir));
        Centro cg = centroRepo.save(new Centro("Gestor PDF SL", "GESTOR", dir));
        listaLerRepo.save(new ListaLer("170405", "Hierro y acero"));
        Residuo r = new Residuo(500.0, "kg", "PENDIENTE", cp);
        r.setCodigoLER("170405");
        residuoRepo.save(r);
        Traslado t = new Traslado(cp, cg, r, trans);
        t.setEstado(EstadoTraslado.COMPLETADO);
        return trasladoRepo.save(t);
    }

    @Test
    @DisplayName("generarCartaDePorte genera PDF no vacio con cabecera PDF")
    void cartaDePorte_generaPdf() {
        Traslado t = crearTrasladoCompleto();
        byte[] pdf = pdfService.generarCartaDePorte(t);
        assertNotNull(pdf);
        assertTrue(pdf.length > 100, "El PDF deberia tener contenido sustancial");
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    @DisplayName("generarNotificacionTraslado genera PDF valido")
    void notificacionTraslado_generaPdf() {
        Traslado t = crearTrasladoCompleto();
        byte[] pdf = pdfService.generarNotificacionTraslado(t);
        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
    }

    @Test
    @DisplayName("generarCertificadoRecepcion genera PDF valido")
    void certificadoRecepcion_generaPdf() {
        Traslado t = crearTrasladoCompleto();
        byte[] pdf = pdfService.generarCertificadoRecepcion(t);
        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
    }

    @Test
    @DisplayName("generarInformeFinalGestion genera PDF con tabla por LER")
    void informeFinalGestion_generaPdf() {
        List<Map<String, Object>> filas = List.of(
            Map.of("codigoLER", "170405", "descripcion", "Hierro", "cantidadTotal", 500.0,
                    "unidad", "kg", "trasladosCompletados", 3, "gestoresUnicos", 2L)
        );
        Map<String, Object> resumen = Map.of(
            "trasladosCompletados", 3,
            "recogidasCompletadas", 1L,
            "codigosLerDistintos", 1
        );
        byte[] pdf = pdfService.generarInformeFinalGestion(filas, resumen, "2025-01-01 a 2025-12-31");
        assertNotNull(pdf);
        assertTrue(pdf.length > 200);
        assertEquals('%', (char) pdf[0]);
    }

    @Test
    @DisplayName("generarInformeFinalGestion con lista vacia genera PDF sin error")
    void informeFinalGestion_listaVacia_noFalla() {
        byte[] pdf = pdfService.generarInformeFinalGestion(
            List.of(), Map.of("trasladosCompletados", 0, "recogidasCompletadas", 0L, "codigosLerDistintos", 0),
            "(todo) a (todo)");
        assertNotNull(pdf);
        assertTrue(pdf.length > 50);
    }
}
