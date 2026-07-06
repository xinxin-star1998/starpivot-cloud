package cn.org.starpivot.ai.mapper;

import cn.org.starpivot.ai.domain.entity.AiKnowledgeChunk;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeChunkHitVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiKnowledgeChunkMapper extends BaseMapper<AiKnowledgeChunk> {

    List<AiKnowledgeChunkHitVo> searchFulltext(@Param("query") String query, @Param("topK") int topK);

    List<AiKnowledgeChunkHitVo> searchLike(@Param("keyword") String keyword, @Param("topK") int topK);

    List<AiKnowledgeChunkHitVo> listEmbeddableChunkBatch(
            @Param("lastChunkId") Long lastChunkId, @Param("limit") int limit);

    int insertBatch(@Param("list") List<AiKnowledgeChunk> list);
}
