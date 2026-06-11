package net.dreamlu.mica.admin.project.system.controller;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.project.system.service.IMonitorService;
import net.dreamlu.mica.redis.cache.MicaRedisCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 监控管理
 *
 * @author L.cm
 */
@RestController
@RequestMapping("/api/system/monitor")
@Tag(name = "系统-服务监控管理")
@RequiredArgsConstructor
public class SysMonitorController {
	private final IMonitorService serverService;
	private final MicaRedisCache redisCache;

	@GetMapping("server")
	@Operation(summary = "服务器监控")
	@PreAuthorize("@sec.hasPermission('system:monitor:servers')")
	public Map<String, Object> serverStat() {
		return serverService.getServers();
	}

	@GetMapping("sql")
	@Operation(summary = "sql监控")
	@PreAuthorize("@sec.hasPermission('system:monitor:sql')")
	public List<Map<String, Object>> sqlStat() {
		DruidStatManagerFacade statManagerFacade = DruidStatManagerFacade.getInstance();
		return statManagerFacade.getSqlStatDataList(null);
	}

	@GetMapping("redis")
	@Operation(summary = "redis监控")
	@PreAuthorize("@sec.hasPermission('system:monitor:redis')")
	public Map<String, Object> redisStat() {
		RedisTemplate<String, Object> redisTemplate = redisCache.getRedisTemplate();
		Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) RedisServerCommands::info);
		Properties commandStats = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("commandstats"));
		Object dbSize = redisTemplate.execute((RedisCallback<Object>) RedisServerCommands::dbSize);

		Map<String, Object> result = new HashMap<>();
		result.put("info", info);
		result.put("dbSize", dbSize);
		List<Map<String, String>> pieList = new ArrayList<>();
		commandStats.stringPropertyNames().forEach(key -> {
			Map<String, String> data = new HashMap<>();
			String property = commandStats.getProperty(key);
			data.put("name", StringUtils.removeStart(key, "cmdstat_"));
			data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
			pieList.add(data);
		});
		result.put("commandStats", pieList);
		return result;
	}

}
