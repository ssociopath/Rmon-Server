package com.bobooi.watch.data.repository.concrete;


import com.bobooi.watch.data.entity.Rule;
import com.bobooi.watch.data.repository.DataRepository;

import java.util.List;

/**
 * @author bobo
 * @date 2021/4/9
 */

public interface RuleRepository extends DataRepository<Rule, Integer> {
    List<Rule> findAllByPcId(Integer pcId);
    Rule findByAccount(String account);
    Rule findByUserIdAndPcId(Integer userId, Integer pcId);
}
