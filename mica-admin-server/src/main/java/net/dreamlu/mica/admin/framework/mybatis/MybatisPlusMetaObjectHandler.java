package net.dreamlu.mica.admin.framework.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.common.enums.StatusEnum;
import net.dreamlu.mica.admin.framework.security.utils.SecurityUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.util.ClassUtils;

import java.time.LocalDateTime;

/**
 * MybatisPlus配置
 *
 * @author L.cm
 */
@Slf4j
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

	@Override
	public void insertFill(MetaObject metaObject) {
		log.debug("mybatis plus start insert fill ....");
		LocalDateTime now = LocalDateTime.now();
		String userName = SecurityUtil.getUserName();
		fillValIfNullByName("createdAt", now, metaObject);
		fillValIfNullByName("updatedAt", now, metaObject);
		fillValIfNullByName("createdBy", userName, metaObject);
		fillValIfNullByName("updatedBy", userName, metaObject);
		// 逻辑删除的填充，避免数据库没有设置默认值，存储为 null
		fillValIfNullByName("status", StatusEnum.NORMAL.getValue(), metaObject);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		log.debug("mybatis plus start update fill ....");
		fillValIfNullByName("updatedAt", LocalDateTime.now(), metaObject);
		fillValIfNullByName("updatedBy", SecurityUtil.getUserName(), metaObject);
	}

	/**
	 * 填充值，先判断是否有手动设置，优先手动设置的值，例如：job必须手动设置
	 *
	 * @param fieldName  属性名
	 * @param fieldVal   属性值
	 * @param metaObject MetaObject
	 */
	private static void fillValIfNullByName(String fieldName, Object fieldVal, MetaObject metaObject) {
		// 1. 没有 get 方法
		if (!metaObject.hasSetter(fieldName)) {
			return;
		}
		// 2. 如果用户有手动设置的值
		Object userSetValue = metaObject.getValue(fieldName);
		if (userSetValue != null) {
			return;
		}
		// 3. field 类型相同时设置
		Class<?> getterType = metaObject.getGetterType(fieldName);
		if (ClassUtils.isAssignableValue(getterType, fieldVal)) {
			metaObject.setValue(fieldName, fieldVal);
		}
	}
}
