package jishoMainingu.backend.jisho;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.springframework.web.client.RestTemplate;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.backend.jisho.model.ResultDto;
import jishoMainingu.function.logging.LogEntry;
import jishoMainingu.function.logging.Logging;

@Named
public class JishoAccess {
	/**
	 * Sucht nach dem übergebenen Keyword und liefert die abgefragten Daten zurück. 
	 * Die Methode geht solange die pages durch, bis keine Daten mehr zurückgeliefert werden.
	 * 
	 * @param keyword Nach diesem Begriff wird gesucht
	 * @param logging Technisches Logging-Objekt
	 * @return Die abgefragten Daten
	 */
	public List<DataDto> read(String keyword, Logging logging) {
		RestTemplate restTemplate = new RestTemplate();

		List<DataDto> data = new ArrayList<>();

		int page = 1;

		boolean finished = false;
		while (!finished) {
			String uri = String.format("http://jisho.org/api/v1/search/words?keyword=%s&page=%d", keyword, page++);

			LogEntry logEntry = logging.createEntry(String.format("Abfrage von %s", uri));

			try {
				ResultDto result = restTemplate.getForObject(uri, ResultDto.class);

				data.addAll(result.getData());
				finished = result.getData().isEmpty();

				logEntry.success();
			}
			catch (Exception e) {
				e.printStackTrace();
				logEntry.exception(e.getMessage());
			}
		}

		return data;
	}
}
