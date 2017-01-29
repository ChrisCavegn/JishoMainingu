package jishoMainingu.function.modeladaption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Named;

import org.springframework.util.StringUtils;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.backend.jisho.model.JapaneseDto;
import jishoMainingu.backend.jisho.model.SenseDto;
import jishoMainingu.function.excel.model.ExcelEntry;
import jishoMainingu.function.logging.Logging;

/**
 * Konvertiert das Jisho-Modell in ein Modell, aus welchem einfacher das Excel
 * erzeugt werden kann.
 * 
 * @author ChrisCavegn
 */
@Named
public class ModelConverter {
	/**
	 * @param someData Die Daten in Jisho-Darstellung
	 * @param logging Das Logging-Objekt
	 * @return Die Daten so aufbereitet, dass sie einfacher in's Excel übernommen werden können
	 */
	public List<ExcelEntry> convert(List<DataDto> someData, Logging logging) {
		List<ExcelEntry> converted = convertData(someData);
		converted = copyFirstReadingToMissingKanji(converted, logging);
		return converted;
	}

	/**
	 * Pro Entry: Falls kein Kanji vorhanden ist aber mindestens ein Reading 
	 * -> Erstes Reading in die Liste der Kanji übernehmen
	 * 
	 * @param allEntries Die Entries
	 * @param logging Das Logging-Objekt
	 * @return Die Entries mit mindestens einem Kanji
	 */
	private List<ExcelEntry> copyFirstReadingToMissingKanji(List<ExcelEntry> allEntries, Logging logging) {
		for (ExcelEntry entry : allEntries) {
			if (entry.getKanjis().isEmpty() && !entry.getReadings().isEmpty()) {
				String firstReading = entry.getReadings().get(0);
				entry.getKanjis().add(firstReading);

				logging.createEntry(
						String.format("Übernehme Reading %s als Kanji (da kein Kanji vorhanden)", firstReading));
			}
		}
		return allEntries;
	}

	/**
	 * @param someData Die Daten in Jisho-Darstellung
	 * @return Die Daten so aufbereitet, dass sie einfacher in's Excel übernommen werden können
	 */
	private List<ExcelEntry> convertData(List<DataDto> someData) {
		List<ExcelEntry> entries = new ArrayList<>();

		for (DataDto data : someData) {
			ExcelEntry entry = new ExcelEntry();

			// Kanji's übernehmen
			for (JapaneseDto japanese : data.getJapanese()) {
				if (!StringUtils.isEmpty(japanese.getWord())) {
					entry.getKanjis().add(japanese.getWord());
				}
			}

			// Readings übernehmen
			for (JapaneseDto japanese : data.getJapanese()) {
				if (!StringUtils.isEmpty(japanese.getReading())) {
					entry.getReadings().add(japanese.getReading());
				}
			}

			// Parts of Speach übernehmen
			for (SenseDto sense : data.getSenses()) {
				entry.getPartsOfSpeechOriginal().addAll(sense.getParts_of_speech());
			}

			// English Definitions übernehmen
			for (SenseDto sense : data.getSenses()) {
				entry.getEnglishDefinitions().add(new ArrayList<>(sense.getEnglish_definitions()));
			}

			// Ermittle, ob unterschiedliche Readings existieren
			entry.setDifferentReadings(new HashSet<>(entry.getReadings()).size() > 1);

			// Ermittle, ob 'Place' in PartsOfSpeach enthalten ist 
			entry.setPlace(entry.getPartsOfSpeechOriginal().contains("Place"));
			
			entries.add(entry);
		}

		return entries;
	}
}
