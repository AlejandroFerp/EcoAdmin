package com.alejandrofernandez.ecoadmin.servicios;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alejandrofernandez.ecoadmin.modelo.Documento;
import com.alejandrofernandez.ecoadmin.modelo.Traslado;
import com.alejandrofernandez.ecoadmin.modelo.enums.EstadoDocumento;
import com.alejandrofernandez.ecoadmin.modelo.enums.TipoDocumento;
import com.alejandrofernandez.ecoadmin.repository.DocumentoRepository;
import com.alejandrofernandez.ecoadmin.repository.TrasladoRepository;

@Service
public class DocumentoServiceDB implements DocumentoService {

    private final DocumentoRepository repo;
    private final TrasladoRepository trasladoRepo;

    public DocumentoServiceDB(DocumentoRepository repo, TrasladoRepository trasladoRepo) {
        this.repo = repo;
        this.trasladoRepo = trasladoRepo;
    }

    @Override
    public List<Documento> findAll() {
        return repo.findAll();
    }

    @Override
    public Documento findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Documento save(Documento d) {
        CodigoInmutableSupport.conservarSiAusente(d.getId(), d.getCodigo(), repo::findById, Documento::getCodigo, d::setCodigo);
        if (d.getTraslado() != null && d.getTraslado().getId() != null)
            d.setTraslado(trasladoRepo.findById(d.getTraslado().getId()).orElse(null));
        return repo.save(d);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public List<Documento> findByTraslado(Traslado traslado) {
        return repo.findByTrasladoOrderByCreadoEnDesc(traslado);
    }

    @Override
    public Documento generarDiParaTraslado(Traslado traslado) {
        String anio = String.valueOf(LocalDate.now().getYear());
        long count = repo.count() + 1;
        String referencia = "DI-" + anio + "-" + String.format("%03d", count);

        String ler = traslado.getResiduo() != null ? traslado.getResiduo().getCodigoLER() : "SIN-LER";
        String origen = traslado.getCentroProductor() != null
                ? traslado.getCentroProductor().getNombre().replaceAll("\\s+", "")
                : "Origen";
        String destino = traslado.getCentroGestor() != null
                ? traslado.getCentroGestor().getNombre().replaceAll("\\s+", "")
                : "Destino";

        String fechaStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nombreArchivo = fechaStr + "_DI_" + origen + "-" + ler + "_" + destino + ".pdf";

        Documento doc = new Documento(TipoDocumento.DOCUMENTO_IDENTIFICACION, traslado, referencia);
        doc.setEstado(EstadoDocumento.EMITIDO);
        doc.setObservaciones("DI generado automaticamente al completar traslado #" + traslado.getId()
                + ". Archivo: " + nombreArchivo);
        return repo.save(doc);
    }

    @Override
    public boolean existeDiParaTraslado(Traslado traslado) {
        return repo.existsByTrasladoAndTipo(traslado, TipoDocumento.DOCUMENTO_IDENTIFICACION);
    }
}
