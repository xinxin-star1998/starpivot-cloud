package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.bo.SysNoticeVO;
import cn.org.starpivot.system.domain.dto.SysNoticeDTO;
import cn.org.starpivot.system.domain.dto.SysNoticeQueryDTO;
import cn.org.starpivot.system.domain.entity.SysNotice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 通知公告服务接口。
 * <p>
 * 提供系统通知公告的增删改查及分页查询能力。
 * 继承 {@link IService} 获得 MyBatis-Plus 基础持久化能力。
 * </p>
 */
public interface ISysNoticeService extends IService<SysNotice>
{
    /**
     * 分页查询通知公告列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResponse<SysNoticeVO> selectSysNoticePage(SysNoticeQueryDTO queryDTO);

    /**
     * 根据主键查询通知公告详细信息
     * 
     * @param noticeId 通知公告主键
     * @return 通知公告信息
     */
    SysNoticeVO selectSysNoticeByNoticeId(Integer noticeId);

    /**
     * 新增通知公告
     * 
     * @param sysNoticeDTO 通知公告信息
     * @return 是否成功
     */
    boolean insertSysNotice(SysNoticeDTO sysNoticeDTO);

    /**
     * 修改通知公告
     * 
     * @param sysNoticeDTO 通知公告信息
     * @return 是否成功
     */
    boolean updateSysNotice(SysNoticeDTO sysNoticeDTO);

    /**
     * 批量删除通知公告
     * 
     * @param noticeIds 需要删除的通知公告主键数组
     * @return 是否成功
     */
    boolean deleteSysNoticeByNoticeIds(List<Long> noticeIds);
}
