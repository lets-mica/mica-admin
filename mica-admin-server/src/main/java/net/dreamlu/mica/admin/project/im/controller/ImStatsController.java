package net.dreamlu.mica.admin.project.im.controller;

import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.im.session.ImSessionRegistry;
import org.dromara.mica.mqtt.spring.server.MqttServerTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * IM 模块运维统计接口。
 * <p>
 * 仅暴露给管理员，便于运维同学在管理后台查看 broker 状态、在线人数。
 *
 * @author L.cm
 */
@RestController
@RequestMapping("/admin/im/stats")
@RequiredArgsConstructor
public class ImStatsController extends BaseController {

	private final MqttServerTemplate mqttServerTemplate;
	private final ImSessionRegistry sessionRegistry;

	/**
	 * 查询当前在线端数（所有 user 累计）。
	 */
	@GetMapping("/online")
	@PreAuthorize("@sec.hasPermission('im:stats:query') or hasRole('ADMIN')")
	public Map<String, Object> online() {
		Map<String, Object> data = new HashMap<>(2);
		data.put("totalOnline", sessionRegistry.totalOnline());
		return data;
	}

	/**
	 * 查询 broker 内部统计（连接数、消息收发量等）。
	 */
	@GetMapping("/broker")
	@PreAuthorize("@sec.hasPermission('im:stats:query') or hasRole('ADMIN')")
	public Map<String, Object> broker() {
		Map<String, Object> data = new HashMap<>(4);
		data.put("stat", mqttServerTemplate.getStat());
		data.put("clients", mqttServerTemplate.getClients());
		return data;
	}
}