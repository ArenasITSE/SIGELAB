package sigelab_api.pdf.controller;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import sigelab_api.pdf.service.PdfService;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('COORDINADOR','ADMINISTRADOR')")
    public ResponseEntity<byte[]> descargarPdf() {

        byte[] pdf =
                pdfService.generarReporteSolicitudes();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=solicitudes.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }

    @GetMapping("/solicitudes/rango")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','COORDINADOR')")
public ResponseEntity<byte[]> descargarPdfPorRango(

        @RequestParam LocalDate fechaInicio,
        @RequestParam LocalDate fechaFin

) {

    byte[] pdf =
            pdfService.generarReporteSolicitudes(
                    fechaInicio,
                    fechaFin
            );

    HttpHeaders headers =
            new HttpHeaders();

    headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=ReporteSolicitudes.pdf"
    );

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(
                    MediaType.APPLICATION_PDF
            )
            .body(pdf);
}
}