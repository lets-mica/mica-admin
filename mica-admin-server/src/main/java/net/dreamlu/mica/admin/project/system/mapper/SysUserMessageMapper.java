package net.dreamlu.mica.admin.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import net.dreamlu.mica.admin.project.system.entity.SysUserMessage;
import net.dreamlu.mica.admin.project.system.pojo.UserMessageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 系统消息推送记录 Mapper 接口
 * </p>
 *
 * @author L.cm
 */
@Mapper
public interface SysUserMessageMapper extends BaseMapper<SysUserMessage> {

	/**
	 * 查询用户未读消息（联查 sys_message）
	 *
	 * @param userId 用户ID
	 * @return 未读消息列表
	 */
	List<UserMessageVo> selectUnreadByUserId(@Param("userId") Long userId);

	/**
	 * 查询用户所有消息（联查 sys_message），支持分页
	 *
	 * @param page       分页参数
	 * @param userId     用户ID
	 * @param blurry     模糊搜索关键字（标题/内容）
	 * @param createTime 创建时间范围
	 * @return 消息分页
	 */
	IPage<UserMessageVo> selectByUserId(IPage<UserMessageVo> page,
									   @Param("userId") Long userId,
									   @Param("blurry") String blurry,
									   @Param("createTime") List<LocalDateTime> createTime);

}
