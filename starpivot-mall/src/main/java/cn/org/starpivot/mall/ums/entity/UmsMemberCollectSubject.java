package cn.org.starpivot.mall.ums.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_member_collect_subject")
public class UmsMemberCollectSubject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Long subjectId;

    private String subjectName;

    private String subjectImg;

    private String subjectUrl;

}
