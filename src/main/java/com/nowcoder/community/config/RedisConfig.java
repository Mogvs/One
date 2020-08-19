package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration //配置标识
public class RedisConfig{
    @Bean //配置第三方组件
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        //链接工厂注入进模板 具备链接数据库的功能 //RedisConnectionFactory factory
        RedisTemplate<String,Object> template=new RedisTemplate<>();//实例化bean
        template.setConnectionFactory(factory);//将连接工厂设置给模板实例 让模板有操作数据库的能力

        //设置key序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置哈希key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置哈希value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        return template;


    }




}




/*@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }

}*/
