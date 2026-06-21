package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.dto.SysNoticeQueryDTO;
import cn.org.starpivot.system.domain.entity.SysNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知公告Mapper接口
 * 
 * @author admin
 * @since 2026-02-05
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice>
{
    /**
     * 分页查询通知公告列表
     * 
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<SysNotice> selectPageList(Page<SysNotice> page, @Param("queryDTO") SysNoticeQueryDTO queryDTO);
}
