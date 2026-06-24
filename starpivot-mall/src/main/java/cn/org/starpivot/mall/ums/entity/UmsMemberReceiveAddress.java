package cn.org.starpivot.mall.ums.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_member_receive_address")
public class UmsMemberReceiveAddress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private String name;

    private String phone;

    private String postCode;

    private String province;

    private String city;

    private String region;

    private String detailAddress;

    private String areacode;

    private Integer defaultStatus;

}
