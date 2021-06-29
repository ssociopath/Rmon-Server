package com.bobooi.watch.data.repository.concrete;

import com.bobooi.watch.data.entity.Pc;
import com.bobooi.watch.data.repository.DataRepository;

/**
 * @author bobo
 * @date 2021/6/27
 */

public interface PcRepository extends DataRepository<Pc,Integer> {
    Pc getByMac(String mac);
}
