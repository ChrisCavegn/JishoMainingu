package jishoMainingu.backend.jisho;

import javax.inject.Named;

import org.springframework.web.client.RestTemplate;

import jishoMainingu.backend.jisho.model.ResultDto;

@Named
public class JishoAccess {
	public ResultDto read(String keyword) {
		RestTemplate restTemplate = new RestTemplate();
		ResultDto result = restTemplate.getForObject("http://jisho.org/api/v1/search/words?keyword=" + keyword, ResultDto.class);
		return result;
	}
}
