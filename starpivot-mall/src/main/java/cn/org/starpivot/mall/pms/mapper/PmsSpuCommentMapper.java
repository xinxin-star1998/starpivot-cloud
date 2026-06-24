package cn.org.starpivot.mall.pms.mapper;

import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.entity.PmsSpuComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PmsSpuCommentMapper extends BaseMapper<PmsSpuComment> {

    IPage<PmsSpuComment> selectPageList(Page<PmsSpuComment> page, @Param("req") CommentReqBo reqBo);
}
