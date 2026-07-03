package cn.org.starpivot.api.member.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 会员内部查询 DTO（跨服务传输，非持久化实体）。
 */
@Data
public class MemberDto implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String mobile;
    private String header;
    private Integer status;
    private Long levelId;
    private Integer integration;
    private Integer growth;
}
