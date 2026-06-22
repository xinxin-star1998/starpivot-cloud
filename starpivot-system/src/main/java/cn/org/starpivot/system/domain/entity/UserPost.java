package cn.org.starpivot.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户与岗位关联实体类，对应数据库表 {@code sys_user_post}。
 * <p>
 * 维护用户与岗位的多对多关联关系。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link TableName} — 映射表名 {@code sys_user_post}</li>
 *   <li>{@link TableId} — 主键，自增策略</li>
 * </ul>
 */
@Data
@TableName("sys_user_post")
public class UserPost {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;
}
