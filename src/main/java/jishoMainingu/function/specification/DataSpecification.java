package jishoMainingu.function.specification;

import lombok.Value;

/**
 * Spezifiziert die abgefragten Daten.
 * 
 * @author ChrisCavegn
 */
@Value
public class DataSpecification {
	private int maxKanjiCount;
	private int maxReadingCount;
}
