package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.until.CommunityConstant;
import com.nowcoder.community.until.CommunityUtil;
import com.nowcoder.community.until.CookieUtil;
import com.nowcoder.community.until.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger= LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @Value("${server.servlet.context-path}")
    private String contextpath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;//获取当前登录的user信息

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;


    @LoginRequired
    @RequestMapping(path="upload",method = RequestMethod.POST)
    public String UploadHreader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

         String fileName= headerImage.getOriginalFilename();//原始文件名
         String suffix=fileName.substring(fileName.lastIndexOf("."));
        // if(!StringUtils.isBlank(suffix)){//判断获取的后缀是否为空
             if(!suffix.equalsIgnoreCase(".GIF")&&!suffix.equalsIgnoreCase(".PNG")
                     &&!suffix.equalsIgnoreCase(".JPG")){
                 model.addAttribute("error", "文件格式不正确,只限png、jpg、git格式图片");
                 return "/site/setting";
             }
        // }
         //生成新的文件名
         fileName= CommunityUtil.generateUUID()+suffix;

        File file = new File(uploadPath); //判断存放路径是否存在，不存在则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        //确定文件存放的路径
        File dest=new File(uploadPath+'/'+fileName);

        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("文件上传失败！"+  e.getMessage());
            throw new RuntimeException("文件上传失败，服务器发生异常！"+e);
        }
        //上传成功之后 更新头像的路径
        User user=hostHolder.getUser();
        // http://localhost:8080/community/user/header/xxx.png


        String headerUrl=domain+contextpath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }
    @RequestMapping(path ="/header/{fileName}" ,method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //找到服务器的问价路径
        fileName=uploadPath+"/"+fileName;
        //解析文件后缀
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //相应图片
        response.setContentType("image/"+suffix);
        try (OutputStream os=response.getOutputStream();//springmvc 自动关流
             FileInputStream fis=new FileInputStream(fileName);)//java7 语法  自动注入finally
        {
            byte[] buffer=new byte[1024];//缓冲区
            int b=0;
            while ((b = fis.read(buffer)) != -1) {//读取
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
           logger.error("读取文件失败"+e.getMessage());
        }
    }

    @RequestMapping(path ="/updatepwd" ,method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassWord, String newPassWord, HttpServletRequest request){

        if(StringUtils.isBlank(oldPassWord)){
            logger.error("原密码为空");
            model.addAttribute("psderror", "原密码不能为空");
            return "/site/setting";

        }
        User user=hostHolder.getUser();
        if(!CommunityUtil.md5(oldPassWord+user.getSalt()).equals(user.getPassword())){//核对输入密码与原密码是否相同
            logger.error("原密码错误");
            model.addAttribute("olderror", "原密码错误");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassWord)){
            logger.error("新密码为空");
            model.addAttribute("newerror", "新密码不能为空");
            return "/site/setting";
        }


        if(CommunityUtil.md5(newPassWord+user.getSalt()).equals(user.getPassword())){//核对新密码与原密码是否相同
            logger.error("新密码不能与原密码相同");
            model.addAttribute("newerror", "新密码不能与原密码相同");
            return "/site/setting";
        }
         newPassWord=CommunityUtil.md5(newPassWord+user.getSalt());
         int flag=userService.updatePassword(user.getId(), newPassWord);


        String ticket = CookieUtil.getValue(request, "ticket");
        userService.logout(ticket);//更新账号标记状态
        return "redirect:/login";


    }
    //个人主页
    @RequestMapping(path="/profile/{userId}",method = RequestMethod.GET)
    public String profil(@PathVariable("userId") int userId,Model model){

        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

         int    likeCount=likeService.findUserLikeCount(user.getId());

         model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = 0;
        followeeCount= followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount =0;
        followerCount=followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);



        return "/site/profile";
    }





}
