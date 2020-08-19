package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.until.CommunityConstant;
import com.nowcoder.community.until.CommunityUtil;
import com.nowcoder.community.until.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController  implements CommunityConstant {
    @Autowired
    private  DiscussPostService discussPostService;
    @Autowired
    private  UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;



    @RequestMapping(path="/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){

        User user=hostHolder.getUser();
        if(user==null){

            return CommunityUtil.getJSONString(403, "请先登录");

        }
        DiscussPost post=new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setUserId(user.getId());
        post.setCreateTime(new Date());
        discussPostService.insertDiscussPost(post);

        //异常统一处理
        return CommunityUtil.getJSONString(0, "发布成功");

    }

    /*
    @RequestMapping(path = "/detail",method = RequestMethod.GET)
    public String  getDetail(Model model, int id){
    public String  getDetail(Model model, @RequestParam(value="id", required=false) int id){*/


    @RequestMapping(path = "/detail/{id}",method = RequestMethod.GET)
    public String  getDetail(Model model, @PathVariable("id") int id, Page page){

        //处理详情内容
        DiscussPost discussPost=discussPostService.selectDiscussPost(id);
        model.addAttribute("discuss", discussPost);
        User user= userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        //帖子点赞数量
        long tlikeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST, id);
        model.addAttribute("tlikeCount", tlikeCount);
        //点赞状态
        int tlikeStatus= hostHolder.getUser()==null ? 0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, id);
        model.addAttribute("tlikeStatus", tlikeStatus);

        //处理评论
        page.setLimit(5);
        page.setPath("/discuss/detail/"+id);
        page.setRows(discussPost.getCommentCount());


        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, id, page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //帖子点赞数量
                long plikeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("plikeCount", plikeCount);
                //点赞状态
                int plikeStatus= hostHolder.getUser()==null ? 0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("plikeStatus", plikeStatus);



                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        //帖子点赞数量
                        long rlikeCount=likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("rlikeCount", rlikeCount);

                        //点赞状态
                        int rlikeStatus= hostHolder.getUser()==null ? 0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("rlikeStatus", rlikeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentsByEntityCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }







}
