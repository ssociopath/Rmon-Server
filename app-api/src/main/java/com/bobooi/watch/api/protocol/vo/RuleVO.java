package com.bobooi.watch.api.protocol.vo;

import com.bobooi.watch.data.entity.Rule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bobo
 * @date 2021/6/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleVO {
    private Integer ruleId;
    private String account;
    private String permission;

    public static RuleVO fromRule(Rule rule){
        String per = "未定义权限";
        switch (rule.getPermission()){
            case '1':
                per = "允许操作";
                break;
            case '2':
                per = "允许监控";
                break;
            default: break;
        }

        return builder().ruleId(rule.getId())
                .account(rule.getAccount())
                .permission(per)
                .build();
    }

    public Character getBytePermission(){
        char per = '0';
        switch (permission){
            case "允许操作":
                per = '1';
                break;
            case "允许监控":
                per = '2';
                break;
            default: break;
        }
        return per;
    }

}
