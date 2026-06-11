package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.project.system.mapper.SysDictMapper;
import net.dreamlu.mica.admin.project.system.entity.SysDict;
import net.dreamlu.mica.admin.project.system.entity.SysDictInfo;
import net.dreamlu.mica.admin.project.system.pojo.DictQuery;
import net.dreamlu.mica.admin.project.system.service.ISysDictInfoService;
import net.dreamlu.mica.admin.project.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-07-19
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {
	@Autowired
	private ISysDictInfoService dictInfoService;

	@Override
	public Wrapper<SysDict> getQueryWrapper(DictQuery query) {
		String queryBlurry = query.getBlurry();
		LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
//		name,description
		wrapper.and(StringUtil.isNotBlank(queryBlurry), w -> w
			.like(SysDict::getName, queryBlurry)
			.or().like(SysDict::getDescription, queryBlurry)
			.or().like(SysDict::getRemark, queryBlurry));
		return wrapper;
	}

	@Override
	public boolean deleteIfUnusedByIds(Collection<Long> ids) {
		List<SysDict> sysDictList = super.listByIds(ids);
		if (sysDictList == null || sysDictList.isEmpty()) {
			return false;
		}
		Set<String> dictNameSet = sysDictList.stream()
			.map(SysDict::getName)
			.collect(Collectors.toSet());
		List<SysDictInfo> dictInfoList = dictInfoService.getListByDictNames(dictNameSet);
		if (dictInfoList != null && !dictInfoList.isEmpty()) {
			R.throwFail("存在字典详情关系");
		}
		return super.removeByIds(ids);
	}

}
