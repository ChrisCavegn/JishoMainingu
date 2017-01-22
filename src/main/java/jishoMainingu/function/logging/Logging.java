package jishoMainingu.function.logging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Logging {
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();

	public LogEntry createEntry(String message) {
		LogEntry entry = new LogEntry(LocalDateTime.now(), message);
		
		log.info(entry.toString());
		
		logEntries.add(entry);
		return entry;
	}

	public List<LogEntry> getLogs() {
		return Collections.unmodifiableList(logEntries);
	}

}
