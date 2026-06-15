package net.dreamlu.mica.admin.project.system.service.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.dreamlu.mica.admin.project.system.service.IMailService;
import net.dreamlu.mica.core.io.FastStringWriter;
import net.dreamlu.mica.core.utils.Exceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件服务
 *
 * @author L.cm
 */
@Service
public class MailServiceImpl implements IMailService {
	@Autowired
	private Configuration configuration;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private MailProperties mailProperties;

	@Override
	public void send(String email, String code) {
		String subject = "Mica-Admin 后台管理系统";
		MimeMessage message = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, false);
			helper.setFrom(mailProperties.getUsername());
			helper.setTo(email);
			helper.setSubject(subject);
			Map<String, Object> data = new HashMap<>(1);
			data.put("code", code);
			helper.setText(getMailHtml(data), true);
			mailSender.send(message);
		} catch (MessagingException e) {
			throw Exceptions.unchecked(e);
		}
	}

	private String getMailHtml(Object dataModel) {
		try {
			Template template = configuration.getTemplate("email.ftl");
			FastStringWriter stringWriter = new FastStringWriter();
			template.process(dataModel, stringWriter);
			return stringWriter.toString();
		} catch (TemplateException | IOException e) {
			throw Exceptions.unchecked(e);
		}
	}

}
