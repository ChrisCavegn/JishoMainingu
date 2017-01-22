package jishoMainingu.function.excel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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
import jishoMainingu.backend.jisho.model.SenseDto;
import jishoMainingu.function.logging.LogEntry;
import jishoMainingu.function.logging.Logging;
import jishoMainingu.function.specification.DataSpecification;

@Named
public class ExcelWriter {

	public ByteArrayOutputStream createWorkbook(String keyword, List<DataDto> allData, DataSpecification specification, Logging logging) throws Exception {

		HSSFWorkbook workbook = new HSSFWorkbook();
		
		LogEntry entry = logging.createEntry("Schreibe Excel");
		createDataSheet(keyword, specification, allData, workbook);
		entry.success();
		
		createLoggingSheet(logging, workbook);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		return outputStream;
	}

	private void createDataSheet(String keyword, DataSpecification specification, List<DataDto> allData, HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet("Suchbegriff " + keyword);

		ExcelSpecification excel = getExcelSpecification(specification);

		int rowNum = 0;
		addHeader(excel, workbook, sheet, rowNum++);
		addContent(allData, excel, sheet, rowNum);
	}

	private ExcelSpecification getExcelSpecification(DataSpecification specification) {
		int kanjiStart = 0;
		int readingStart = kanjiStart + specification.getMaxKanjiCount();
		int englishDefinitionStart = readingStart + specification.getMaxReadingCount();
		int multipleKanji = englishDefinitionStart + 3;
		ExcelSpecification excelSpecification = new ExcelSpecification(kanjiStart, readingStart, englishDefinitionStart, multipleKanji);
		return excelSpecification;
	}

	private void addHeader(ExcelSpecification excel, HSSFWorkbook workbook, HSSFSheet sheet, int rowNum) {
		Row headerRow = sheet.createRow(rowNum);

		for (int i = excel.getKanjiStart(); i < excel.getReadingStart(); i++) {
			addHeaderCell(i, "Kanji " + i, workbook, headerRow);
		}

		for (int i = excel.getReadingStart(); i < excel.getEnglishDefinitionStart(); i++) {
			int readingIndex = i - excel.getReadingStart();
			addHeaderCell(i, "Reading " + readingIndex, workbook, headerRow);
		}

		for (int i = excel.getEnglishDefinitionStart(); i < excel.getMultipleKanji(); i++) {
			int englishDefinitionIndex = i - excel.getEnglishDefinitionStart();
			addHeaderCell(i, "English Definition " + englishDefinitionIndex, workbook, headerRow);
		}

		Cell lastCell = addHeaderCell(excel.getMultipleKanji(), "multiple_kanji", workbook, headerRow);
		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:" + lastCell.getAddress()));
	}

	private void addContent(List<DataDto> allData, ExcelSpecification excel, HSSFSheet sheet, int rowNum) {
		for (DataDto data : allData) {
			Row row = sheet.createRow(rowNum++);

			// Add Kanji's
			int kanjiIndex = excel.getKanjiStart();
			for (JapaneseDto japanese : data.getJapanese()) {
				if (!StringUtils.isEmpty(japanese.getWord())) {
					Cell kanjiCell = row.createCell(kanjiIndex);
					kanjiCell.setCellValue(japanese.getWord());

					kanjiIndex++;
				}
			}

			// Add Readings
			int readingIndex = excel.getReadingStart();
			for (JapaneseDto japanese : data.getJapanese()) {
				if (!StringUtils.isEmpty(japanese.getReading())) {
					Cell readingCell = row.createCell(readingIndex);
					readingCell.setCellValue(japanese.getReading());

					readingIndex++;
				}
			}

			// Add Englisch Definitions
			if (data.getSenses().size() > 0) {
				SenseDto sense = data.getSenses().get(0);

				Cell firstEnglishWordCell = row.createCell(excel.getEnglishDefinitionStart() + 0);
				firstEnglishWordCell.setCellValue(sense.getEnglish_definitions().get(0));

				if (sense.getEnglish_definitions().size() > 1) {
					List<String> englishDefinitionsWithoutFirst = new ArrayList<>(sense.getEnglish_definitions());
					englishDefinitionsWithoutFirst.remove(0);

					String englishWordList = StringUtils.collectionToDelimitedString(englishDefinitionsWithoutFirst, ", ");
					Cell secondEnglishWordCell = row.createCell(excel.getEnglishDefinitionStart() + 1);
					secondEnglishWordCell.setCellValue(englishWordList);
				}

			}
			if (data.getSenses().size() > 1) {
				List<String> allEnglishDefinitions = new ArrayList<>();
				for (int i = 1; i < data.getSenses().size(); i++) {
					SenseDto sense = data.getSenses().get(i);
					allEnglishDefinitions.addAll(sense.getEnglish_definitions());
				}
				String englishWordList = StringUtils.collectionToDelimitedString(allEnglishDefinitions, ", ");
				Cell thirdEnglishWordCell = row.createCell(excel.getEnglishDefinitionStart() + 2);
				thirdEnglishWordCell.setCellValue(englishWordList);
			}

			// Multiple Kanji
			Cell multipleKanjiCell = row.createCell(excel.getMultipleKanji());
			multipleKanjiCell.setCellValue(kanjiIndex - excel.getKanjiStart() > 1 ? 1 : 0);
		}

		for (int i = 0; i <= excel.getMultipleKanji(); i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private Cell addHeaderCell(int index, String title, HSSFWorkbook workbook, Row headerRow) {
		HSSFFont headerFont = workbook.createFont();
		headerFont.setBold(true);

		HSSFCellStyle style = workbook.createCellStyle();
		style.setFont(headerFont);

		Cell wordCell = headerRow.createCell(index);
		wordCell.setCellValue(title);
		wordCell.setCellStyle(style);

		return wordCell;
	}

	private void createLoggingSheet(Logging logging, HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet("Logging");

		Row headerRow = sheet.createRow(0);
		addHeaderCell(0, "StartedAt", workbook, headerRow);
		addHeaderCell(1, "Message", workbook, headerRow);
		addHeaderCell(2, "EndedAt", workbook, headerRow);
		addHeaderCell(3, "Duration", workbook, headerRow);
		addHeaderCell(4, "Status", workbook, headerRow);
		addHeaderCell(5, "Detail", workbook, headerRow);

		int rownum = 1;
		for (LogEntry entry : logging.getLogs()) {
			Row row = sheet.createRow(rownum++);

			if (entry.getStartdAt() != null) {
				Cell startedAtCell = row.createCell(0);
				startedAtCell.setCellValue(entry.getStartdAt().toString());
			}
			if (entry.getMessage() != null) {
				Cell messageCell = row.createCell(1);
				messageCell.setCellValue(entry.getMessage());
			}
			if (entry.getEndedAt() != null) {
				Cell endedAtCell = row.createCell(2);
				endedAtCell.setCellValue(entry.getEndedAt().toString());
			}
			if (entry.getDuration() != null) {
				Cell endedAtCell = row.createCell(3);
				endedAtCell.setCellValue(entry.getDuration().toMillis());
			}
			if (entry.getStatus() != null) {
				Cell statusCell = row.createCell(4);
				statusCell.setCellValue(entry.getStatus());
			}
			if (entry.getStatusDetail() != null) {
				Cell statusDetailCell = row.createCell(5);
				statusDetailCell.setCellValue(entry.getStatusDetail());
			}
		}

		for (int i = 0; i <= 5; i++) {
			sheet.autoSizeColumn(i);
		}

	}
}
