package jishoMainingu.function.excel.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ExcelEntry {
	private boolean differentReadings;

	private List<String> kanjis = new ArrayList<>();
	private List<String> readings = new ArrayList<>();
	private List<String> partsOfSpeech = new ArrayList<>();
	private List<List<String>> englishDefinitions = new ArrayList<>();
}
