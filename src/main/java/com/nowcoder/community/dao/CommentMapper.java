package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    //获取指定类型的评论集合
    List<Comment> selectCommentByEntity(@Param("entityType") int entityType,@Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);
    //获取指定类型的评论总数
    int selectCountByEntity(@Param("entityType") int entityType,@Param("entityId")  int entityId);

    //增加评论内容
    int insertCommentEntity(Comment comment);





}
