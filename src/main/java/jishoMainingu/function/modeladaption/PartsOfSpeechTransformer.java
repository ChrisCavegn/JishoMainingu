package jishoMainingu.function.modeladaption;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import jishoMainingu.function.excel.model.ExcelEntry;
import jishoMainingu.function.logging.LogEntry;
import jishoMainingu.function.logging.Logging;
import lombok.Data;

@Named
public class PartsOfSpeechTransformer {

	public Map<String, String> posMapping = new LinkedHashMap<>();

	@Data
	class PosEntry {
		String pos;
		String mapping;
	}

	@Value(value = "classpath:dictionary_jisho_POS.csv")
	private Resource resource;

	public void initialize(Logging logging) {
		LogEntry log = logging
				.createEntry(String.format("Öffne File mit dem POS Mapping (%s)", resource.getFilename()));

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), Charset.forName("UTF-8")))) {
			while (reader.ready()) {
				String line = reader.readLine();
				String[] tokens = line.split(";");
				if (tokens.length > 1) {
					posMapping.put(tokens[0], tokens[1]);
					logging.createEntry(String.format("Lese POS-Mapping '%s'->'%s'", tokens[0], tokens[1]));
				}
				else {
					posMapping.put(tokens[0], null);
					logging.createEntry(String.format("Lese POS-Mapping '%s'->'%s'", tokens[0], "<null>"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log.exception(e.getMessage());
		}
	}

	public Map<String, String> getMapping() {
		return Collections.unmodifiableMap(posMapping);
	}

	public void createModifiedPOS(List<ExcelEntry> someEntries) {
		for (ExcelEntry entry : someEntries) {
			for (String posOriginal : entry.getPartsOfSpeechOriginal()) {
				String mapped = posMapping.get(posOriginal);
				if (mapped != null && !entry.getPartsOfSpeechMapped().contains(mapped)) {
					entry.getPartsOfSpeechMapped().add(mapped);
				}
			}
		}
	}
}
