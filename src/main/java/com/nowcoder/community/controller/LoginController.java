package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.until.CommunityConstant;
import com.nowcoder.community.until.CommunityUtil;
import com.nowcoder.community.until.RedisKeyUntil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public  String getRegisterPage(){
        return "/site/register";

    }
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public  String getLoginPage(){
        return "/site/login";
    }

    @Autowired
    UserService userService;
    @RequestMapping(value="/register",method = RequestMethod.POST)
    public String register(Model model,User user){
        Map<String,Object> map=userService.register(user);

        if(map==null|| map.isEmpty()){

            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }
        else{
           model.addAttribute("usernamemsg", map.get("usernamemsg"));
           model.addAttribute("passwordmsg", map.get("passwordmsg"));
           model.addAttribute("emailmsg", map.get("emailmsg"));
           return "/site/register";
        }

    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userid}/{code}")
    public String activation(Model model, @PathVariable("userid") int userid,@PathVariable("code") String  code){
      int result= userService.activation(userid, code);
      if(result==ACTIVATION_SUCCESS){//激活成功
          model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
          model.addAttribute("target", "/login");//跳转路径 是requestMapping的path
      }
      else if (result==ACTIVATION_REPEAT){//重复激活
          model.addAttribute("msg", "无效操作，您的账号已经激活！");
          model.addAttribute("target", "/index");

      }
      else {//激活失败

          model.addAttribute("msg", "激活失败，您的提供的激活码不正确！");
          model.addAttribute("target", "/index");
      }

        return "/site/operate-result";


    }

    @Autowired
    private Producer kaptchaProducer;//生成验证码

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        // 将验证码存入session
      //  session.setAttribute("kaptcha", text);没用redis之前的

        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie=new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUntil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);






        // 将突图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }


    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(Model model, String username,String password,
                        String code,boolean rememberme,
                        /*HttpSession session,*/HttpServletResponse response ,@CookieValue("kaptchaOwner") String  kaptchaOwner){
       // Map<String,Object> map=new HashMap<>();
        //检查验证码
       // String kaptcha= (String) session.getAttribute("kaptcha");

        /*redis*/
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUntil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        /*redisjieshu*/


        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codemsg", "验证码不正确");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
       Map<String,Object>map=userService.login(username, password, expiredSeconds);//服务层中的处理结果集合
       if(map.containsKey("ticket")){
           Cookie cookie=new Cookie("ticket", map.get("ticket").toString());
           cookie.setPath(contextPath);//有效的路徑 网站的任何位置都有已登录的信息
           cookie.setMaxAge(expiredSeconds);
           response.addCookie(cookie);
           return "redirect:/index";

       }
       else{
           model.addAttribute("usernamemsg", map.get("usernamemsg"));
           model.addAttribute("passwordmsg", map.get("passwordmsg"));
           return "/site/login";
       }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);//更新账号标记状态
        return "redirect:/login";
    }




}
