package net.dreamlu.mica.admin.framework.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.dreamlu.mica.core.utils.BeanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 分页工具类
 *
 * @author L.cm
 */
public class PageUtil {

	/**
	 * Page 转换
	 *
	 * @param page   IPage
	 * @param target 需要copy转换的类型
	 * @param <T>    泛型
	 * @return PageResult
	 */
	public static <T> Page<T> toPage(IPage<?> page, Class<T> target) {
		List<T> records = BeanUtil.copy(page.getRecords(), target);
		return toPage(page, records);
	}

	/**
	 * Page 转换
	 *
	 * @param page    IPage
	 * @param records 转换过的list模型
	 * @param <T>     泛型
	 * @return PageResult
	 */
	public static <T> Page<T> toPage(IPage<?> page, List<T> records) {
		Page<T> pageResult = new Page<>();
		pageResult.setCurrent(page.getCurrent());
		pageResult.setSize(page.getSize());
		pageResult.setPages(page.getPages());
		pageResult.setTotal(page.getTotal());
		pageResult.setRecords(records);
		return pageResult;
	}

	/**
	 * Page 转换
	 *
	 * @param page     IPage
	 * @param function 转换过的函数
	 * @param <T>      泛型
	 * @return PageResult
	 */
	public static <T, R> Page<R> toPage(IPage<T> page, Function<T, R> function) {
		List<R> records = new ArrayList<>();
		for (T record : page.getRecords()) {
			records.add(function.apply(record));
		}
		return toPage(page, records);
	}

}
