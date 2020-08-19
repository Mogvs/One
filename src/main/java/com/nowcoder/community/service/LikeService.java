package com.nowcoder.community.service;

import com.nowcoder.community.until.RedisKeyUntil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId, int entityType, int entityId,int belikeUserId) {//belikeUserId 被赞人的id

      /*  //获取数组中的key
        String entityLikeKey = RedisKeyUntil.getEntityLikeKey(entityType, entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);//指定元素是否在集合中
        if (isMember) {
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }*/

      redisTemplate.execute(new SessionCallback() {
          @Override
          public Object execute(RedisOperations redisOperations) throws DataAccessException {
              String entityLikeKey = RedisKeyUntil.getEntityLikeKey(entityType, entityId);
              String userLikekey=RedisKeyUntil.getUserEntityLike(userId);

              boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);//指定元素是否在集合中

              redisOperations.multi();//开启redis事物
              if (isMember) {
                  redisTemplate.opsForSet().remove(entityLikeKey, userId);
                  redisTemplate.opsForValue().decrement(userLikekey);
              } else {
                  redisTemplate.opsForSet().add(entityLikeKey, userId);
                  redisTemplate.opsForValue().increment(userLikekey);
              }


              return redisOperations.exec();
          }
      });

    }

    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUntil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);//获取实体点赞数量

    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUntil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
    //查找耨个用户获得的赞-受其他点赞的影响 所以需要事物控制
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUntil.getUserEntityLike(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;

    }


}
