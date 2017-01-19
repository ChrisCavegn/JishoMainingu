package jishoMainingu.function.excel;

import java.io.ByteArrayOutputStream;

import javax.inject.Named;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.StringUtils;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.backend.jisho.model.JapaneseDto;
import jishoMainingu.backend.jisho.model.ResultDto;
import jishoMainingu.backend.jisho.model.SenseDto;

@Named
public class ExcelWriter {
	private static final int JAPANESE_WORD = 0;
	private static final int JAPANESE_READING = 1;
	private static final int ENGLISH_DEFINITION_LIST = 2;

	public ByteArrayOutputStream createWorkbook(String keyword, ResultDto result) throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Suchbegriff " + keyword);

		int rowNum = 0;
		Row headerRow = sheet.createRow(rowNum++);
		
		addHeaderCell(JAPANESE_WORD, "Japanese Word", workbook, headerRow);
		addHeaderCell(JAPANESE_READING, "Japanese Reading", workbook, headerRow);
		addHeaderCell(ENGLISH_DEFINITION_LIST, "English Definitions", workbook, headerRow);
		
		for (DataDto data : result.getData()) {
			for (JapaneseDto japanese : data.getJapanese()) {
				for (SenseDto sense : data.getSenses()) {
					Row row = sheet.createRow(rowNum++);

					Cell japaneseWordCell = row.createCell(JAPANESE_WORD);
					japaneseWordCell.setCellValue(japanese.getWord());

					Cell japaneseReadingCell = row.createCell(JAPANESE_READING);
					japaneseReadingCell.setCellValue(japanese.getReading());

					String englishWordList = StringUtils.collectionToDelimitedString(sense.getEnglish_definitions(), ",");
					Cell englishWordCell = row.createCell(ENGLISH_DEFINITION_LIST);
					englishWordCell.setCellValue(englishWordList);
				}
			}
		}

		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:C1"));
		
		sheet.autoSizeColumn(JAPANESE_WORD);
		sheet.autoSizeColumn(JAPANESE_READING);
		sheet.autoSizeColumn(ENGLISH_DEFINITION_LIST);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		return outputStream;
	}

	private void addHeaderCell(int index, String title, HSSFWorkbook workbook, Row headerRow) {
		HSSFFont headerFont = workbook.createFont();
		headerFont.setBold(true);

		HSSFCellStyle style = workbook.createCellStyle();
		style.setFont(headerFont);
		
		Cell wordCell = headerRow.createCell(index);
		wordCell.setCellValue("Japanese Word");
		wordCell.setCellStyle(style);
	}
}
