package com.iesdoctorbalmis.spring.servicios;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@Service
public class QrService {

    private static final int TAMANO_PX = 300;

    public byte[] generarQrTraslado(Long idTraslado, String baseUrl) {
        String contenido = baseUrl + "/api/traslados/" + idTraslado;
        return generarQr(contenido);
    }

    public byte[] generarQr(String contenido) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, TAMANO_PX, TAMANO_PX, hints);

            BufferedImage imagen = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(imagen, "PNG", out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando QR para contenido: " + contenido, e);
        }
    }
}
