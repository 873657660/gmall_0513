package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jay
 * @create 2019-10-25 16:53
 */
@RestController
public class OrderController {

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("trade")
    public List<UserAddress> trade(String userId) {

        return userInfoService.getUserAddressByUserId(userId);
    }

    @RequestMapping("trade1")
    public List<UserAddress> trade(UserAddress address) {

        return userInfoService.getUserAddressByUserId(address);
    }
}
