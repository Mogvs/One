package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.until.SensitiveFilter;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
        return  discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }
    public  int selectDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Autowired
    private SensitiveFilter sensitiveFilter;
    public  int insertDiscussPost(DiscussPost post){
        if(post==null)
        {
            throw new IllegalArgumentException("参数不能为空");
        }
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));//HtmlUtils.htmlEscape 过滤特殊标签《script》等
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //输入内容过滤
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));


        return  discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost selectDiscussPost(int diccussId){//获取详情

        return discussPostMapper.selectDiscussPostRow(diccussId);
    }

    public  int updateDiscussCount(int diccussId,int commentCount ){//更新帖子评论数量

        return  discussPostMapper.updateDiscussCount(diccussId, commentCount);

    }






















    @Mapper
    public static interface LoginTicketMapper {

        @Insert({
                "insert  into login_ticket ",
                "(user_id,ticket,status,expired) ",
                "values(#{userId},#{ticket},#{status},#{expired})"
        })
        @Options(useGeneratedKeys = true, keyProperty = "id")
//自动生成主键进行注入
        int insertLoginTicket(LoginTicket loginTicket);

        @Select({
                "select id,user_id,ticket,status,expired ",
                "from login_ticket where ticket=#{ticket}"

        })
        LoginTicket selectByTicket(String ticket);//根据状态码获取登录信息实体  //mapper 注解版动态sql <script> if 转义加闭合


        @Update({
                "<script>",
                "update login_ticket ",
                "set status=#{param2} ",
                "where ticket=#{param1} ",
                "<if test=\"#{param1}!=null\"> ",
                "and 1=1 ",
                "</if>",
                "</script>"

        })
        int updateStatus(String ticket, int status);//注销改状态




    }
}
