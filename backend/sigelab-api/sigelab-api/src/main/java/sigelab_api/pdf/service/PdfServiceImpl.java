package sigelab_api.pdf.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import sigelab_api.pdf.config.FooterPageEvent;
import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.entity.SolicitudLaboratorio;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public byte[] generarReporteSolicitudes() {

        List<SolicitudLaboratorio> solicitudes =
                solicitudRepository.findAll();

        return generarPdf(
                solicitudes,
                null,
                null
        );
    }

    @Override
    public byte[] generarReporteSolicitudes(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        List<SolicitudLaboratorio> solicitudes =
                solicitudRepository.findByFechaBetween(
                        fechaInicio,
                        fechaFin
                );

        return generarPdf(
                solicitudes,
                fechaInicio,
                fechaFin
        );
    }

    private byte[] generarPdf(
            List<SolicitudLaboratorio> solicitudes,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        try {

            Document document =
                    new Document(
                            PageSize.A4.rotate(),
                            40,
                            40,
                            50,
                            50
                    );

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            PdfWriter writer =
                    PdfWriter.getInstance(
                            document,
                            out
                    );

            writer.setPageEvent(
                    new FooterPageEvent()
            );

            document.open();

            Color azulTecnm =
                    new Color(0, 70, 127);

            Color grisClaro =
                    new Color(240, 240, 240);

            Font tituloFont =
                    new Font(
                            Font.HELVETICA,
                            20,
                            Font.BOLD
                    );

            Font subtituloFont =
                    new Font(
                            Font.HELVETICA,
                            12,
                            Font.NORMAL
                    );

            Font encabezadoTabla =
                    new Font(
                            Font.HELVETICA,
                            11,
                            Font.BOLD,
                            Color.WHITE
                    );

            Font textoNormal =
                    new Font(
                            Font.HELVETICA,
                            10,
                            Font.NORMAL
                    );
                                PdfPTable logos =
                    new PdfPTable(3);

            logos.setWidthPercentage(100);
            logos.setWidths(
                    new float[]{1, 3, 1}
            );

            PdfPCell izquierda =
                    new PdfPCell();

            izquierda.setBorder(
                    Rectangle.NO_BORDER
            );

            PdfPCell centro =
                    new PdfPCell();

            centro.setBorder(
                    Rectangle.NO_BORDER
            );

            PdfPCell derecha =
                    new PdfPCell();

            derecha.setBorder(
                    Rectangle.NO_BORDER
            );

            try {

                Image logoITSE =
                        Image.getInstance(
                                new ClassPathResource(
                                        "static/images/logo-itse.png"
                                ).getURL()
                        );

                logoITSE.scaleToFit(
                        90,
                        90
                );

                izquierda.addElement(
                        logoITSE
                );

            } catch (Exception ignored) {
            }

            try {

                Image logoCronos =
                        Image.getInstance(
                                new ClassPathResource(
                                        "static/images/logo-chronoslab.png"
                                ).getURL()
                        );

                logoCronos.scaleToFit(
                        90,
                        90
                );

                derecha.addElement(
                        logoCronos
                );

            } catch (Exception ignored) {
            }

            Paragraph institucion =
                    new Paragraph(
                            "INSTITUTO TECNOLÓGICO SUPERIOR DE ESCÁRCEGA",
                            tituloFont
                    );

            institucion.setAlignment(
                    Element.ALIGN_CENTER
            );

            Paragraph sistema =
                    new Paragraph(
                            "CRONOS LAB - Sistema de Gestión de Laboratorios",
                            subtituloFont
                    );

            sistema.setAlignment(
                    Element.ALIGN_CENTER
            );

            centro.addElement(
                    institucion
            );

            centro.addElement(
                    sistema
            );

            logos.addCell(
                    izquierda
            );

            logos.addCell(
                    centro
            );

            logos.addCell(
                    derecha
            );

            document.add(
                    logos
            );

            document.add(
                    new Paragraph(" ")
            );

            Paragraph reporte =
                    new Paragraph(
                            "REPORTE GENERAL DE SOLICITUDES",
                            tituloFont
                    );

            reporte.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(
                    reporte
            );

            document.add(
                    new Paragraph(" ")
            );

            LocalDateTime ahora =
                    LocalDateTime.now();

            DateTimeFormatter formato =
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy HH:mm:ss"
                    );

            Paragraph fechaGeneracion =
                    new Paragraph(
                            "Fecha de generación: "
                                    + ahora.format(formato),
                            textoNormal
                    );

            document.add(
                    fechaGeneracion
            );

            if (fechaInicio != null
                    && fechaFin != null) {

                Paragraph periodo =
                        new Paragraph(
                                "Periodo seleccionado: "
                                        + fechaInicio
                                        + " al "
                                        + fechaFin,
                                textoNormal
                        );

                document.add(
                        periodo
                );
            }

            document.add(
                    new Paragraph(" ")
            );

            long aprobadas =
                    solicitudes.stream()
                            .filter(
                                    s -> s.getEstado()
                                            == EstadoSolicitud.APROBADA
                            )
                            .count();

            long pendientes =
                    solicitudes.stream()
                            .filter(
                                    s -> s.getEstado()
                                            == EstadoSolicitud.PENDIENTE
                            )
                            .count();

            long rechazadas =
                    solicitudes.stream()
                            .filter(
                                    s -> s.getEstado()
                                            == EstadoSolicitud.RECHAZADA
                            )
                            .count();

            Paragraph resumenTitulo =
                    new Paragraph(
                            "RESUMEN EJECUTIVO",
                            new Font(
                                    Font.HELVETICA,
                                    14,
                                    Font.BOLD
                            )
                    );

            resumenTitulo.setAlignment(
                    Element.ALIGN_LEFT
            );

            document.add(
                    resumenTitulo
            );

            document.add(
                    new Paragraph(" ")
            );

            PdfPTable resumen =
                    new PdfPTable(2);

            resumen.setWidthPercentage(
                    35
            );

            resumen.setHorizontalAlignment(
                    Element.ALIGN_LEFT
            );

            resumen.addCell(
                    "Indicador"
            );

            resumen.addCell(
                    "Cantidad"
            );

            resumen.addCell(
                    "Total"
            );

            resumen.addCell(
                    String.valueOf(
                            solicitudes.size()
                    )
            );

            resumen.addCell(
                    "Aprobadas"
            );

            resumen.addCell(
                    String.valueOf(
                            aprobadas
                    )
            );

            resumen.addCell(
                    "Pendientes"
            );

            resumen.addCell(
                    String.valueOf(
                            pendientes
                    )
            );

            resumen.addCell(
                    "Rechazadas"
            );

            resumen.addCell(
                    String.valueOf(
                            rechazadas
                    )
            );

            document.add(
                    resumen
            );

            document.add(
                    new Paragraph(" ")
            );
                        PdfPTable tabla =
                    new PdfPTable(8);

            tabla.setWidthPercentage(
                    100
            );

            tabla.setWidths(
                    new float[]{
                            0.8f,
                            2.5f,
                            2.5f,
                            2.5f,
                            1.4f,
                            1.8f,
                            1.3f,
                            3.0f
                    }
            );

            agregarEncabezado(
                    tabla,
                    "ID",
                    encabezadoTabla,
                    azulTecnm
            );

            agregarEncabezado(
                    tabla,
                    "DOCENTE",
                    encabezadoTabla,
                    azulTecnm
            );

            agregarEncabezado(
                    tabla,
                    "MATERIA",
                    encabezadoTabla,
                    azulTecnm
            );

            agregarEncabezado(
                    tabla,
                    "LABORATORIO",
                    encabezadoTabla,
                    azulTecnm
            );

            agregarEncabezado(
                    tabla,
                    "FECHA",
                    encabezadoTabla,
                    azulTecnm
            );

            agregarEncabezado(
                    tabla,
                    "HORARIO",
                    encabezadoTabla,
                    azulTecnm
            );

            agregarEncabezado(
                    tabla,
                    "ESTADO",
                    encabezadoTabla,
                    azulTecnm
            );

            agregarEncabezado(
                    tabla,
                    "OBSERVACIONES",
                    encabezadoTabla,
                    azulTecnm
            );

            boolean alternarColor =
                    false;

            for (SolicitudLaboratorio s : solicitudes) {

                Color colorFila =
                        alternarColor
                                ? Color.WHITE
                                : grisClaro;

                PdfPCell id =
                        new PdfPCell(
                                new Phrase(
                                        String.valueOf(
                                                s.getId()
                                        ),
                                        textoNormal
                                )
                        );

                id.setBackgroundColor(
                        colorFila
                );

                tabla.addCell(
                        id
                );

                PdfPCell docente =
                        new PdfPCell(
                                new Phrase(
                                        s.getDocente()
                                                .getNombre(),
                                        textoNormal
                                )
                        );

                docente.setBackgroundColor(
                        colorFila
                );

                tabla.addCell(
                        docente
                );

                PdfPCell materia =
                        new PdfPCell(
                                new Phrase(
                                        s.getMateria()
                                                .getNombre(),
                                        textoNormal
                                )
                        );

                materia.setBackgroundColor(
                        colorFila
                );

                tabla.addCell(
                        materia
                );

                PdfPCell laboratorio =
                        new PdfPCell(
                                new Phrase(
                                        s.getLaboratorio()
                                                .getNombre(),
                                        textoNormal
                                )
                        );

                laboratorio.setBackgroundColor(
                        colorFila
                );

                tabla.addCell(
                        laboratorio
                );

                PdfPCell fecha =
                        new PdfPCell(
                                new Phrase(
                                        s.getFecha()
                                                .toString(),
                                        textoNormal
                                )
                        );

                fecha.setBackgroundColor(
                        colorFila
                );

                tabla.addCell(
                        fecha
                );

                PdfPCell horario =
                        new PdfPCell(
                                new Phrase(
                                        s.getHoraInicio()
                                                + " - "
                                                + s.getHoraFin(),
                                        textoNormal
                                )
                        );

                horario.setBackgroundColor(
                        colorFila
                );

                tabla.addCell(
                        horario
                );

                PdfPCell estado =
                        new PdfPCell(
                                new Phrase(
                                        s.getEstado()
                                                .name(),
                                        textoNormal
                                )
                        );

                estado.setHorizontalAlignment(
                        Element.ALIGN_CENTER
                );

                if (s.getEstado()
                        == EstadoSolicitud.APROBADA) {

                    estado.setBackgroundColor(
                            new Color(
                                    198,
                                    239,
                                    206
                            )
                    );

                } else if (s.getEstado()
                        == EstadoSolicitud.PENDIENTE) {

                    estado.setBackgroundColor(
                            new Color(
                                    255,
                                    235,
                                    156
                            )
                    );

                } else {

                    estado.setBackgroundColor(
                            new Color(
                                    255,
                                    199,
                                    206
                            )
                    );

                }

                tabla.addCell(
                        estado
                );

                PdfPCell observaciones =
                        new PdfPCell(
                                new Phrase(
                                        s.getObservaciones() == null
                                                ? "-"
                                                : s.getObservaciones(),
                                        textoNormal
                                )
                        );

                observaciones.setBackgroundColor(
                        colorFila
                );

                tabla.addCell(
                        observaciones
                );

                alternarColor =
                        !alternarColor;
            }

            document.add(
                    tabla
            );

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(" ")
            );
                        Paragraph firma =
                    new Paragraph(
                            "________________________________________\n"
                                    + "CRONOS LAB\n"
                                    + "Sistema de Gestión de Laboratorios",
                            new Font(
                                    Font.HELVETICA,
                                    11,
                                    Font.BOLD
                            )
                    );

            firma.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(
                    firma
            );

            document.add(
                    new Paragraph(" ")
            );

            Font pieFont =
                    new Font(
                            Font.HELVETICA,
                            9,
                            Font.ITALIC
                    );

            Paragraph pie =
                    new Paragraph(
                            "Documento generado automáticamente por CRONOS LAB - Instituto Tecnológico Superior de Escárcega",
                            pieFont
                    );

            pie.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(
                    pie
            );

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al generar PDF",
                    e
            );
        }
    }

    private void agregarEncabezado(
            PdfPTable tabla,
            String texto,
            Font font,
            Color color
    ) {

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                texto,
                                font
                        )
                );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setPadding(
                8
        );

        cell.setBackgroundColor(
                color
        );

        tabla.addCell(
                cell
        );
    }
}