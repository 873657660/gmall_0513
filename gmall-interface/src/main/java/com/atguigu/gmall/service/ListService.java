package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;


/**
 * @author Jay
 * @create 2019-11-02 16:49
 */
public interface ListService {

    /**
     * 保存sku数据业务
     * @param skuLsInfo
     */
    void
    saveSkuInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据用户输入的条件生成 DSL语句。并返回结果
     * @param skuLsParams
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 按照商品id更新es，更新具体数据
     * @param skuId
     */
    void incrHotScore(String skuId);
}
