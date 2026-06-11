package net.dreamlu.mica.admin.project.system.service;

/**
 * 邮件服务
 */
public interface IMailService {

	/**
	 * 发送邮件信息
	 *
	 * @param email 邮箱
	 * @param code code 码
	 */
	void send(String email, String code);

}
