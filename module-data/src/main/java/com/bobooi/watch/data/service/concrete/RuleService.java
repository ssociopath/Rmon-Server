package com.bobooi.watch.data.service.concrete;

import com.bobooi.watch.data.entity.Rule;
import com.bobooi.watch.data.entity.User;
import com.bobooi.watch.data.repository.concrete.RuleRepository;
import com.bobooi.watch.data.repository.concrete.UserRepository;
import com.bobooi.watch.data.service.BaseDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

/**
 * @author bobo
 * @date 2021/6/25
 */

@Service
@Slf4j
public class RuleService extends BaseDataService<Rule,Integer> {
    @Resource
    RuleRepository ruleRepository;

    public boolean deleteRuleById(Integer id){
        Rule rule = this.getOneOr(id, null);
        if(null!=rule){
            this.delete(rule);
            return true;
        }
        return false;
    }

    public boolean updateRule(Rule rule) {
        if(findOne(rule).isPresent()){
            return false;
        }
        save(rule);
        return true;
    }

    public List<Rule> getAllByMac(String mac){
        return ruleRepository.findAllByMac(mac);
    }

    public Rule getRuleByAccountAndMac(Rule rule){
        return ruleRepository.findAllByAccountAndMac(rule.getAccount(),rule.getMac());
    }
}
