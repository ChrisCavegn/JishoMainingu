package jishoMainingu.backend.jisho;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.backend.jisho.model.ResultDto;
import jishoMainingu.function.logging.LogEntry;
import jishoMainingu.function.logging.Logging;

/**
 * Klasse, welche den Zugriff auf den Jisho-REST-Service bereitstellt.
 * 
 * @author ChrisCavegn
 */
@Named
public class JishoAccess {
	
	@Value("${jishoUrl}")
	private String jishoUrl;
	
	/**
	 * Sucht nach dem übergebenen Keyword und liefert die abgefragten Daten zurück. 
	 * Die Methode geht solange die pages durch, bis keine Daten mehr zurückgeliefert werden.
	 * 
	 * @param keyword Nach diesem Begriff wird gesucht
	 * @param maxPage Optionaler Parameter, gibt an, bis zu welcher Page maximal Daten geladen werden.
	 * @param logging Technisches Logging-Objekt
	 * @return Die abgefragten Daten
	 */
	public List<DataDto> read(String keyword, Integer maxPage, Logging logging) {
		RestTemplate restTemplate = new RestTemplate();

		List<DataDto> data = new ArrayList<>();

		int page = 1;

		boolean finished = false;
		while (!finished) {
			String uri = String.format(jishoUrl, keyword, page++);

			LogEntry logEntry = logging.createEntry(String.format("Abfrage von %s", uri));

			try {
				ResultDto result = restTemplate.getForObject(uri, ResultDto.class);

				data.addAll(result.getData());
				
				finished |= result.getData().isEmpty();
				finished |= maxPage != null && maxPage.intValue() < page;

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
