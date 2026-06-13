package net.dreamlu.mica.admin.framework.config;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 浏览器 ua 解析配置
 *
 * @author L.cm
 */
@Configuration
public class UserAgentAnalyzerConfig {

	@Bean
	public UserAgentAnalyzer userAgentAnalyzer() {
		return UserAgentAnalyzer.newBuilder()
			.withCache(4096)
			.build();
	}

}
