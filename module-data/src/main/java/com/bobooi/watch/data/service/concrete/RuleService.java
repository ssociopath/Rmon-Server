package com.bobooi.watch.data.service.concrete;

import com.bobooi.watch.common.exception.ApplicationException;
import com.bobooi.watch.common.exception.AssertUtils;
import com.bobooi.watch.common.response.SystemCodeEnum;
import com.bobooi.watch.common.utils.misc.Constant;
import com.bobooi.watch.data.entity.Rule;
import com.bobooi.watch.data.service.BaseDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author bobo
 * @date 2021/6/25
 */

@Service
@Slf4j
public class RuleService extends BaseDataService<Rule,Integer> {

    public Rule addPcUser(Rule rule){
        AssertUtils.isNull(findOne(rule), new ApplicationException(SystemCodeEnum.ARGUMENT_WRONG, "相同的用户权限已存在！"));
        AssertUtils.isFalse(rule.getMac().length() != Constant.MAC_LEN, new ApplicationException(SystemCodeEnum.ARGUMENT_WRONG, "mac地址长度有误！"));
        return this.insert(rule);
    }
}
