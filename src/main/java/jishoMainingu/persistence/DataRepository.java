package jishoMainingu.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

/**
 * Zugriff auf die Embedded-Datenbank, welche Daten von Jisho zwischenspeichert.
 *
 * @author ChrisCavegn
 */
public interface DataRepository extends CrudRepository<JishoDataDto, Long> {

	/**
	 * Sucht alle Datensätze, welche mit keyword und maxPage gesucht wurden.
	 * 
	 * @param keyword Keyword
	 * @param maxPage MaxPage
	 * @return Die passenden Datensätze, neuster Datensatz entspricht dem ersten Eintrag.
	 */
	List<JishoDataDto> findByKeywordAndMaxPageOrderByQueryDateDesc(String keyword, Integer maxPage);

}
