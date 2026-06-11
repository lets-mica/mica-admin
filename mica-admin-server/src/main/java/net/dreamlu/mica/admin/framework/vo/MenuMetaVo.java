package net.dreamlu.mica.admin.framework.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 前端菜单路由元数据
 *
 * @author L.cm
 */
@Getter
@RequiredArgsConstructor
public class MenuMetaVo implements Serializable {

	private final String title;
	private final String icon;
	private final Boolean noCache;

}
