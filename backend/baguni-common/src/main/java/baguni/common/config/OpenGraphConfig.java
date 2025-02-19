package baguni.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import baguni.common.lib.opengraph.OpenGraphOption;

@Configuration
public class OpenGraphConfig {

	@Bean
	public OpenGraphOption openGraphOption() {
		return new OpenGraphOption(5 * 60);
	}
}
