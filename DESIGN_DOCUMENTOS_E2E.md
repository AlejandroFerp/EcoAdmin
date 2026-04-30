# Documentos Fase 19 - Diseño Técnico

## 1. Diagrama de Entidad-Relación (Base de Datos)

```mermaid
erDiagram
    DOCUMENTOS ||--o{ TRASLADOS : "traslado_id"
    DOCUMENTOS ||--o{ CENTROS : "centro_id"
    TRASLADOS ||--|| CENTROS : "centro_productor_id"
    TRASLADOS ||--|| CENTROS : "centro_gestor_id"
    TRASLADOS ||--|| RESIDUOS : "residuo_id"
    
    DOCUMENTOS {
        bigint id PK
        varchar codigo UK
        varchar tipo ENUM "DOCUMENTO_IDENTIFICACION|NOTIFICACION_PREVIA|CONTRATO|FICHA_ACEPTACION|HOJA_SEGUIMIENTO|INFORME_FINAL|ARCHIVO_CRONOLOGICO"
        varchar estado ENUM "BORRADOR|PENDIENTE_ADJUNTO|EMITIDO|CERRADO|VENCIDO"
        varchar numero_referencia
        date fecha_emision
        date fecha_vencimiento
        date fecha_cierre
        varchar observaciones
        varchar archivo_url
        timestamp creado_en
    }
    
    TRASLADOS {
        bigint id PK
        varchar codigo UK
        varchar estado ENUM "PENDIENTE|EN_TRANSITO|COMPLETADO|RECHAZADO"
        timestamp fecha_creacion
        timestamp fecha_inicio_transporte
        timestamp fecha_entrega
        varchar observaciones
    }
    
    CENTROS {
        bigint id PK
        varchar nombre
        varchar codigo_postal
        varchar ciudad
        varchar provincia
    }
    
    RESIDUOS {
        bigint id PK
        varchar codigo_ler
        varchar descripcion
        double cantidad
        varchar unidad
        varchar estado
    }
```

---

## 2. Diagrama de Clases (Arquitectura)

