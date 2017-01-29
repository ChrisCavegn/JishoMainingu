package jishoMainingu.function.excel.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ExcelEntry {
	private boolean differentReadings;
	private boolean place;

	private List<String> kanjis = new ArrayList<>();
	private List<String> readings = new ArrayList<>();
	private List<String> partsOfSpeechOriginal = new ArrayList<>();
	private List<String> partsOfSpeechMapped = new ArrayList<>();
	private List<List<String>> englishDefinitions = new ArrayList<>();
}
