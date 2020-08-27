package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //@Param注解用于给参数取别名  传递多个参数必须由@Param注解指明
    List<DiscussPost> selectDiscussPosts( @Param("userId")int userId,@Param("offset") int offset, @Param("limit") int limit,@Param("orderMode") int orderMode);
    //@Param注解用于给参数取别名，
    //如果只有一个参数 并且在<if>块中使用，必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostRow( int discussId );

    DiscussPost selectDiscussPostById(int id);



    //更新帖子的评论数量
    int updateDiscussCount(@Param("discussId") int discussId,@Param("number") int number);

    int updateType(@Param("id") int id, @Param("type") int type);

    int updateStatus(@Param("id") int id, @Param("status") int status);

    int updateScore(@Param("id") int id, @Param("score") double score);

}
