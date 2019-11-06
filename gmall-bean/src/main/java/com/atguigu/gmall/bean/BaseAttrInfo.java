package com.atguigu.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Jay
 * @create 2019-10-26 16:49
 */
@Data
public class BaseAttrInfo implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String  id;

    @Column
    private String attrName;

    @Column
    private String catalog3Id;

    // 不是数据库中的原生字段
    @Transient
    private List<BaseAttrValue> attrValueList;


}