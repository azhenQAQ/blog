package com.blog.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.mapper.UserMapper;
import com.blog.backend.model.dto.user.UserQueryRequest;
import com.blog.backend.model.entity.User;
import com.blog.backend.model.enums.UserRoleEnum;
import com.blog.backend.model.vo.user.LoginUserVO;
import com.blog.backend.model.vo.user.UserVO;
import com.blog.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 密码加密盐值
     */
    private static final String SALT = "QAQ";

    @Override
    public long userRegister(String username, String password, String checkPassword) {
        if (StrUtil.hasBlank(username, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");
        }
        if (password.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 检查账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        // 加密密码
        String encryptPassword = getEncryptPassword(password);

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        user.setNickname("无名");
        user.setRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String password) {
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String username, String password) {
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 校验账号密码
        String encryptPassword = getEncryptPassword(password);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", encryptPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // Sa-Token 登录
        StpUtil.login(user.getId());

        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser() {
        // 先确保已登录（未登录会抛出 NotLoginException）
        StpUtil.checkLogin();

        Object loginId = StpUtil.getLoginId();
        User currentUser = this.getById(Long.valueOf(loginId.toString()));
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public boolean userLogout() {
        // 先确保已登录
        StpUtil.checkLogin();
        StpUtil.logout();
        return true;
    }

    @Override
    public boolean updateMyUser(long loginUserId, String nickname, String userProfile) {
        if (loginUserId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (ObjectUtil.isAllEmpty(nickname, userProfile)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (StrUtil.isNotBlank(nickname) && nickname.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称过长");
        }
        if (StrUtil.isNotBlank(userProfile) && userProfile.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户简介过长");
        }

        User user = new User();
        user.setId(loginUserId);
        // 只更新传入的非空字段，避免覆盖已有数据
        if (StrUtil.isNotBlank(nickname)) {
            user.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(userProfile)) {
            user.setUserProfile(userProfile);
        }
        return this.updateById(user);
    }

    @Override
    public boolean updatePassword(String oldPassword, String newPassword) {
        if (StrUtil.hasBlank(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码过短");
        }

        // 获取当前登录用户
        User user = getLoginUser();
        String oldEncryptPassword = getEncryptPassword(oldPassword);
        if (!oldEncryptPassword.equals(user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }

        // 更新密码
        String newEncryptPassword = getEncryptPassword(newPassword);
        user.setPassword(newEncryptPassword);
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败");
        }

        // 强制该用户所有登录会话下线
        StpUtil.logout(user.getId());

        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String username = userQueryRequest.getUsername();
        String nickname = userQueryRequest.getNickname();
        String userProfile = userQueryRequest.getUserProfile();
        String role = userQueryRequest.getRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(role), "role", role);
        queryWrapper.like(StrUtil.isNotBlank(username), "username", username);
        queryWrapper.like(StrUtil.isNotBlank(nickname), "nickname", nickname);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "user_profile", userProfile);

        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), StrUtil.toUnderlineCase(sortField));
        }

        return queryWrapper;
    }
}
