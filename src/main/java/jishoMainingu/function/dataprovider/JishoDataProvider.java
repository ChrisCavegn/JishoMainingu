package jishoMainingu.function.dataprovider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import jishoMainingu.backend.jisho.JishoAccess;
import jishoMainingu.backend.jisho.model.DataDto;
import jishoMainingu.function.logging.LogEntry;
import jishoMainingu.function.logging.Logging;
import jishoMainingu.persistence.DataRepository;
import jishoMainingu.persistence.JishoDataDto;
import jishoMainingu.persistence.SimplifiedJishoDataDto;

/**
 * Provider für die Jisho-Daten. Falls möglich, werden die im Cache zwischengespeicherten Daten zurückgegeben.
 * Sind im Cache keine Daten verfügbar, werden sie direkt über die Schnittstelle von Jisho bezogen.
 *
 * @author ChrisCavegn
 */
@Named
public class JishoDataProvider {
	@Inject
	private JishoAccess jishoAccess;

	@Inject
	private DataRepository repository;

	@Transactional(value = TxType.REQUIRES_NEW)
	public List<DataDto> read(String keyword, Integer maxPage, Logging logging) {

		List<DataDto> cachedData = getCachedData(keyword, maxPage, logging);
		if (CollectionUtils.isNotEmpty(cachedData)) {
			return cachedData;
		}

		// Keine Daten im Cache vorhanden, von Schnittstelle lesen
		List<DataDto> data = jishoAccess.read(keyword, maxPage, logging);

		addDataToCache(keyword, maxPage, data, logging);

		return data;
	}

	/**
	 * Fügt die übergebenen Daten dem Cache hinzu.
	 * 
	 * @param keyword Das Keyword
	 * @param maxPage MaxPage
	 * @param data Die Daten
	 * @param logging Das Logging-Objekt
	 */
	private void addDataToCache(String keyword, Integer maxPage, List<DataDto> data, Logging logging) {
		LogEntry log = logging.createEntry("Füge Daten dem Cache hinzu");
		try {
			ObjectMapper mapper = new Jackson2ObjectMapperBuilder().build();
			String dataString = mapper.writeValueAsString(data);

			JishoDataDto entity = new JishoDataDto(null, keyword, LocalDateTime.now(), maxPage, dataString, data.size());
			repository.save(entity);
			log.success();
		}
		catch (Exception e) {
			e.printStackTrace();
			log.exception(e.getMessage());
		}
	}

	/**
	 * Durchsuche den Cache nach Einträgen mit denselben Werten für keyword und maxPage.
	 * Falls der Cache mehr als einen möglichen Datensatz enthält, wird derjenige mit dem letzten Abfragedatum verwendet.
	 * 
	 * @param keyword Das Keyword
	 * @param maxPage MaxPage
	 * @param logging Das Logging-Objekt
	 * @return Die Daten aus dem Cache oder eine leere Liste.
	 */
	private List<DataDto> getCachedData(String keyword, Integer maxPage, Logging logging) {
		List<DataDto> result = new ArrayList<>();

		LogEntry log = logging.createEntry(String.format("Suche in Cache nach keyword='%s', maxPage='%d'", keyword, maxPage));
		try {
			List<JishoDataDto> allData = repository.findByKeywordAndMaxPageOrderByQueryDateDesc(keyword, maxPage);
			if (CollectionUtils.isNotEmpty(allData)) {
				JishoDataDto data = allData.get(0);

				ObjectMapper mapper = new Jackson2ObjectMapperBuilder().build();
				JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, DataDto.class);
				result.addAll(mapper.readValue(data.getData(), type));

				logging.createEntry(String.format("Daten im Cache gefunden (Abfragedatum: %s)", data.getQueryDate()));
			}
			else {
				logging.createEntry("Keine Daten im Cache gefunden");
			}

			log.success();
		}
		catch (Exception e) {
			e.printStackTrace();
			log.exception(e.getMessage());
		}

		return result;
	}

	/**
	 * @return Sämtliche im Cache enthaltenen Daten.
	 */
	@Transactional(value = TxType.NOT_SUPPORTED)
	public List<SimplifiedJishoDataDto> getCachedData() {
		Iterable<JishoDataDto> allData = repository.findAll();

		List<SimplifiedJishoDataDto> result = new ArrayList<>();
		for (JishoDataDto data : allData) {
			SimplifiedJishoDataDto entry = new SimplifiedJishoDataDto();
			entry.setId(data.getId());
			entry.setKeyword(data.getKeyword());
			entry.setMaxPage(data.getMaxPage());
			entry.setQueryDate(data.getQueryDate());
			entry.setEntryCount(data.getEntryCount());
			result.add(entry);
		}

		return result;
	}

	/**
	 * Setzt sämtliche zwischengespeicherten Daten zurück.
	 */
	@Transactional(value = TxType.REQUIRES_NEW)
	public void resetCache() {
		repository.deleteAll();
	}
}
