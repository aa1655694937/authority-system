package com.manong.dao;

import com.manong.entity.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 栋栋
 * @since 2022-08-12
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询菜单列表
     * @param userId
     * @return
     */
    List<Permission> findPermissionListByUserId(Long userId);
}
