package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Jay
 * @create 2019-11-02 16:46
 */
@Data
public class SkuLsInfo implements Serializable {
    String id;

    BigDecimal price;

    String skuName;

    String catalog3Id;

    String skuDefaultImg;

    Long hotScore=0L;

    List<SkuLsAttrValue> skuAttrValueList;
}
