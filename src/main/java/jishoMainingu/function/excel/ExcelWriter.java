package jishoMainingu.function.excel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.StringUtils;

import jishoMainingu.function.excel.model.ExcelEntry;
import jishoMainingu.function.logging.LogEntry;
import jishoMainingu.function.logging.Logging;
import jishoMainingu.function.specification.DataSpecification;

@Named
public class ExcelWriter {

	@Inject
	private ExcelUtil excelUtil;

	/**
	 * Erzeugt aus den übergebenen Daten ein Excel-File.
	 * 
	 * @param keyword Das Keyword, nach dem gesucht wurde.
	 * @param someEntries Die zum Keyword ermittelten Daten
	 * @param specification Die Spezifikation der Daten (Anzahl Kanji, Reading)
	 * @param logging Das Logging-Objekt
	 * @return Ein OutputStream, welcher das Excel-File enthält
	 * @throws Exception Wird im Fehlerfall geworfen
	 */
	public ByteArrayOutputStream createWorkbook(String keyword, List<ExcelEntry> someEntries,
			DataSpecification specification, Logging logging) throws Exception {

		HSSFWorkbook workbook = new HSSFWorkbook();

		LogEntry entry = logging.createEntry("Schreibe Excel");
		createDataSheet(keyword, someEntries, specification, workbook);
		entry.success();

		createLoggingSheet(logging, workbook);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		return outputStream;
	}

	/**
	 * Erzeugt das Sheet, welches die abgefragten/aufbereiteten Daten enthält.
	 * 
	 * @param keyword Das Keyword, nach dem gesucht wurde.
	 * @param someEntries Die zum Keyword ermittelten Daten
	 * @param specification Die Spezifikation der Daten (Anzahl Kanji, Reading)
	 * @param workbook Das Excel-Workbook
	 */
	private void createDataSheet(String keyword, List<ExcelEntry> someEntries, DataSpecification specification,
			HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet("Suchbegriff " + keyword);

		ExcelSpecification excel = getExcelSpecification(specification);

		int rowNum = 0;
		addHeader(excel, workbook, sheet, rowNum++);
		addContent(excel, someEntries, sheet, rowNum);
	}

	/**
	 * Die 'Spezifikation' des zu erzeugenden Excel-Files. 
	 * - Startposition der Kanji-Rows 
	 * - Startposition der Reading-Rows ...
	 * 
	 * @param specification Die Spezifikation der Daten
	 * @return Die Spezifikation des zu erzeugenden Excel-Files
	 */
	private ExcelSpecification getExcelSpecification(DataSpecification specification) {
		int kanjiStart = 0;
		int readingStart = kanjiStart + specification.getMaxKanjiCount();
		int partsOfSpeechStart = readingStart + specification.getMaxReadingCount();
		int englishDefinitionStart = partsOfSpeechStart + 1;
		int multipleKanji = englishDefinitionStart + 3;
		int multipleReading = multipleKanji + 1;
		int place = multipleReading + 1;
		int summe = place + 1;

		ExcelSpecification excelSpecification = new ExcelSpecification(kanjiStart, readingStart, partsOfSpeechStart,
				englishDefinitionStart, multipleKanji, multipleReading, place, summe);
		return excelSpecification;
	}

	/**
	 * Erzeugt den Header im Daten-Excel.
	 * 
	 * @param excel Die Spezifikation des Excel-Files
	 * @param workbook Das Workbook
	 * @param sheet Das Sheet, das die Daten enthalten wird
	 * @param rowNum Position, an der der Header eingefügt werden soll
	 */
	private void addHeader(ExcelSpecification excel, HSSFWorkbook workbook, HSSFSheet sheet, int rowNum) {
		Row headerRow = sheet.createRow(rowNum);

		for (int i = excel.getKanjiStart(); i < excel.getReadingStart(); i++) {
			excelUtil.addHeaderCell(i, "Kanji " + i, workbook, headerRow);
		}

		for (int i = excel.getReadingStart(); i < excel.getEnglishDefinitionStart(); i++) {
			int readingIndex = i - excel.getReadingStart();
			excelUtil.addHeaderCell(i, "Reading " + readingIndex, workbook, headerRow);
		}

		excelUtil.addHeaderCell(excel.getPartsOfSpeech(), "Parts of speech", workbook, headerRow);

		for (int i = excel.getEnglishDefinitionStart(); i < excel.getMultipleKanji(); i++) {
			int englishDefinitionIndex = i - excel.getEnglishDefinitionStart();
			excelUtil.addHeaderCell(i, "English Definition " + englishDefinitionIndex, workbook, headerRow);
		}

		excelUtil.addHeaderCell(excel.getMultipleKanji(), "multiple_kanji", workbook, headerRow);
		excelUtil.addHeaderCell(excel.getMultipleReading(), "multiple_reading", workbook, headerRow);
		excelUtil.addHeaderCell(excel.getMultipleReading(), "isPlace", workbook, headerRow);

		Cell lastCell = excelUtil.addHeaderCell(excel.getMultipleReading(), "summe", workbook, headerRow);
		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:" + lastCell.getAddress()));
	}

