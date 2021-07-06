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

    public Rule toRule(String mac){
        return new Rule(ruleId,account,mac, "允许操作".equals(permission) ?'1':'2');
    }

    public static RuleVO fromRule(Rule rule){
        return builder().ruleId(rule.getId())
                .account(rule.getAccount())
                .permission(rule.getPermission()=='1'?"允许操作":"允许访问")
                .build();
    }

}
