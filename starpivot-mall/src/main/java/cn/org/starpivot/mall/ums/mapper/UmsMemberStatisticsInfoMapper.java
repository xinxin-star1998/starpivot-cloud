package cn.org.starpivot.mall.ums.mapper;

import cn.org.starpivot.mall.ums.domain.bo.MemberStatisticsReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberStatisticsVo;
import cn.org.starpivot.mall.ums.entity.UmsMemberStatisticsInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UmsMemberStatisticsInfoMapper extends BaseMapper<UmsMemberStatisticsInfo> {

    IPage<MemberStatisticsVo> selectPageList(Page<MemberStatisticsVo> page, @Param("query") MemberStatisticsReqBo query);

    UmsMemberStatisticsInfo aggregateByMemberId(@Param("memberId") Long memberId);
}
