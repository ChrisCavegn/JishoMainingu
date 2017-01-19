package jishoMainingu.backend.jisho.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataDto {
	private List<JapaneseDto> japanese;
	private List<SenseDto> senses;

}
