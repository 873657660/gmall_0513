package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.config.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Jay
 * @create 2019-11-06 17:07
 */
@Controller
public class CartController {

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart() {

        return "success";
    }

}
