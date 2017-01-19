package jishoMainingu.backend.jisho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JapaneseDto {
	private String word;
	private String reading;
}
