package cn.org.starpivot.system.service.support;

import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.UserVO;
import cn.org.starpivot.system.domain.dto.UserDTO;
import cn.org.starpivot.system.domain.excel.SysUserExcel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户 Excel 导入导出辅助。
 */
@Component
public class SysUserExcelSupport {

    public List<SysUserExcel> toExcelRows(List<UserVO> userList) {
        if (userList == null || userList.isEmpty()) {
            return List.of();
        }
        List<SysUserExcel> exportList = new ArrayList<>(userList.size());
        for (UserVO user : userList) {
            SysUserExcel row = new SysUserExcel();
            row.setUserName(user.getUserName());
            row.setNickName(user.getNickName());
            row.setEmail(user.getEmail());
            row.setPhonenumber(user.getPhonenumber());
            row.setSex(convertSexCodeToText(user.getSex()));
            row.setStatus(AppConstants.Status.NORMAL.equals(user.getStatus()) ? "正常" : "停用");
            row.setDeptId(user.getDeptId());
            row.setDeptName(user.getDeptName());
            row.setRemark(user.getRemark());
            exportList.add(row);
        }
        return exportList;
    }

    public UserDTO buildUserDTOFromExcel(SysUserExcel row, int rowIndex) {
        if (row == null) {
            throw new BizException(ErrorCode.USER_IMPORT_ROW_EMPTY, "第 " + rowIndex + " 行数据为空");
        }
        if (!StringUtils.hasText(row.getUserName())) {
            throw new BizException(ErrorCode.USER_IMPORT_USERNAME_EMPTY, "第 " + rowIndex + " 行用户账号不能为空");
        }
        if (!StringUtils.hasText(row.getNickName())) {
            throw new BizException(ErrorCode.USER_IMPORT_NICKNAME_EMPTY, "第 " + rowIndex + " 行用户昵称不能为空");
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(row.getUserName().trim());
        userDTO.setNickName(row.getNickName().trim());
        if (StringUtils.hasText(row.getEmail())) {
            userDTO.setEmail(row.getEmail().trim());
        }
        if (StringUtils.hasText(row.getPhonenumber())) {
            userDTO.setPhonenumber(row.getPhonenumber().trim());
        }
        String sexCode = "2";
        if (StringUtils.hasText(row.getSex())) {
            String sexText = row.getSex().trim();
            if ("男".equals(sexText)) {
                sexCode = "0";
            } else if ("女".equals(sexText)) {
                sexCode = "1";
            }
        }
        userDTO.setSex(sexCode);
        String statusCode = AppConstants.Status.NORMAL;
        if (StringUtils.hasText(row.getStatus())) {
            String statusText = row.getStatus().trim();
            if ("停用".equals(statusText) || "禁用".equals(statusText)) {
                statusCode = AppConstants.Status.DISABLE;
            }
        }
        userDTO.setStatus(statusCode);
        userDTO.setDeptId(row.getDeptId());
        if (StringUtils.hasText(row.getRemark())) {
            userDTO.setRemark(row.getRemark().trim());
        }
        return userDTO;
    }

    private String convertSexCodeToText(String sexCode) {
        if (!StringUtils.hasText(sexCode)) {
            return "未知";
        }
        return switch (sexCode) {
            case "0" -> "男";
            case "1" -> "女";
            default -> "未知";
        };
    }
}
