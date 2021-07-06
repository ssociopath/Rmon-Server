package com.bobooi.watch.data.service.concrete;

import com.bobooi.watch.common.exception.ApplicationException;
import com.bobooi.watch.common.exception.AssertUtils;
import com.bobooi.watch.common.response.SystemCodeEnum;
import com.bobooi.watch.common.utils.misc.Constant;
import com.bobooi.watch.data.entity.User;
import com.bobooi.watch.data.repository.concrete.UserRepository;
import com.bobooi.watch.data.service.BaseDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author bobo
 * @date 2021/6/25
 */

@Service
@Slf4j
public class UserService extends BaseDataService<User, Integer> {
    @Resource
    UserRepository userRepository;

    public void addUser(User user){
        AssertUtils.isNull(userRepository.findUserByAccount(user.getAccount()), new ApplicationException(SystemCodeEnum.ARGUMENT_WRONG, "帐号已存在！"));
        AssertUtils.isFalse(user.getPassword().length() > Constant.ACCOUNT_MAX_LEN, new ApplicationException(SystemCodeEnum.ARGUMENT_WRONG, "帐号不能超过20位！"));
        AssertUtils.isFalse(user.getPassword().length() > Constant.PASSWORD_MAX_LEN, new ApplicationException(SystemCodeEnum.ARGUMENT_WRONG, "密码不能超过8位！"));
        user.setPassword(DigestUtils.sha1Hex(user.getPassword()));
        this.insert(user);
    }

    public User getUserByAccountAndPwd(User user) {
        User theUser = findOne(user).orElse(null);
        AssertUtils.isTrue(theUser!=null, ApplicationException.withResponse(SystemCodeEnum.NEED_LOGIN, "用户名或密码错误"));
        return findOne(user).orElse(null);
    }
}
