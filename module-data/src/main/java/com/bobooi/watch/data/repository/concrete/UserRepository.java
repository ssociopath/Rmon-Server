package com.bobooi.watch.data.repository.concrete;

import com.bobooi.watch.data.entity.User;
import com.bobooi.watch.data.repository.DataRepository;

/**
 * @author bobo
 * @date 2021/3/31
 */

public interface UserRepository extends DataRepository<User, Integer> {
    /**
     * 通过帐号和密码查找用户是否存在
     * @param account 帐号
     * @param password 密码
     * @return 如果存在，返回user，否则返回null
     */
    User findUserByAccountAndPassword(String account, String password);

    /**
     * 通过帐号和密码查找用户是否存在
     * @param account 帐号
     * @return 如果存在，返回user，否则返回null
     */
    User findUserByAccount(String account);
}
