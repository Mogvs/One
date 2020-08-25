package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.until.CommunityConstant;
import com.nowcoder.community.until.CommunityUtil;
import com.nowcoder.community.until.MailClient;
import com.nowcoder.community.until.RedisKeyUntil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService  implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    private DiscussPostService.LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        //return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;


    public Map<String,Object> register(User user){

        Map<String,Object>map= new  HashMap<>();

        if(user==null){

            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){

            map.put("usernamemsg","账号不能为空");
            return map;

        }if(StringUtils.isBlank(user.getPassword())){

            map.put("passwordmsg","密码不能为空");
            return map;

        }if(StringUtils.isBlank(user.getEmail())){

            map.put("emailmsg","邮箱不能为空");
            return map;

        }

        User u=userMapper.selectByName(user.getUsername());
        if(u!=null){

            map.put("usernamemsg","账号已存在");
            return map;
        }
        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null){

            map.put("emailmsg","邮箱已存在");
            return map;
        }

        //注册开始

        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));//生成随机的字符串用于与用户密码进行加密
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));//生成加密密码
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(10000)));
        user.setCreateTime(new Date());

        //保存提交的数据
        userMapper.insertUser(user);

        //激活邮件
        Context context=new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);


    return map;
    }

    public int activation(int userId,String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId,1);
            //更新数据之后清除缓存

            clearCache(userId);


            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public Map<String,Object> login(String username,String password, int expiredSeconds){
        Map<String,Object> map=new HashMap<>();

        if(StringUtils.isBlank(username)){
            map.put("usernamemsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordmsg","密码不能为空");
            return map;
        }
        //合法性
        User user=userMapper.selectByName(username);
        if(user==null){
            map.put("usernamemsg","该账号不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernamemsg","该账号未激活");
            return map;
        }
        //验证密码的正确性
       password=CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordmsg","密码不正确");
            return map;
        }

        //生成登录实体
        LoginTicket loginTicket=new LoginTicket();

        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        loginTicket.setTicket(CommunityUtil.generateUUID());
        //loginTicketMapper.insertLoginTicket(lginTicket);



        String redisKey = RedisKeyUntil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);


        map.put("ticket", loginTicket.getTicket());

        return map;
    }
    public void logout(String ticket) {

        //loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUntil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }


    public LoginTicket findLoginTicket(String ticket)
    {
        String redisKey = RedisKeyUntil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        //return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl) {


        int rows=userMapper.updateHeader(userId, headerUrl);
        if(rows>0){
            clearCache(userId);
        }

        return rows;
    }

    public int updatePassword(int userId,String password) {

        return userMapper.updatePassword(userId, password);
    }


    public User findUserByName(String username) {

        return userMapper.selectByName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUntil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUntil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);//生效时间
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUntil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    //权限角色方法
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }



}
