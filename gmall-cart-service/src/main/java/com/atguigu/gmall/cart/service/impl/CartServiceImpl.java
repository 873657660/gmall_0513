package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.service.CartService;

/**
 * @author Jay
 * @create 2019-11-06 17:05
 */
@Service
public class CartServiceImpl implements CartService {

    /**
     * 添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {



    }
}