```mermaid
classDiagram
    %% DTOs
    class DocumentoDraftCreateDTO {
        +tipo: TipoDocumento
        +trasladoId: Long
        +centroId: Long
        +numeroReferencia: String
        +fechaEmision: LocalDate
        +fechaVencimiento: LocalDate
        +observaciones: String
        +metadatos: Map~String,Object~
    }
    
    class DocumentoWorkflowDTO {
        +id: Long
        +codigo: String
        +tipo: TipoDocumento
        +estado: EstadoDocumento
        +numeroReferencia: String
        +requiereAdjunto: boolean
        +tieneArchivo: boolean
        +archivoUrl: String
        +siguienteAccion: String
    }
    
    %% Entity
    class Documento {
        -id: Long
        -codigo: String
        -tipo: TipoDocumento
        -estado: EstadoDocumento
        -numeroReferencia: String
        -traslado: Traslado
        -centro: Centro
        -fechaEmision: LocalDate
        -fechaVencimiento: LocalDate
        -fechaCierre: LocalDate
        -observaciones: String
        -archivoUrl: String
        -creadoEn: LocalDateTime
        +getId()
        +setTipo(TipoDocumento)
        +getEstado()
    }
    
    class Traslado {
        -id: Long
        -codigo: String
        -estado: EstadoTraslado
        -centroProductor: Centro
        -centroGestor: Centro
        -residuo: Residuo
        -transportista: Empresa
        -fechaCreacion: LocalDateTime
    }
    
    class Centro {
        -id: Long
        -nombre: String
        -ciudad: String
        -provincia: String
    }
    
    class Residuo {
        -id: Long
        -codigoLER: String
        -cantidad: Double
        -unidad: String
    }
    
    %% Controllers
    class DocumentoController {
        +crearDraft(DocumentoDraftCreateDTO): DocumentoWorkflowDTO
        +generarDocumento(Long): Map
        +uploadArchivo(Long, MultipartFile): Documento
        +obtenerWorkflow(Long): DocumentoWorkflowDTO
        -validarDraft(DocumentoDraftCreateDTO)
        -validarMetadatos(TipoDocumento, Map)
        -generarPdf(TipoDocumento, Traslado): byte[]
    }
    
    %% Services
    class DocumentoService {
        +findAll(): List~Documento~
        +findById(Long): Documento
        +save(Documento): Documento
        +delete(Long)
    }
    
    class PdfService {
        +generarCartaDePorte(Traslado): byte[]
        +generarNotificacionTraslado(Traslado): byte[]
        +generarFichaAceptacion(Traslado): byte[]
        +generarHojaSeguimiento(Traslado): byte[]
        +generarInformeDocumento(Traslado): byte[]
        +generarDocumentoContrato(Traslado): byte[]
        +generarCertificadoRecepcion(Traslado): byte[]
    }
    
    %% Repositories
    class DocumentoRepository {
        +findAll(): List~Documento~
        +findById(Long): Optional~Documento~
        +save(Documento): Documento
        +deleteById(Long)
    }
    
    %% Enums
    class TipoDocumento {
        DOCUMENTO_IDENTIFICACION
        NOTIFICACION_PREVIA
        CONTRATO
        FICHA_ACEPTACION
        HOJA_SEGUIMIENTO
        INFORME_FINAL
        ARCHIVO_CRONOLOGICO
    }
    
    class EstadoDocumento {
        BORRADOR
        PENDIENTE_ADJUNTO
        EMITIDO
        CERRADO
        VENCIDO
    }
    
    %% Relaciones
    DocumentoController --> DocumentoService
    DocumentoController --> PdfService
    DocumentoController --> DocumentoDraftCreateDTO
    DocumentoController --> DocumentoWorkflowDTO
    DocumentoService --> DocumentoRepository
    DocumentoRepository --> Documento
    Documento --> TipoDocumento
    Documento --> EstadoDocumento
    Documento --> Traslado
    Documento --> Centro
    Traslado --> Residuo
```

---

## 3. Diagrama de Flujo del Workflow (Estados y Transiciones)

```mermaid
stateDiagram-v2
    [*] --> Crear_Draft
    
    Crear_Draft --> Evaluar_Tipo
    
    Evaluar_Tipo --> Es_Generables: DI, NP, INFORME, HOJA
    Evaluar_Tipo --> Requiere_Adjunto: CONTRATO, FICHA
    
    %% Rama Generables
    Es_Generables --> Borrador
    
    Borrador --> Usuario_Inicia_Generacion
    Usuario_Inicia_Generacion --> Generar_PDF: POST /generar
    
    Generar_PDF --> Validar_Traslado: ¿tiene traslado?
    Validar_Traslado --> Error_No_Traslado: NO
    Error_No_Traslado --> [*]
    
    Validar_Traslado --> Ejecutar_GenerarPdf: SI
    Ejecutar_GenerarPdf --> PdfService_Mapeo
    
    PdfService_Mapeo --> PDF_CartaPorte: tipo=DI
    PdfService_Mapeo --> PDF_Notificacion: tipo=NP
    PdfService_Mapeo --> PDF_Ficha: tipo=FICHA
    PdfService_Mapeo --> PDF_Hoja: tipo=HOJA
    
    PDF_CartaPorte --> Persistir_Estado
    PDF_Notificacion --> Persistir_Estado
    PDF_Ficha --> Persistir_Estado
    PDF_Hoja --> Persistir_Estado
    
    Persistir_Estado --> Emitido: estado=EMITIDO
    
    Emitido --> Listo: siguiente=LISTO
    Listo --> Usuario_Descarga
    Usuario_Descarga --> Descargar_PDF: GET /archivo
    Descargar_PDF --> [*]
    
    %% Rama Requiere Adjunto
    Requiere_Adjunto --> Pendiente_Adjunto
    
    Pendiente_Adjunto --> Usuario_Selecciona_PDF
    Usuario_Selecciona_PDF --> Subir_PDF: POST /upload
    
    Subir_PDF --> Validar_PDF: ¿PDF válido?
    Validar_PDF --> Error_PDF_Invalido: NO
    Error_PDF_Invalido --> Pendiente_Adjunto
    
    Validar_PDF --> Guardar_URL: SI
    Guardar_URL --> Emitido
    
    Emitido --> Usuario_Finaliza
    Usuario_Finaliza --> [*]
```

