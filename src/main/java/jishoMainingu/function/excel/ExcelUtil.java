package jishoMainingu.function.excel;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.inject.Named;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.util.StringUtils;

/**
 * Stellt allgemeine Funktionalität für das Befüllen eines Excel's bereit.
 * 
 * @author ChrisCavegn
 */
@Named
public class ExcelUtil {

	/**
	 * Fügt der Header-Zeile eine einzelne Zelle hinzu.
	 * 
	 * @param index Position, an der die Zelle hinzugefügt werden soll
	 * @param title Inhalt der Zelle
	 * @param workbook Das Workbook, wird zum Erzeugen von Schriftart und Zelle benötigt
	 * @param headerRow Die Header-Zeile
	 * @return Die erzeugte Zelle
	 */
	public Cell addHeaderCell(int index, String title, HSSFWorkbook workbook, Row headerRow) {
		HSSFFont headerFont = workbook.createFont();
		headerFont.setBold(true);

		HSSFCellStyle style = workbook.createCellStyle();
		style.setFont(headerFont);

		Cell wordCell = headerRow.createCell(index);
		wordCell.setCellValue(title);
		wordCell.setCellStyle(style);

		return wordCell;
	}

	/**
	 * Erzeugt eine neue Zelle in der übergebenen Row und setzt den Wert.
	 * 
	 * @param row Die Row
	 * @param column Identifiziert die Spalte der Zelle
	 * @param value Der Wert
	 */
	public void addContentCell(Row row, int column, String value) {
		if (!StringUtils.isEmpty(value)) {
			Cell cell = row.createCell(column);
			cell.setCellValue(value);
		}
	}

	/**
	 * Erzeugt eine neue Zelle in der übergebenen Row und setzt den Wert.
	 * 
	 * @param row Die Row
	 * @param column Identifiziert die Spalte der Zelle
	 * @param value Der Wert
	 */
	public void addContentCell(Row row, int column, int value) {
		Cell cell = row.createCell(column);
		cell.setCellValue(value);
	}

	/**
	 * Erzeugt eine neue Zelle in der übergebenen Row und setzt den Wert.
	 * 
	 * @param row Die Row
	 * @param column Identifiziert die Spalte der Zelle
	 * @param value Der Wert
	 */
	public void addContentCell(Row row, int column, Duration value) {
		if (value != null) {
			Cell cell = row.createCell(column);
			cell.setCellValue(value.toMillis());
		}
	}

	/**
	 * Erzeugt eine neue Zelle in der übergebenen Row und setzt den Wert.
	 * 
	 * @param row Die Row
	 * @param column Identifiziert die Spalte der Zelle
	 * @param value Der Wert
	 */
	public void addContentCell(Row row, int column, LocalDateTime value) {
		if (value != null) {
			Cell cell = row.createCell(column);
			cell.setCellValue(value.toString());
		}
	}
}
