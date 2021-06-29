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
    @Resource
    UserRepository userRepository;

    public boolean deleteById(Integer id){
        Rule rule = this.getOneOr(id, null);
        if(null!=rule){
            this.delete(rule);
            return true;
        }
        return false;
    }

    public boolean update(Integer id, Integer pcId, String account, Character permission) {
        Rule rule;
        User user = userRepository.findUserByAccount(account);
        if(user!=null && ruleRepository.findByAccount(account)==null){
            if(id==null){
                rule = new Rule();
            }else{
                rule = this.getOneOr(id, null);
                if(null==rule){
                    return false;
                }
            }
            rule.setPcId(pcId);
            rule.setUserId(user.getId());
            rule.setAccount(account);
            rule.setPermission(permission);
            System.out.println(rule);
            this.save(rule);
            return true;
        }



        return false;
    }

    public List<Rule> findAllByPcId(Integer pcId){
        return ruleRepository.findAllByPcId(pcId);
    }

    public Rule findOneByUserIdAndPcId(Integer userId,Integer pcId){
        return ruleRepository.findByUserIdAndPcId(userId,pcId);
    }
}
