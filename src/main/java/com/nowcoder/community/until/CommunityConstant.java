package com.nowcoder.community.until;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 评论的类型：实体类型: 帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 评论的类型：实体类型: 评论 //用于评论评论的 也就是回复
     */
    int ENTITY_TYPE_COMMENT = 2;


}
