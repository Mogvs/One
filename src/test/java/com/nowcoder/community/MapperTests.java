package com.nowcoder.community;


import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper ;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public  void  testSelectUser(){

        User user=userMapper.selectById(101);
        System.out.println(user);

        user=userMapper.selectByName("liubei");
        System.out.println(user);

    }
    @Test
    public void  testSelectPosts() {

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost post : list) {
            System.out.println(
                    post
            );
        }
        int count=discussPostMapper.selectDiscussPostRows(149);
        System.out.println("共："+count+"条");

    }
    @Autowired
    private DiscussPostService.LoginTicketMapper loginTicketMapper;
    LoginTicket loginTicket=new LoginTicket();
    @Test
    public void  testaddTicket(){

        loginTicket.setUserId(1);
        loginTicket.setStatus(1);
        loginTicket.setExpired(new Date());
        loginTicket.setTicket("code");

       int id= loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println("生成id："+id);
    }
    @Test
    public void  testselectTicket(){
        loginTicket=loginTicketMapper.selectByTicket("code");
        System.out.println("获取实体为："+loginTicket);


    }
    @Test
    public void testupdateTicket() {
        int state = loginTicketMapper.updateStatus("code",1);
        System.out.println("更新结果：" + state);
    }

//    public void testupdate(@Param("status") int status,@Param("ticket") String ticket){
//        int state=loginTicketMapper.updateStatus("code", 0);
//        System.out.println("更新结果："+state);
//    }


}
