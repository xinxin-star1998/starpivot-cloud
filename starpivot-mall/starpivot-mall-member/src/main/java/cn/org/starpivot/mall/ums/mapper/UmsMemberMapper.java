package cn.org.starpivot.mall.ums.mapper;

import cn.org.starpivot.mall.ums.entity.UmsMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UmsMemberMapper extends BaseMapper<UmsMember> {

    /**
     * 原子增加积分。
     *
     * @param memberId 会员 ID
     * @param delta    增量（正数增加，负数减少）
     * @return 受影响行数
     */
    @Update("UPDATE ums_member SET integration = IFNULL(integration, 0) + #{delta} WHERE id = #{memberId}")
    int addIntegration(@Param("memberId") Long memberId, @Param("delta") int delta);

    /**
     * 原子减少积分，且不低于 0。
     *
     * @param memberId 会员 ID
     * @param delta    减少量（正数）
     * @return 受影响行数
     */
    @Update("UPDATE ums_member SET integration = GREATEST(IFNULL(integration, 0) - #{delta}, 0) WHERE id = #{memberId}")
    int deductIntegrationClampZero(@Param("memberId") Long memberId, @Param("delta") int delta);

    /**
     * 原子增加成长值。
     *
     * @param memberId 会员 ID
     * @param delta    增量（正数增加，负数减少）
     * @return 受影响行数
     */
    @Update("UPDATE ums_member SET growth = IFNULL(growth, 0) + #{delta} WHERE id = #{memberId}")
    int addGrowth(@Param("memberId") Long memberId, @Param("delta") int delta);

    /**
     * 原子减少成长值，且不低于 0。
     *
     * @param memberId 会员 ID
     * @param delta    减少量（正数）
     * @return 受影响行数
     */
    @Update("UPDATE ums_member SET growth = GREATEST(IFNULL(growth, 0) - #{delta}, 0) WHERE id = #{memberId}")
    int deductGrowthClampZero(@Param("memberId") Long memberId, @Param("delta") int delta);

    /**
     * 原子扣减积分（带余额校验），积分不足时返回 0。
     *
     * @param memberId 会员 ID
     * @param points   扣减量（正数）
     * @return 受影响行数（1 表示成功，0 表示余额不足）
     */
    @Update("UPDATE ums_member SET integration = IFNULL(integration, 0) - #{points} " +
            "WHERE id = #{memberId} AND IFNULL(integration, 0) >= #{points}")
    int deductIntegrationIfSufficient(@Param("memberId") Long memberId, @Param("points") int points);

    /**
     * 仅更新会员等级，避免整实体 updateById 覆盖并发中的积分/成长值。
     */
    @Update("UPDATE ums_member SET level_id = #{levelId} WHERE id = #{memberId}")
    int updateLevelId(@Param("memberId") Long memberId, @Param("levelId") Long levelId);
}
