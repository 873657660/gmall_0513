package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jay
 * @create 2019-11-03 11:47
 */
@Controller
//@CrossOrigin
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    //@ResponseBody
    public String search(SkuLsParams skuLsParams, HttpServletRequest request) {
        // 首先设置每页5条数据
        skuLsParams.setPageSize(4);

        SkuLsResult skuLsResult = listService.search(skuLsParams);
        // 得到SkuInfo属性集合
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        // 得到平台属性id集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        // 根据 valueId查询平台属性和平台属性值
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);

        // 记录查询的参数条件
        String urlParam = makeUrlParm(skuLsParams);
        System.out.println("urlParam:"+urlParam);

        // 声明一个面包屑
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

        // baseAttrInfoList，获取平台属性值集合
        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            // 获取平台属性值集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            // 与参数上的valueId 进行匹配 是否需要判断参数是否有空？需要判空！
            if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {

                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (String valueId : skuLsParams.getValueId()) {
                        // 根据参数的valueId 与实体类的baseAttrValue.id
                        if (valueId.equals(baseAttrValue.getId())){
                            // 删除平台属性对象
                            iterator.remove();

                            // 平台属性名称：平台属性值名称
                            BaseAttrValue baseAttrValueed = new BaseAttrValue();
                            baseAttrValueed.setValueName(baseAttrInfo.getAttrName() +":"+ baseAttrValue.getValueName());

                            String newParam = makeUrlParm(skuLsParams, valueId);
                            System.out.println("newParam:"+newParam);
                            // 将新的newParam放到面包屑上面，点击跳到新的url
                            baseAttrValueed.setUrlParam(newParam);

                            // 添加到面包屑集合中
                            baseAttrValueArrayList.add(baseAttrValueed);
                        }
                    }
                }
            }

        }

        request.setAttribute("pageNo", skuLsParams.getPageNo());

        request.setAttribute("totalPages", skuLsResult.getTotalPages());

        request.setAttribute("baseAttrValueArrayList", baseAttrValueArrayList);

        request.setAttribute("keyword", skuLsParams.getKeyword());

        request.setAttribute("urlParam", urlParam);

        request.setAttribute("baseAttrInfoList", baseAttrInfoList);

        request.setAttribute("skuLsInfoList", skuLsInfoList);

        return "list";
    }

    /**
     * @param skuLsParams 传入的参数列表
     * @param excludeValueIds 点击面包屑时所得到的valueId 平台属性值Id
     * @return
     */
    private String makeUrlParm(SkuLsParams skuLsParams, String... excludeValueIds) {
        String urlParam = "";

        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0 ) {
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }

        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }

        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            // 循环拼接
            for (String valueId : skuLsParams.getValueId()) {
                if (excludeValueIds != null && excludeValueIds.length > 0){
                    // 获取点击的平台属性id
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)) {
                        // 跳出本次for循环
                        continue;
                    }
                }

                // 如果urlParam长度不为0，说明前面有keyword或是catalog3Id
                if (urlParam.length() > 0){
                    urlParam += "&";
                }
                urlParam += "valueId=" + valueId;
            }
        }

        return urlParam;
    }
}
