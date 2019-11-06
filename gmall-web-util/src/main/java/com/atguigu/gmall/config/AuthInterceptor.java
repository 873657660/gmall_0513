package com.atguigu.gmall.config;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 拦截器，将登陆后的token保存到cookie中
 * @author Jay
 * @create 2019-11-05 20:03
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token，存入cookie中。只有登录的时候，才能获取newToken
        // https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
        String token = request.getParameter("token");
        // 1.token不为空，放入cookie中
        if (token != null) {
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        }
        // 2.token为空，说明token可能已经存到了cookie中，所以可以先去cookie中取出查看是否存在
        if (token == null) {
            token = CookieUtil.getCookieValue(request, "token", false);
        }
        // 3.此时再次查看是否已经从cookie中取到token,有token就将中间的对象部分解析出来
        if (token != null) {
            // 写一个方法来解析token数据，得到nickName
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);
        }


        // 如何获取到方法上的注解！
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        // 判断注解是否为空
        if (methodAnnotation != null) {
            // 从请求头中获取salt
            String salt = request.getHeader("X-forwarded-for");
            // 调用认证方法
            // http://passport.atguigu.com/verify?token=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ&salt=192.168.67.1
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            // 判断放回结果，返回success说明已经登录了
            if ("success".equals(result)) {
                Map map = getUserMapByToken(token);
                // 从解析后的token中，获取userId
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);

                return true;
            } else {
                // 当前注解中autoRedirect=true 时必须登录！
                if (methodAnnotation.autoRedirect()) {
                    // 必须登录，如果没有登录，则重定向到登录页面！
                    // http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F38.html
                    // 获取当前请求的URL（从哪个地址发送的请求，记下来）
                    String requestURL = request.getRequestURL().toString();
                    System.out.println("发送请求的requestURL:"+requestURL);

                    // 将获取到的URL，编码处理  http%3A%2F%2Fitem.gmall.com%2F38.html
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    System.out.println("编码后的encodeURL:"+encodeURL);
                    // 重定向到登录页面，后面拼接发送请求的编码后的源地址
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 截取token中间部分，封装到Map中
     * @param token
     * @return
     */
    private Map getUserMapByToken(String token) {

        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        // 创建Base64UrlCodec对象，解析token的中间部分
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();

        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);
        // 字节数组转换成字符串
        String strJson = new String(bytes);
        // 将字符串封装到Map中
        return JSON.parseObject(strJson, Map.class);

    }


}