	/**
	 * Fügt dem Sheet die Daten hinzu.
	 * 
	 * @param excel Die Spezifikation des Excel-Files
	 * @param workbook Das Workbook
	 * @param sheet Das Sheet, das die Daten enthalten wird
	 * @param rowNum Position, an der Die Daten eingefügt werden soll
	 */
	private void addContent(ExcelSpecification excel, List<ExcelEntry> someEntries, HSSFSheet sheet, int rowNum) {
		for (ExcelEntry entry : someEntries) {
			Row row = sheet.createRow(rowNum++);

			// Add Kanji's
			int kanjiIndex = excel.getKanjiStart();
			for (String kanji : entry.getKanjis()) {
				excelUtil.addContentCell(row, kanjiIndex, kanji);

				kanjiIndex++;
			}

			// Add Readings
			int readingIndex = excel.getReadingStart();
			for (String reading : entry.getReadings()) {
				excelUtil.addContentCell(row, readingIndex, reading);

				readingIndex++;
			}

			// Add Parts of Speech
			String partsOfSpeech = StringUtils.collectionToDelimitedString(entry.getPartsOfSpeech(), ", ");
			excelUtil.addContentCell(row, excel.getPartsOfSpeech(), partsOfSpeech);

			// Add Englisch Definitions
			if (entry.getEnglishDefinitions().size() > 0) {
				List<String> englishDefinitions = entry.getEnglishDefinitions().get(0);

				excelUtil.addContentCell(row, excel.getEnglishDefinitionStart() + 0, englishDefinitions.get(0));

				/*
				 * Falls das erste Sense-Objekt mehr als English-Definition
				 * enthält, werden die weiteren English-Definitions als
				 * Komma-separierte Liste in die zweite
				 * English-Definitions-Zelle geschrieben.
				 */
				if (englishDefinitions.size() > 1) {
					List<String> englishDefinitionsWithoutFirst = new ArrayList<>(englishDefinitions);
					englishDefinitionsWithoutFirst.remove(0);

					String englishWordList = StringUtils.collectionToDelimitedString(englishDefinitionsWithoutFirst,
							", ");

					excelUtil.addContentCell(row, excel.getEnglishDefinitionStart() + 1, englishWordList);
				}

			}
			/*
			 * Falls mehr als ein Sense-Objekt vorhanden ist, werden die
			 * English-Definitions der weiteren Senses Komma-separiert in die
			 * dritte English-Definitions-Zelle geschrieben. Die Senses werden
			 * dabei jeweils mit einem Semikolon getrennt.
			 */
			if (entry.getEnglishDefinitions().size() > 1) {
				List<String> allEnglishDefinitions = new ArrayList<>();
				for (int i = 1; i < entry.getEnglishDefinitions().size(); i++) {
					List<String> englishDefinitions = entry.getEnglishDefinitions().get(i);
					String englishDefinition = StringUtils.collectionToDelimitedString(englishDefinitions, ", ");
					allEnglishDefinitions.add(englishDefinition);
				}
				String englishWordList = StringUtils.collectionToDelimitedString(allEnglishDefinitions, "; ");
				excelUtil.addContentCell(row, excel.getEnglishDefinitionStart() + 2, englishWordList);
			}

			// Multiple Kanji
			int multipleKanjiValue = kanjiIndex - excel.getKanjiStart() > 1 ? 1 : 0;
			excelUtil.addContentCell(row, excel.getMultipleKanji(), multipleKanjiValue);

			// Multiple Reading
			int differentReadingsValue = entry.isDifferentReadings() ? 1 : 0;
			excelUtil.addContentCell(row, excel.getMultipleReading(), differentReadingsValue);

			// Place
			int placeValue = entry.isPlace() ? 1 : 0;
			excelUtil.addContentCell(row, excel.getPlace(), placeValue);

			// Summe
			int summe = multipleKanjiValue + differentReadingsValue + placeValue;
			excelUtil.addContentCell(row, excel.getSumme(), summe);
		}

		// Auto-Resize aller Spalten.
		for (int i = 0; i <= excel.getMultipleKanji(); i++) {
			sheet.autoSizeColumn(i);
		}
	}

	/**
	 * Erzeugt das Sheet mit den Logging-Informationen
	 * 
	 * @param logging Das Logging-Objekt
	 * @param workbook Das Workbook
	 */
	private void createLoggingSheet(Logging logging, HSSFWorkbook workbook) {
		HSSFSheet sheet = workbook.createSheet("Logging");

		int startedAtPosition = 0;
		int messagePosition = startedAtPosition + 1;
		int endedAtPosition = messagePosition + 1;
		int durationPosition = endedAtPosition + 1;
		int statusPosition = durationPosition + 1;
		int detailPosition = statusPosition + 1;

		Row headerRow = sheet.createRow(0);
		excelUtil.addHeaderCell(startedAtPosition, "StartedAt", workbook, headerRow);
		excelUtil.addHeaderCell(messagePosition, "Message", workbook, headerRow);
		excelUtil.addHeaderCell(endedAtPosition, "EndedAt", workbook, headerRow);
		excelUtil.addHeaderCell(durationPosition, "Duration", workbook, headerRow);
		excelUtil.addHeaderCell(statusPosition, "Status", workbook, headerRow);
		excelUtil.addHeaderCell(detailPosition, "Detail", workbook, headerRow);

		int rownum = 1;
		for (LogEntry entry : logging.getLogs()) {
			Row row = sheet.createRow(rownum++);

			excelUtil.addContentCell(row, startedAtPosition, entry.getStartdAt());
			excelUtil.addContentCell(row, messagePosition, entry.getMessage());
			excelUtil.addContentCell(row, endedAtPosition, entry.getEndedAt());
			excelUtil.addContentCell(row, durationPosition, entry.getDuration());
			excelUtil.addContentCell(row, statusPosition, entry.getStatus());
			excelUtil.addContentCell(row, detailPosition, entry.getStatusDetail());
		}

		for (int i = 0; i <= 5; i++) {
			sheet.autoSizeColumn(i);
		}
	}
}
