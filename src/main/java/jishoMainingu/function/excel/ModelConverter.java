package jishoMainingu.function.excel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Named;

import org.springframework.util.StringUtils;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.backend.jisho.model.JapaneseDto;
import jishoMainingu.backend.jisho.model.SenseDto;
import jishoMainingu.function.excel.model.ExcelEntry;

@Named
public class ModelConverter {
	public List<ExcelEntry> convert(List<DataDto> someData) {
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
			for(SenseDto sense: data.getSenses()){
				entry.getPartsOfSpeech().addAll(sense.getParts_of_speech());
			}
			
			// English Definitions übernehmen
			for (SenseDto sense : data.getSenses()) {
				entry.getEnglishDefinitions().add(new ArrayList<>(sense.getEnglish_definitions()));
			}

			// Ermittle, ob unterschiedliche Readings existieren
			entry.setDifferentReadings(new HashSet<>(entry.getReadings()).size() > 1);

			entries.add(entry);
		}

		return entries;
	}
}
