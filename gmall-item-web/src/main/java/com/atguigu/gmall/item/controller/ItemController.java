package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jay
 * @create 2019-10-30 17:25
 */
@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @LoginRequire
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, HttpServletRequest request) {
        System.out.println("打印skuId"+skuId);

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        // 根据skuId 查询skuImage
        //List<SkuImage> skuImageList = manageService.getImageList(skuId);
        // 获取到skuinfo后，将skuImageList放到skuinfo中
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();

        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);

        // 通过spuid查询销售属性值id对应的skuid
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        // 拼接json串 {"123|127":"37", "124|128":"38"}
        Map<String, String> map = new HashMap<>();
        String key = "";
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            // 当key不为空的时候拼接
            if (key.length() > 0) {
                key += "|";
            }

            key += skuSaleAttrValue.getSaleAttrValueId();
            // 当到最后一个，或是与下一个skuid不同时，停止拼接
            if ((i+1)==skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())) {
                map.put(key, skuSaleAttrValue.getSkuId());
                key = "";
            }
        }

        String valuesSkuJson = JSON.toJSONString(map);
        System.out.println("拼接后的JSON 串"+valuesSkuJson);

        request.setAttribute("valuesSkuJson", valuesSkuJson);
        request.setAttribute("spuSaleAttrList", spuSaleAttrList);
        //request.setAttribute("skuImageList", skuImageList);
        request.setAttribute("skuInfo", skuInfo);

        listService.incrHotScore(skuId);

        return "item";
    }

}
