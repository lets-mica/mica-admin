package net.dreamlu.mica.admin.project.im.config;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.project.im.auth.MqttAuthInterceptor;
import net.dreamlu.mica.admin.project.im.topic.MqttTopicFilter;
import org.dromara.mica.mqtt.spring.server.MqttServerTemplate;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Configuration;

/**
 * IM 模块 MQTT Broker 配置。
 * <p>
 * 启动 mica-mqtt 内嵌 broker，端口配置见 {@code application.yml} 的 {@code mqtt.server} 节点。
 * TCP 1883 用于原生客户端 (App / mqtt.fx)，WebSocket 8083 用于浏览器 (mqtt.js)。
 * <p>
 * 实际的服务端行为（鉴权、订阅校验、消息路由）由
 * {@link MqttAuthInterceptor}、
 * {@link MqttTopicFilter}、
 * {@link net.dreamlu.mica.admin.project.im.handler.ImP2pMessageHandler}
 * 实现，并由 mica-mqtt-spring-boot-starter 自动扫描注入。
 *
 * @author L.cm
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class ImMqttConfig implements SmartInitializingSingleton {

	private final MqttServerTemplate mqttServerTemplate;

	public ImMqttConfig(MqttServerTemplate mqttServerTemplate) {
		this.mqttServerTemplate = mqttServerTemplate;
	}

	@Override
	public void afterSingletonsInstantiated() {
		// mica-mqtt-spring-boot-starter 启动时会自动 start broker，
		// 此处仅做日志提示，便于运维确认端口监听情况。
		log.info("IM: mica-mqtt broker 已就绪，可通过 mqttServerTemplate.publishAll(...) 发布消息");
	}
}