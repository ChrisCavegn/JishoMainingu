package jishoMainingu.function.excel;

import lombok.Value;

@Value
public class ExcelSpecification {
	private int kanjiStart;
	private int readingStart;
	private int partsOfSpeech;
	private int englishDefinitionStart;
	private int multipleKanji;
	private int multipleReading;
	private int place;
	private int summe;
}
