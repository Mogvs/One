package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.until.CommunityConstant;
import com.nowcoder.community.until.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    //获取评论列表
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }
    //获取评论总数用于分页
    public  int findCommentsByEntityCount(int entityType, int entityId)
    {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }


    //新增评论
    //添加全局事物注解：合适的隔离级别
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int insertComment(Comment comment){

        if(comment==null){

            throw new IllegalArgumentException("参数不能为空");

        }
        System.out.println("获取实体："+comment);

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));//过滤特殊标签
        comment.setContent(sensitiveFilter.filter(comment.getContent()));//过滤屏蔽字符

        int rows= commentMapper.insertCommentEntity(comment);
        //更新帖子评论数量

        if(comment.getEntityType()==ENTITY_TYPE_POST){//确定评论的内容是帖子才进行更新

            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            //getEntityType 根据评论的类型不同 添加不桶的标记， getEntityId 根据评论的目标 可能是帖子可能是评论 存入对应类型的id
            discussPostService.updateDiscussCount(comment.getEntityId(), count);
        }

        return rows;
    }

}
