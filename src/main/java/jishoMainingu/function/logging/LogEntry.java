package jishoMainingu.function.logging;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@ToString
public class LogEntry {

	@Getter
	@NonNull
	private LocalDateTime startdAt;
	@Getter
	@NonNull
	private String message;
	@Getter
	private LocalDateTime endedAt;
	@Getter
	private String status;
	@Getter
	private String statusDetail;

	public void success() {
		this.endedAt = LocalDateTime.now();
		this.status = "Ok";
	}

	public void exception(String statusDetail) {
		this.endedAt = LocalDateTime.now();
		this.status = "Exception";
		this.statusDetail = statusDetail;

		log.error(this.toString());
	}

	public Duration getDuration() {
		if (startdAt != null && endedAt != null) {
			return Duration.between(startdAt, endedAt);
		}
		return null;
	}
}
