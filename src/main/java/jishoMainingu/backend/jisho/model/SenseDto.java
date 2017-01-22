package jishoMainingu.backend.jisho.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SenseDto {
	private List<String> english_definitions;
	private List<String> parts_of_speech;
}
