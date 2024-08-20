package com.nowcoder.community.util;

public interface CommunityConstant {

    int ACTIVATION_SUCCESS = 0; //激活成功

    int ACTIVATION_REPEAT = 1; //重复激活

    int ACTIVATION_FAILURE = 2; //激活失败

    int DEFAULT_EXPIRED_SECONDS = 3600*12; //默认状态的登录凭证的超时时间

    int REMEMBER_EXPIRED_SECONDS = 3600*24*30; //“记住我”状态的登录凭证的超时时间


}