---

## 4. Flujo End-to-End del Usuario (Escenarios)

```mermaid
sequenceDiagram
    actor User
    participant UI as documents.html
    participant API as DocumentoController
    participant Svc as DocumentoService
    participant PDF as PdfService
    participant DB as Database
    
    User->>UI: 1. Click "Nuevo Documento"
    UI->>UI: 2. Abre modal, usuario elige tipo
    UI->>UI: 3. Modal muestra campos específicos por tipo
    User->>UI: 4. Relleña formulario + metadatos
    User->>UI: 5. Click "Guardar"
    
    alt TIPO = DI (Generables)
        UI->>API: POST /api/documentos/drafts { tipo: DI, trasladoId... }
        API->>API: validarDraft() + validarMetadatos()
        API->>Svc: save(documento borrador)
        Svc->>DB: INSERT documento (estado=BORRADOR)
        DB-->>Svc: id asignado
        Svc-->>API: documento guardado
        API-->>UI: { id, estado: BORRADOR, siguienteAccion: GENERAR_PDF }
        
        UI->>UI: 6a. Muestra botón "Generar PDF"
        UI->>User: 6b. Info: "Documento creado, haz clic en Generar"
        User->>UI: 7a. Click "Generar PDF"
        
        UI->>API: POST /api/documentos/{id}/generar
        API->>Svc: findById(id)
        Svc->>DB: SELECT documento
        DB-->>Svc: documento(estado=BORRADOR)
        API->>PDF: generarCartaDePorte(traslado)
        PDF->>PDF: Construir PDF con título específico
        PDF-->>API: byte[] PDF
        API->>Svc: save(documento con estado=EMITIDO)
        Svc->>DB: UPDATE documento SET estado=EMITIDO, fechaEmision=NOW
        DB-->>Svc: updated
        Svc-->>API: documento actualizado
        API-->>UI: { pdfUrl: "/api/documentos/{id}/pdf?inline=true" }
        
        UI->>User: 8a. Notificación "PDF generado"
        UI->>UI: 8b. Actualiza tabla (fila verde, estado=EMITIDO, siguiente=LISTO)
        
    else TIPO = CONTRATO (Requiere Adjunto)
        UI->>API: POST /api/documentos/drafts { tipo: CONTRATO, centroId... }
        API->>API: validarDraft() + validarMetadatos()
        API->>Svc: save(documento)
        Svc->>DB: INSERT documento (estado=PENDIENTE_ADJUNTO)
        DB-->>Svc: id asignado
        Svc-->>API: documento guardado
        API-->>UI: { id, estado: PENDIENTE_ADJUNTO, siguienteAccion: SUBIR_PDF }
        
        UI->>UI: 6a. Muestra botón "Completar adjunto"
        UI->>User: 6b. Info: "Documento creado, adjunta PDF"
        User->>UI: 7a. Click "Completar adjunto"
        UI->>UI: 7b. FileInput dialog
        
        User->>UI: 8. Selecciona PDF local
        UI->>API: POST /api/documentos/{id}/upload + FormData(archivo)
        API->>Svc: findById(id)
        Svc->>DB: SELECT documento
        DB-->>Svc: documento(estado=PENDIENTE_ADJUNTO)
        API->>API: Guardar PDF en filesystem
        API->>API: archivoUrl = "/uploads/documentos/{nombre}"
        API->>Svc: save(documento con estado=EMITIDO, archivoUrl)
        Svc->>DB: UPDATE documento SET estado=EMITIDO, archivoUrl=...
        DB-->>Svc: updated
        Svc-->>API: documento actualizado
        API-->>UI: documento { estado: EMITIDO, archivoUrl }
        
        UI->>User: 9a. Notificación "PDF subido"
        UI->>UI: 9b. Actualiza tabla (fila verde, estado=EMITIDO, siguiente=LISTO)
    end
    
    User->>UI: 10. Click icono "Descargar" en fila
    UI->>API: GET /api/documentos/{id}/archivo
    API->>Svc: findById(id)
    Svc->>DB: SELECT documento
    DB-->>Svc: documento
    API->>API: Lee archivoUrl (si existe) o genera PDF inline
    API-->>UI: Binary PDF content
    UI->>User: 11. Descarga/abre PDF en navegador
```

