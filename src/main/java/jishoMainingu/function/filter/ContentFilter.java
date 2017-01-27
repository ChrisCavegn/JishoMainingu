package jishoMainingu.function.filter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.backend.jisho.model.SenseDto;
import jishoMainingu.function.logging.Logging;

/**
 * Filtert unerwünschte Einträge aus den Daten.
 * 
 * @author ChrisCavegn
 */
@Named
public class ContentFilter {

	private static final String EVIL_SOURCE = "Wikipedia definition";

	/**
	 * Falls filter=true werden die übergebenen Daten gefiltert. Andernfalls
	 * werden die Daten nicht verändert.
	 * 
	 * @param allData Die Daten
	 * @param filter true falls der Filter angewendet werden soll
	 * @param logging Das Logging-Objekt
	 */
	public void filter(List<DataDto> allData, boolean filter, Logging logging) {
		if (!filter) {
			return;
		}

		for (DataDto data : allData) {
			List<SenseDto> relevantSenses = new ArrayList<>();
			for (SenseDto sense : data.getSenses()) {
				if (!sense.getParts_of_speech().contains(EVIL_SOURCE)) {
					relevantSenses.add(sense);
				} 
				else {
					String message = String.format("Filter Sense %s wegen Evil-Source %s",
							sense.getEnglish_definitions(), EVIL_SOURCE);
					logging.createEntry(message);
				}
			}
			data.setSenses(relevantSenses);
		}
	}
}
