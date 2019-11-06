package com.atguigu.gmall.service;

/**
 * @author Jay
 * @create 2019-11-06 17:02
 */
public interface CartService {

    /**
     * 根据商品id，用户id和商品数量添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(String skuId, String userId, Integer skuNum);
}
