package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.until.CommunityConstant;
import com.nowcoder.community.until.CommunityUtil;
import com.nowcoder.community.until.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller

public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int discussId){
       User user=hostHolder.getUser();
       likeService.like(user.getId(), entityType, entityId,entityUserId);


       long likeCount=likeService.findEntityLikeCount(entityType, entityId);
       int likeStatus=likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String,Object> map=new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //点赞发送消息
        if(likeStatus==1){//点赞才会发布消息
            Event event=new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("discussId", discussId);//用于在点赞列表跳转到帖子 关于评论部分的点赞 最终都是属于某个帖子的
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
