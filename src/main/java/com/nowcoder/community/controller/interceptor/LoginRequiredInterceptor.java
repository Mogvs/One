package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.until.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;
    //配合自定义的注解 对指定内容进行拦截，在进控制器之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //handler 被拦截的目标对象 包括资源方法等
        if (handler instanceof HandlerMethod) {//只针对方法进行拦截
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);//人为干预只处理带有注解的那一部分
            //获取此自定义注解为不为空
            if (loginRequired != null && hostHolder.getUser() == null) {//加了此注解的方法需要一些条件才能访问
                response.sendRedirect(request.getContextPath() + "/login");//重定向到指定页面 可以单配，也可以注入其他对象
                return false;
            }
        }//满足条件后放行 访问目标方法
        return true;
    }
}
