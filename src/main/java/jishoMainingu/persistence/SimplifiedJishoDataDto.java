package jishoMainingu.persistence;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Vereinfachter Jisho-Datensatz, welcher dazu verwendet wird, eine Übersicht über die Daten im Cache zu vermitteln.
 *
 * @author ChrisCavegn
 */
@Data
public class SimplifiedJishoDataDto {
	private Long id;
	private String keyword;
	private LocalDateTime queryDate;
	private Integer maxPage;
	private int entryCount;
}
