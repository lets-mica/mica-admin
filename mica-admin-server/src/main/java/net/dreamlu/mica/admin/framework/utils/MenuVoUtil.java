package net.dreamlu.mica.admin.framework.utils;

import net.dreamlu.mica.core.utils.StringPool;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.framework.vo.MenuMetaVo;
import net.dreamlu.mica.admin.framework.vo.MenuVo;
import net.dreamlu.mica.admin.project.system.entity.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单处理过滤
 *
 * @author L.cm
 */
public class MenuVoUtil {

	/**
	 * 菜单转换
	 *
	 * @param menuList 菜单列表
	 * @return 菜单
	 */
	public static List<MenuVo> transform(List<SysMenu> menuList) {
		List<MenuVo> menuVoList = new ArrayList<>();
		for (SysMenu menu : menuList) {
			String name = menu.getName();
			String path = menu.getPath();
			String title = menu.getTitle();
			String component = menu.getComponent();
			MenuVo menuVo = new MenuVo();
			menuVo.setId(menu.getId());
			menuVo.setParentId(menu.getParentId());
			menuVo.setName(StringUtil.isNotBlank(name) ? name : title);
			// 一级目录需要加斜杠，不然会报警告
			Long parentId = menu.getParentId();
			if (parentId == null && !path.startsWith("http")) {
				menuVo.setPath(StringPool.SLASH + path);
			} else {
				menuVo.setPath(path);
			}
			menuVo.setHidden(menu.getHidden());
			// 如果不是外链
			if (!menu.getIsFrame()) {
				if (parentId == null) {
					menuVo.setComponent(StringUtil.isBlank(component) ? "Layout" : component);
				} else if (StringUtil.isNotBlank(component)) {
					menuVo.setComponent(component);
				}
			}
			menuVo.setMeta(new MenuMetaVo(title, menu.getIcon(), !menu.getCache()));
			menuVoList.add(menuVo);
		}
		return menuVoList;
	}
}
