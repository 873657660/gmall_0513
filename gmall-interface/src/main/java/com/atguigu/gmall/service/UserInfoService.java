package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

/**
 * @author Jay
 * @create 2019-10-24 17:38
 */
public interface UserInfoService {

    // 查询所有数据
    List<UserInfo> findAll();

    // 根据用户的loginname或i是name查询数据
    List<UserInfo> findUserInfoProperty(UserInfo userInfo);

    // 按条件查询
    List<UserInfo> findUserByName(UserInfo userInfo);

    // 插入数据
    void addUser(UserInfo userInfo);

    // 修改数据
    void updateUser(UserInfo userInfo);

    // 删除数据
    void delUser(UserInfo userInfo);

    // 根据主键删除
    void delByPrimaryKey(String id);

    /**
     * 根据用户id查询用户地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressByUserId(String userId);

}
