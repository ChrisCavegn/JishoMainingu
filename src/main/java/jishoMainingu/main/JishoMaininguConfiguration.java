package jishoMainingu.main;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jishoMainingu.rest.JishoMaininguRs;

@EnableJpaRepositories(basePackages = "jishoMainingu.persistence")
@EntityScan(basePackages = "jishoMainingu.persistence")
@Configuration
public class JishoMaininguConfiguration extends ResourceConfig {
	public JishoMaininguConfiguration() {
		register(JishoMaininguRs.class);
	}
}
