package com.blog.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.backend.model.dto.user.UserQueryRequest;
import com.blog.backend.model.entity.User;
import com.blog.backend.model.vo.user.LoginUserVO;
import com.blog.backend.model.vo.user.UserVO;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(String username, String password, String checkPassword);

    /**
     * 密码加密
     */
    String getEncryptPassword(String password);

    /**
     * 用户登录
     */
    LoginUserVO userLogin(String username, String password);

    /**
     * 获取当前登录用户
     */
    User getLoginUser();

    /**
     * 获取脱敏的登录用户视图
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登出
     */
    boolean userLogout();

    /**
     * 更新个人信息
     */
    boolean updateMyUser(long loginUserId, String nickname, String userProfile);

    /**
     * 修改当前登录用户密码
     */
    boolean updatePassword(String oldPassword, String newPassword);

    /**
     * 获取脱敏用户视图
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏用户视图列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
