package jishoMainingu.function.specification;

import java.util.List;

import javax.inject.Named;

import org.springframework.util.StringUtils;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.backend.jisho.model.JapaneseDto;
import jishoMainingu.function.logging.Logging;

/**
 * Ermittelt die Spezifikation der übergebenen Daten.
 * 
 * - Maximale Anzahl JapaneseDto Objekte in den DataDto's - Maximale Anzahl
 * SenseDto Objekte in den DataDto's
 * 
 * @author ChrisCavegn
 *
 */
@Named
public class SpecificationCalculator {

	/**
	 * @param allData
	 *            Die Abgefragten Daten
	 * @param logging 
	 * @return Die Spezifikation der Daten
	 */
	public DataSpecification calculate(List<DataDto> allData, Logging logging) {
		int maxKanji = 0;
		int maxReading = 0;

		for (DataDto data : allData) {
			int kanji = 0;
			int reading = 0;

			for (JapaneseDto japanese : data.getJapanese()) {
				if (!StringUtils.isEmpty(japanese.getWord())) {
					kanji++;
				}
				if (!StringUtils.isEmpty(japanese.getReading())) {
					reading++;
				}
			}

			maxKanji = Math.max(maxKanji, kanji);
			maxReading = Math.max(maxReading, reading);
		}

		DataSpecification specification = new DataSpecification(maxKanji, maxReading);

		logging.createEntry(String.format("Ermittelte Spezifikation: %s", specification));

		return specification;
	}

}
