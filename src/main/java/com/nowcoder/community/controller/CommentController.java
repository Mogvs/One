package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.until.CommunityConstant;
import com.nowcoder.community.until.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path="/add/{postId}",method = RequestMethod.POST)
    private String addComment(@PathVariable("postId") int postId, Comment comment){

        User user=hostHolder.getUser();
        if(user==null){

            return "/site/login";

        }
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        //评论完成 系统发送通知 异步会有延迟 不影响其他业务的执行
        Event event=new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", postId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.selectDiscussPost(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event); //加入消息队列

        event=new Event()//搜索模块使用 更改了评论数量需要更改es中存的值
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(postId);
        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/"+postId;
    }

}
