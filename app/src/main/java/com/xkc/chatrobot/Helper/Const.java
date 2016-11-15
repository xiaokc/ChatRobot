package com.xkc.chatrobot.Helper;

/**
 * Created by xkc on 6/18/16.
 */
public class Const {
    public static final String turing_url = "http://www.tuling123.com/openapi/api";
    public static final String local_server = "http://10.3.200.10:5000";
    public static final String key = "da89ff52e58c5ade5acbcb3c7bb35c8c";
    public static final String tuling_key = "3d89ec012826fbf85e7a018daec73026";
    public static final int connect_timeout = 60000;
    public static final String register_url = local_server + "/register";
    public static final String login_url = local_server +"/login";
    public static final String chat_url = local_server +"/chat";

    public static final String NO_USERID = "没有userid";
    public static final String REGISTER_FAIL = "注册失败";
    public static final String REGISTER_SUCCESS = "注册成功";
    public static final String USER_EXISTED = "用户名已存在";
    public static final String NOT_SAME_PASSWORD = "两次密码不一样，请检查";

    public static final String NULL_PARAMS = "参数为NULL";
    public static final String NETWORK_ERROR = "网络错误";
    public static final String ERROR = "出错了";

    public static final String LOGIN_SUCCESS = "登陆成功";
    public static final String LOGIN_FAIL = "登录失败";
    public static final String USER_NOT_EXISTED = "用户名不存在";


}