---

## 5. Mapeador de Tipos a Generadores PDF

```mermaid
graph TB
    subgraph "TipoDocumento"
        DI["DOCUMENTO_IDENTIFICACION"]
        NP["NOTIFICACION_PREVIA"]
        FA["FICHA_ACEPTACION"]
        IF["INFORME_FINAL"]
        HS["HOJA_SEGUIMIENTO"]
        CO["CONTRATO"]
        AC["ARCHIVO_CRONOLOGICO"]
    end
    
    subgraph "PdfService - Método Generador"
        GCP["generarCartaDePorte()"]
        GNT["generarNotificacionTraslado()"]
        GFA["generarFichaAceptacion()"]
        GID["generarInformeDocumento()"]
        GHS["generarHojaSeguimiento()"]
        GDC["generarDocumentoContrato()"]
    end
    
    subgraph "Título PDF"
        TCP["CARTA DE PORTE"]
        TNT["NOTIFICACIÓN DE TRASLADO"]
        TFA["FICHA DE ACEPTACIÓN"]
        TID["INFORME DEL TRASLADO"]
        THS["HOJA DE SEGUIMIENTO"]
        TDC["CONTRATO / ACUERDO"]
    end
    
    DI --> GCP --> TCP
    NP --> GNT --> TNT
    FA --> GFA --> TFA
    IF --> GID --> TID
    HS --> GHS --> THS
    CO --> GDC --> TDC
    AC --> GCP --> TCP
    
    style DI fill:#e1f5e1
    style NP fill:#e1f5e1
    style FA fill:#fff4e1
    style IF fill:#fff4e1
    style HS fill:#fff4e1
    style CO fill:#ffe1e1
    style AC fill:#e1e5ff
```

**Leyenda:**
- 🟢 Verde: Documentos generables (GENERABLES)
- 🟠 Naranja: Documentos con ambigüedad actual (a refactor en segunda ola)
- 🔴 Rojo: Documentos que requieren adjunto externo (ADJUNTOS)
- 🔵 Azul: Documentos automáticos del sistema (AUTOMATICOS)

---

## 6. Validaciones y Reglas de Negocio

### Validación en Creación de Draft

```mermaid
graph TD
    A["POST /api/documentos/drafts"] --> B{"¿tipo == null?"}
    B -->|SI| B1["❌ Error: tipo requerido"]
    B -->|NO| C{"¿tipo == ARCHIVO_CRONOLOGICO?"}
    C -->|SI| C1["❌ Error: no se crea manualmente"]
    C -->|NO| D{"¿tipo IN [DI, NP]?"}
    D -->|SI| D1{"¿trasladoId != null?"}
    D1 -->|NO| D1A["❌ Error: trasladoId requerido"]
    D1 -->|SI| D1B{"¿traslado existe en BD?"}
    D1B -->|NO| D1C["❌ Error: traslado no encontrado"]
    D1B -->|SI| E
    D -->|NO| E{"¿tipo == CONTRATO?"}
    E -->|SI| E1{"¿centroId != null?"}
    E1 -->|NO| E1A["❌ Error: centroId requerido"]
    E1 -->|SI| E1B{"¿centro existe en BD?"}
    E1B -->|NO| E1C["❌ Error: centro no encontrado"]
    E1B -->|SI| F
    E -->|NO| F["Validar metadatos si existen"]
    F --> G{"¿metadatos válidos?"}
    G -->|NO| G1["❌ Error: metadatos inválidos"]
    G -->|SI| H["✅ Crear documento en BD"]
    H --> I{"¿requiereAdjuntoManual?"}
    I -->|SI| I1["estado = PENDIENTE_ADJUNTO"]
    I -->|NO| I2["estado = BORRADOR"]
    I1 --> J["Retornar DocumentoWorkflowDTO"]
    I2 --> J
```

