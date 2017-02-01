package jishoMainingu.persistence;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objekt, welches einen Jisho-Datensatz mit den Parametern, mit welchen er abgefragt wurde, verbindet.
 *
 * @author ChrisCavegn
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class JishoDataDto {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "keyword")
	private String keyword;

	@Column(name = "queryDate")
	private LocalDateTime queryDate;

	@Column(name = "maxPage")
	private Integer maxPage;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "data", columnDefinition = "CLOB NOT NULL")
	private String data;

	@Column(name = "entryCount")
	private int entryCount;
}
