package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_comment_replay")
public class PmsCommentReplay {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;

    private Long replyId;

}
