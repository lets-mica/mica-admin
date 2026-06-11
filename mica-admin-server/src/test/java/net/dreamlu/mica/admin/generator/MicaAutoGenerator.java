package net.dreamlu.mica.admin.generator;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.engine.AbstractTemplateEngine;
import net.dreamlu.mica.core.utils.Charsets;
import net.dreamlu.mica.core.utils.StringPool;
import net.dreamlu.mica.core.utils.UrlUtil;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * mica mybatis plus AutoGenerator
 *
 * @author L.cm
 */
public final class MicaAutoGenerator {
	private final DataSourceConfig.Builder dataSourceConfigBuilder;
	private final GlobalConfig.Builder globalConfigBuilder;
	private final TemplateConfig.Builder templateConfigBuilder;
	private final PackageConfig.Builder packageConfigBuilder;
	private final StrategyConfig.Builder strategyConfigBuilder;
	private final InjectionConfig.Builder injectionConfigBuilder;
	private AbstractTemplateEngine templateEngine;

	private MicaAutoGenerator(DataSourceConfig.Builder dataSourceConfigBuilder) {
		this.dataSourceConfigBuilder = dataSourceConfigBuilder;
		this.globalConfigBuilder = new GlobalConfig.Builder();
		this.packageConfigBuilder = new PackageConfig.Builder();
		this.strategyConfigBuilder = new StrategyConfig.Builder();
		this.injectionConfigBuilder = new InjectionConfig.Builder();
		this.templateConfigBuilder = new TemplateConfig.Builder();
	}

	public static MicaAutoGenerator form(@NonNull String name) {
		PropertySource<?> propertySource = loadPropertySource(name);
		String url = String.valueOf(propertySource.getProperty("spring.datasource.url"));
		Objects.requireNonNull(url, "Property spring.datasource.url is null.");
		String username = String.valueOf(propertySource.getProperty("spring.datasource.username"));
		String password = String.valueOf(propertySource.getProperty("spring.datasource.password"));
		return create(url, username, password);
	}

	public static MicaAutoGenerator create(@NonNull String url, String username, String password) {
		return new MicaAutoGenerator(new DataSourceConfig.Builder(url, username, password));
	}

	public static MicaAutoGenerator create(@NonNull DataSource dataSource) {
		return new MicaAutoGenerator(new DataSourceConfig.Builder(dataSource));
	}

	public MicaAutoGenerator dataSourceConfig(Consumer<DataSourceConfig.Builder> consumer) {
		consumer.accept(this.dataSourceConfigBuilder);
		return this;
	}

	public MicaAutoGenerator globalConfig(Consumer<GlobalConfig.Builder> consumer) {
		consumer.accept(this.globalConfigBuilder);
		return this;
	}

	public MicaAutoGenerator packageConfig(Consumer<PackageConfig.Builder> consumer) {
		consumer.accept(this.packageConfigBuilder);
		return this;
	}

	public MicaAutoGenerator strategyConfig(Consumer<StrategyConfig.Builder> consumer) {
		consumer.accept(this.strategyConfigBuilder);
		return this;
	}

	public MicaAutoGenerator injectionConfig(Consumer<InjectionConfig.Builder> consumer) {
		consumer.accept(this.injectionConfigBuilder);
		return this;
	}

	public MicaAutoGenerator templateConfig(Consumer<TemplateConfig.Builder> consumer) {
		consumer.accept(this.templateConfigBuilder);
		return this;
	}

	public MicaAutoGenerator templateEngine(AbstractTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
		return this;
	}

	public void execute() {
		new AutoGenerator(this.dataSourceConfigBuilder.build())
			// 全局配置
			.global(this.globalConfigBuilder.build())
			// 模板配置
			.template(this.templateConfigBuilder.build())
			// 包配置
			.packageInfo(this.packageConfigBuilder.build())
			// 策略配置
			.strategy(this.strategyConfigBuilder.build())
			// 注入配置
			.injection(this.injectionConfigBuilder.build())
			// 执行
			.execute(this.templateEngine);
	}

	/**
	 * 获取配置文件
	 *
	 * @return 配置Props
	 */
	private static PropertySource<?> loadPropertySource(String name) {
		Resource resource = new ClassPathResource(name);
		PropertySourceLoader loader;
		CompositePropertySource propertySource = new CompositePropertySource(name);
		if (name.endsWith(".properties")) {
			loader = new PropertiesPropertySourceLoader();
		} else {
			loader = new YamlPropertySourceLoader();
		}
		try {
			List<PropertySource<?>> propertySources = loader.load(name, resource);
			for (PropertySource<?> source : propertySources) {
				propertySource.addPropertySource(source);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return propertySource;
	}

	/**
	 * 生成到项目中
	 *
	 * @return outputDir
	 */
	public static String getOutputDir() {
		return getClassPath() + File.separator + "gen_code";
	}

	@Nullable
	private static String getClassPath() {
		try {
			URL url = MicaAutoGenerator.class.getResource(StringPool.SLASH).toURI().toURL();
			return toFilePath(url);
		} catch (Exception var2) {
			String path = MicaAutoGenerator.class.getResource(StringPool.EMPTY).getPath();
			return (new File(path)).getParentFile().getParentFile().getAbsolutePath();
		}
	}

	@Nullable
	private static String toFilePath(@Nullable URL url) {
		if (url == null) {
			return null;
		} else {
			String protocol = url.getProtocol();
			String file = UrlUtil.decode(url.getPath(), Charsets.UTF_8);
			if ("file".equals(protocol)) {
				return (new File(file)).getParentFile().getAbsolutePath();
			} else if (!"jar".equals(protocol) && !"zip".equals(protocol)) {
				return file;
			} else {
				int idx = file.indexOf("!/");
				if (idx > 0) {
					file = file.substring(0, idx);
				}
				if (file.startsWith("file:")) {
					file = file.substring("file:".length());
				}
				return (new File(file)).getAbsolutePath();
			}
		}
	}

}