---

## 7. Test E2E - Coverage

El archivo `DocumentoE2ETest.java` cubre:

| Escenario | Test | Validaciones |
|-----------|------|--------------|
| Crear + generar DI | `testCrearYGenerarDI()` | Draft → BORRADOR → generar → EMITIDO |
| Crear + generar NP | `testCrearYGenerarNP()` | NP con metadatos → generación |
| Crear CONTRATO | `testCrearContratoConAdjunto()` | PENDIENTE_ADJUNTO → NO permite generar |
| Validación de requeridos | `testDIsinTrasladoFalla()` | DI sin trasladoId rechazada |
| ARCHIVO_CRONOLOGICO | `testArchivoCronologicoNoSeCreaManu()` | No se crea manualmente |
| Listar documentos | `testListarDocumentos()` | Múltiples estados visibles |
| Workflow endpoint | `testWorkflowEndpoint()` | API devuelve siguiente acción |
| Validación estricta de metadatos | `testValidacionMetadatosEstrict()` | cantidad debe ser numérica |
| Flujo completo E2E | `testFlujoCompletoE2E()` | crear → generar → descargar → BD |

---

## 8. Notas de Implementación

### Cambios Realizados

1. **PdfService.java**
   - Agregados 4 nuevos métodos públicos (generarFichaAceptacion, generarHojaSeguimiento, generarInformeDocumento, generarDocumentoContrato)
   - Cada método genera PDF con título específico

2. **DocumentoController.java**
   - Nuevo endpoint `POST /api/documentos/drafts` para crear drafts
   - Nuevo endpoint `POST /api/documentos/{id}/generar` para generar PDFs
   - Nuevo endpoint `GET /api/documentos/{id}/workflow` para obtener estado
   - Métodos privados `validarDraft()` y `validarMetadatos()` para validación estricta
   - `generarPdf()` remapped a métodos específicos por tipo

3. **EstadoDocumento.java**
   - Agregado estado `PENDIENTE_ADJUNTO` entre BORRADOR y EMITIDO

4. **DocumentoDraftCreateDTO.java** (nuevo record)
   - Payload para creación de draft con metadatos

5. **DocumentoWorkflowDTO.java** (nuevo record)
   - Respuesta de workflow con `siguienteAccion` clara

6. **documents.html**
   - Modal con campos dinámicos por tipo
   - Secciones de metadatos ocultas/mostradas según tipo
   - Botones de acciones ("Generar PDF", "Completar adjunto")
   - Columna "Siguiente" en tabla
   - Lógica `actualizarFormularioSegunTipo()` y `leerMetadatosPorTipo()`

### Pendiente de Segunda Ola

- [ ] Persistencia de `metadatos` como JSON en BD (tabla `documentos.metadatos TEXT`)
- [ ] Backend consume metadatos para ciertos tipos (ej. DI: calcula cantidad desde traslado + metadatos)
- [ ] UI avanzada: paso a paso wizard (tipo → formulario → adjunto/generar)
- [ ] Soporte para editar documento tras creación
- [ ] Auditoría: tabla `documento_eventos` para rastrear cambios de estado
