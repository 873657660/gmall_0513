package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.config.JwtUtil;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jay
 * @create 2019-11-05 17:02
 */
@Controller
public class PassportController {

    @Value("${token.key}")
    private String key;

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("index")
    public String index(HttpServletRequest request) {
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request) {
        // 调用登陆服务器
        UserInfo info = userInfoService.login(userInfo);
        // 如果info不为空，生成token
        if (info != null) {
            // salt从nginx代理中获取
            String salt = request.getHeader("X-forwarded-for");
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", info.getId());
            map.put("nickName", info.getNickName());

            String token = JwtUtil.encode(key, map, salt);
            return token;
        } else {
            return "fail";
        }

    }

    @RequestMapping("verify")
    public String verify(HttpServletRequest request) {
        // 从地址栏中获取token和salt
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");

        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        // 如果获取的map不为空，则取出userId，根据userId取redis中查询是否存在此数据
        if (map != null && map.size() > 0) {
            String userId = (String) map.get("userId");
            UserInfo userInfo = userInfoService.verify(userId);

            if (userInfo != null) {
                return "success";
            } else {
                return "fail";
            }
        }
        return "fail";
    }


}
