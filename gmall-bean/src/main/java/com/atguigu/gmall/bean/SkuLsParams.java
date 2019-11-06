package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;

/**制作出入参数的类
 * @author Jay
 * @create 2019-11-02 19:24
 */
@Data
public class SkuLsParams implements Serializable {

    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;
}

