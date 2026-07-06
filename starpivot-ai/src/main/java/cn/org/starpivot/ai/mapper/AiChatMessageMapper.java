package cn.org.starpivot.ai.mapper;

import cn.org.starpivot.ai.domain.entity.AiChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {}
