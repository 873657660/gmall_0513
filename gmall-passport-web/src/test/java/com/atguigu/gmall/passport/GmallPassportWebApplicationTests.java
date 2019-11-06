package com.atguigu.gmall.passport;

import com.atguigu.gmall.passport.config.JwtUtil;
import io.jsonwebtoken.Jwt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void testJWT() {
        String key = "atguigu";
        String salt = "192.168.79.128";
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", "1001");
        map.put("nickName", "admin");
        String token = JwtUtil.encode(key, map, salt);

        System.out.println("加密后的token:"+token);
        System.out.println("*************解密**************");
        Map<String, Object> decodeMap = JwtUtil.decode(token, key, salt);
        System.out.println("解密后的decodeMap:"+decodeMap);
    }

}
