package com.bobooi.watch.data.service.concrete;

import com.bobooi.watch.data.entity.Pc;
import com.bobooi.watch.data.entity.Rule;
import com.bobooi.watch.data.repository.concrete.PcRepository;
import com.bobooi.watch.data.repository.concrete.RuleRepository;
import com.bobooi.watch.data.service.BaseDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bobo
 * @date 2021/6/28
 */

@Service
@Slf4j
public class PcService extends BaseDataService<Pc,Integer> {
    @Resource
    PcRepository pcRepository;

    public Pc findOneByMac(String mac){
        return pcRepository.getByMac(mac);
    }

    public Pc login(Pc pc){
        if(pcRepository.getByMac(pc.getMac())==null){
            return this.insert(pc);
        }
        return findOne(pc).orElse(null);
    }
}
