package com.manong.service;

import com.manong.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 栋栋
 * @since 2022-08-12
 */
public interface PermissionService extends IService<Permission> {
    /**
     * 根据用户ID查询菜单列表
     * @param userId
     * @return
     */
    List<Permission> findPermissionListByUserId(Long userId);
}
