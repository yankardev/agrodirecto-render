package com.agrodirecto.pedido.report;

import com.agrodirecto.common.exception.BusinessException;
import com.agrodirecto.pedido.report.ReporteDtos.Tabla;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Convierte una {@link Tabla} en PDF, Excel (.xlsx) o CSV. */
@Component
public class ExportadorReportes {

    public byte[] pdf(Tabla t) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 40, 36);
            PdfWriter.getInstance(doc, salida);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0x2F, 0x6B, 0x3A));
            Font sub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
            Font cab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cuerpo = FontFactory.getFont(FontFactory.HELVETICA, 9);

            doc.add(new Paragraph("AgroDirecto - " + t.titulo(), titulo));
            Paragraph fecha = new Paragraph("Generado el "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), sub);
            fecha.setSpacingAfter(12);
            doc.add(fecha);

            PdfPTable tabla = new PdfPTable(t.cabeceras().size());
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);
            for (String c : t.cabeceras()) {
                PdfPCell celda = new PdfPCell(new Phrase(c, cab));
                celda.setBackgroundColor(new Color(0x2F, 0x6B, 0x3A));
                celda.setPadding(5);
                tabla.addCell(celda);
            }
            int n = 0;
            for (List<String> fila : t.filas()) {
                for (String valor : fila) {
                    PdfPCell celda = new PdfPCell(new Phrase(valor == null ? "" : valor, cuerpo));
                    celda.setPadding(4);
                    if (n % 2 == 1) {
                        celda.setBackgroundColor(new Color(0xF1, 0xF4, 0xEC));
                    }
                    tabla.addCell(celda);
                }
                n++;
            }
            if (t.filas().isEmpty()) {
                PdfPCell vacio = new PdfPCell(new Phrase("Sin datos para los filtros elegidos.", cuerpo));
                vacio.setColspan(t.cabeceras().size());
                vacio.setHorizontalAlignment(Element.ALIGN_CENTER);
                vacio.setPadding(10);
                tabla.addCell(vacio);
            }
            doc.add(tabla);
            doc.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el PDF.");
        }
    }

    public byte[] excel(Tabla t) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");
            CellStyle negrita = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuente = libro.createFont();
            fuente.setBold(true);
            negrita.setFont(fuente);

            Row cab = hoja.createRow(0);
            for (int i = 0; i < t.cabeceras().size(); i++) {
                Cell c = cab.createCell(i);
                c.setCellValue(t.cabeceras().get(i));
                c.setCellStyle(negrita);
            }
            int r = 1;
            for (List<String> fila : t.filas()) {
                Row row = hoja.createRow(r++);
                for (int i = 0; i < fila.size(); i++) {
                    row.createCell(i).setCellValue(fila.get(i));
                }
            }
            for (int i = 0; i < t.cabeceras().size(); i++) {
                hoja.setColumnWidth(i, 6000);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el Excel.");
        }
    }

    /** CSV con BOM UTF-8 para que Excel muestre bien las tildes. */
    public byte[] csv(Tabla t) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(String.join(",", t.cabeceras().stream().map(ExportadorReportes::celda).toList())).append("\r\n");
        for (List<String> fila : t.filas()) {
            sb.append(String.join(",", fila.stream().map(ExportadorReportes::celda).toList())).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String celda(String valor) {
        String v = valor == null ? "" : valor;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
