package net.dreamlu.mica.admin;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * mica-admin 启动程序
 *
 * @author L.cm
 */
@SpringBootApplication
@EnableFileStorage
public class MicaAdminApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MicaAdminApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MicaAdminApplication.class);
	}
}
