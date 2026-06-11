package net.dreamlu.mica.admin.generator;

import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.base.BaseModel;

public class MysqlAutoGenerator {

	public static void main(String[] args) {
		MicaAutoGenerator.form("application-dev.yml")
			.globalConfig(builder -> {
				builder.author("L.cm")
					.dateType(DateType.TIME_PACK)
					.enableSpringdoc()
					.outputDir(MicaAutoGenerator.getOutputDir());
			})
			.packageConfig(builder -> {
				builder.parent("net.dreamlu.mica.admin.project")
					.moduleName("system");
			})
			.strategyConfig(builder -> {
				builder.addInclude("sys_file_storage")
					.entityBuilder()
					.enableFileOverride()
					.enableLombok()
					.superClass(BaseModel.class)
					.addSuperEntityColumns("id", "created_by", "created_at", "updated_by", "updated_at")
					.naming(NamingStrategy.underline_to_camel)
					.controllerBuilder()
					.enableFileOverride()
					.superClass(BaseController.class)
					.enableRestStyle()
					.enableHyphenStyle()
					.mapperBuilder()
					.enableFileOverride()
					.enableBaseResultMap()
					.enableBaseColumnList()
				;
			})
			.templateEngine(new FreemarkerTemplateEngine())
			.execute();
	}
}
