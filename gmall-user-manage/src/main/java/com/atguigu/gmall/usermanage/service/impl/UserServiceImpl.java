package com.atguigu.gmall.usermanage.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author Jay
 * @create 2019-10-25 16:26
 */
@Service
public class UserServiceImpl implements UserInfoService {

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*48;


    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserInfo> findUserInfoProperty(UserInfo userInfo) {
        return null;
    }

    @Override
    public List<UserInfo> findUserByName(UserInfo userInfo) {
        return null;
    }

    @Override
    public void addUser(UserInfo userInfo) {

    }

    @Override
    public void updateUser(UserInfo userInfo) {

    }

    @Override
    public void delUser(UserInfo userInfo) {

    }

    @Override
    public void delByPrimaryKey(String id) {

    }

    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
        Example example = new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId",userId);
        return userAddressMapper.selectByExample(example);
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(UserAddress address) {
        return userAddressMapper.select(address);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        // 获取登陆信息中的密码，加密后与数据库中的比较
        String passwd = userInfo.getPasswd();
        String newPwd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPwd);
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if (info != null) {
            // 数据库中有信息，保存到redis中，设置过期时间
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix+info.getId()+userinfoKey_suffix;
            jedis.setex(userKey, userKey_timeOut, JSON.toJSONString(info));

            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        // 拼接userKey,查询redis，如果返回结果不为空，则转换为UserInfo对象
        String userKey = userKey_prefix+ userId +userinfoKey_suffix;
        String userJson = jedis.get(userKey);
        
        if (!StringUtils.isEmpty(userJson)) {
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            jedis.close();
            return userInfo;
        }
        jedis.close();
        return null;
    }

}
