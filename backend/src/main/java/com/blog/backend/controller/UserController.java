package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.user.UserLoginRequest;
import com.blog.backend.model.dto.user.UserQueryRequest;
import com.blog.backend.model.dto.user.UserRegisterRequest;
import com.blog.backend.model.dto.user.UserUpdateMyRequest;
import com.blog.backend.model.dto.user.UserUpdatePasswordRequest;
import com.blog.backend.model.entity.User;
import com.blog.backend.model.vo.user.LoginUserVO;
import com.blog.backend.model.vo.user.UserVO;
import com.blog.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long id = userService.userRegister(
                request.getUsername(),
                request.getPassword(),
                request.getCheckPassword()
        );
        return ResultUtils.success(id);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(request.getUsername(), request.getPassword());
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        boolean result = userService.userLogout();
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/current")
    public BaseResponse<LoginUserVO> getCurrentUser() {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        boolean result = userService.updateMyUser(
                loginUser.getId(),
                request.getNickname(),
                request.getUserProfile()
        );
        return ResultUtils.success(result);
    }

    /**
     * 修改密码
     */
    @PutMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UserUpdatePasswordRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.updatePassword(
                request.getOldPassword(),
                request.getNewPassword()
        );
        return ResultUtils.success(result);
    }

    // ============ 管理员接口 ============

    /**
     * 分页查询用户列表（管理员）
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(request));
        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
