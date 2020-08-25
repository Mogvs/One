package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository //springboot访问数据库的注解
//@Component
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {


}
