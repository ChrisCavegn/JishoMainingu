package jishoMainingu.function.excel;

import lombok.Value;

@Value
public class ExcelSpecification {
	private int kanjiStart;
	private int readingStart;
	private int partsOfSpeechModified;
	private int partsOfSpeechOriginal;
	private int englishDefinitionStart;
	private int multipleKanji;
	private int multipleReading;
	private int place;
	private int summe;
}
