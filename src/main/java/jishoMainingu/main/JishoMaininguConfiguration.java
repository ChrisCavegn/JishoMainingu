package jishoMainingu.main;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import jishoMainingu.rest.JishoMaininguRs;

@Configuration
public class JishoMaininguConfiguration extends ResourceConfig {
	public JishoMaininguConfiguration() {
		register(JishoMaininguRs.class);
	}
}
