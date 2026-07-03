package cn.org.starpivot.mall.pms.mapper;

import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.entity.PmsSpuComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PmsSpuCommentMapper extends BaseMapper<PmsSpuComment> {

    IPage<PmsSpuComment> selectPageList(Page<PmsSpuComment> page, @Param("req") CommentReqBo reqBo);

    @Select("""
            SELECT COUNT(1) AS total, AVG(star) AS avgStar
            FROM pms_spu_comment
            WHERE spu_id = #{spuId} AND show_status = 1
              AND (comment_type IS NULL OR comment_type = 0)
            """)
    Map<String, Object> selectSummaryBySpuId(@Param("spuId") Long spuId);

    List<Map<String, Object>> selectSummaryBySpuIds(@Param("spuIds") List<Long> spuIds);
}
