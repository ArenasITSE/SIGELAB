package sigelab_api.excel.service;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import sigelab_api.solicitud.entity.EstadoSolicitud;
import sigelab_api.solicitud.entity.SolicitudLaboratorio;
import sigelab_api.solicitud.repository.SolicitudLaboratorioRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final SolicitudLaboratorioRepository solicitudRepository;

    @Override
    public byte[] generarExcelSolicitudes() {

        List<SolicitudLaboratorio> solicitudes =
                solicitudRepository.findAll();

        return generarExcel(
                solicitudes,
                null,
                null
        );
    }

    @Override
    public byte[] generarExcelSolicitudes(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        List<SolicitudLaboratorio> solicitudes =
                solicitudRepository.findByFechaBetween(
                        fechaInicio,
                        fechaFin
                );

        return generarExcel(
                solicitudes,
                fechaInicio,
                fechaFin
        );
    }

    private byte[] generarExcel(
            List<SolicitudLaboratorio> solicitudes,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        try (

                Workbook workbook =
                        new XSSFWorkbook();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()

        ) {

            Sheet resumen =
                    workbook.createSheet(
                            "Resumen"
                    );

            Sheet detalle =
                    workbook.createSheet(
                            "Solicitudes"
                    );

            CellStyle tituloStyle =
                    workbook.createCellStyle();

            Font tituloFont =
                    workbook.createFont();

            tituloFont.setBold(
                    true
            );

            tituloFont.setFontHeightInPoints(
                    (short) 16
            );

            tituloStyle.setFont(
                    tituloFont
            );

            CellStyle encabezadoStyle =
                    workbook.createCellStyle();

            Font encabezadoFont =
                    workbook.createFont();

            encabezadoFont.setBold(
                    true
            );

            encabezadoFont.setColor(
                    IndexedColors.WHITE.getIndex()
            );

            encabezadoStyle.setFont(
                    encabezadoFont
            );

            encabezadoStyle.setFillForegroundColor(
                    IndexedColors.DARK_BLUE.getIndex()
            );

            encabezadoStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );
                        Row titulo =
                    resumen.createRow(0);

            Cell tituloCell =
                    titulo.createCell(0);

            tituloCell.setCellValue(
                    "CRONOS LAB - REPORTE DE SOLICITUDES"
            );

            tituloCell.setCellStyle(
                    tituloStyle
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

            resumen.createRow(2)
                    .createCell(0)
                    .setCellValue(
                            "Total de solicitudes"
                    );

            resumen.getRow(2)
                    .createCell(1)
                    .setCellValue(
                            solicitudes.size()
                    );

            resumen.createRow(3)
                    .createCell(0)
                    .setCellValue(
                            "Aprobadas"
                    );

            resumen.getRow(3)
                    .createCell(1)
                    .setCellValue(
                            aprobadas
                    );

            resumen.createRow(4)
                    .createCell(0)
                    .setCellValue(
                            "Pendientes"
                    );

            resumen.getRow(4)
                    .createCell(1)
                    .setCellValue(
                            pendientes
                    );

            resumen.createRow(5)
                    .createCell(0)
                    .setCellValue(
                            "Rechazadas"
                    );

            resumen.getRow(5)
                    .createCell(1)
                    .setCellValue(
                            rechazadas
                    );

            if (fechaInicio != null
                    && fechaFin != null) {

                resumen.createRow(7)
                        .createCell(0)
                        .setCellValue(
                                "Periodo"
                        );

                resumen.getRow(7)
                        .createCell(1)
                        .setCellValue(
                                fechaInicio
                                        + " al "
                                        + fechaFin
                        );
            }

            Row encabezado =
                    detalle.createRow(0);

            String[] columnas = {
                    "ID",
                    "DOCENTE",
                    "MATERIA",
                    "LABORATORIO",
                    "FECHA",
                    "HORA INICIO",
                    "HORA FIN",
                    "ESTADO",
                    "OBSERVACIONES"
            };

            for (int i = 0;
                 i < columnas.length;
                 i++) {

                Cell cell =
                        encabezado.createCell(i);

                cell.setCellValue(
                        columnas[i]
                );

                cell.setCellStyle(
                        encabezadoStyle
                );
            }

            int fila = 1;

            CellStyle aprobadoStyle =
                    workbook.createCellStyle();

            aprobadoStyle.setFillForegroundColor(
                    IndexedColors.LIGHT_GREEN.getIndex()
            );

            aprobadoStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            CellStyle pendienteStyle =
                    workbook.createCellStyle();

            pendienteStyle.setFillForegroundColor(
                    IndexedColors.LIGHT_YELLOW.getIndex()
            );

            pendienteStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            CellStyle rechazadoStyle =
                    workbook.createCellStyle();

            rechazadoStyle.setFillForegroundColor(
                    IndexedColors.ROSE.getIndex()
            );

            rechazadoStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );
                        for (SolicitudLaboratorio s : solicitudes) {

                Row row =
                        detalle.createRow(
                                fila++
                        );

                row.createCell(0)
                        .setCellValue(
                                s.getId()
                        );

                row.createCell(1)
                        .setCellValue(
                                s.getDocente()
                                        .getNombre()
                        );

                row.createCell(2)
                        .setCellValue(
                                s.getMateria()
                                        .getNombre()
                        );

                row.createCell(3)
                        .setCellValue(
                                s.getLaboratorio()
                                        .getNombre()
                        );

                row.createCell(4)
                        .setCellValue(
                                s.getFecha()
                                        .toString()
                        );

                row.createCell(5)
                        .setCellValue(
                                s.getHoraInicio()
                                        .toString()
                        );

                row.createCell(6)
                        .setCellValue(
                                s.getHoraFin()
                                        .toString()
                        );

                Cell estadoCell =
                        row.createCell(7);

                estadoCell.setCellValue(
                        s.getEstado()
                                .name()
                );

                if (s.getEstado()
                        == EstadoSolicitud.APROBADA) {

                    estadoCell.setCellStyle(
                            aprobadoStyle
                    );

                } else if (s.getEstado()
                        == EstadoSolicitud.PENDIENTE) {

                    estadoCell.setCellStyle(
                            pendienteStyle
                    );

                } else {

                    estadoCell.setCellStyle(
                            rechazadoStyle
                    );
                }

                row.createCell(8)
                        .setCellValue(
                                s.getObservaciones() == null
                                        ? "-"
                                        : s.getObservaciones()
                        );
            }

            detalle.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0,
                            fila - 1,
                            0,
                            8
                    )
            );

            for (int i = 0;
                 i < 9;
                 i++) {

                detalle.autoSizeColumn(
                        i
                );
            }

            for (int i = 0;
                 i < 2;
                 i++) {

                resumen.autoSizeColumn(
                        i
                );
            }

            workbook.write(
                    out
            );

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al generar Excel",
                    e
            );
        }
    }
}