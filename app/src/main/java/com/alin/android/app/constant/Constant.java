package com.alin.android.app.constant;

/**
 * 通用常量
 */
public interface Constant {
    String ENV_PROPERTIES = "env.properties";

    String KEY_API_URL = "api.url";
    String KEY_CHAT_API_URL = "chat.api.url";
    String KEY_CHAT_WS_URL = "chat.ws.url";
    String KEY_CHAT_USER_FROM = "chat.user.from";
    String KEY_CHAT_USER_TO = "chat.user.to";
    String KEY_BROWSER_URL = "browser.url";
    String KEY_APP_VERSION_CHECK = "app.version-check";

    String XML_CHAT_LOGIN_USER = "chat_login_user.xml";
    String XML_CHAT_USER = "chat_user.xml";
    String XML_CHAT_BOOK = "chat_book.xml";
    String XML_CHAT_MESSAGE_PREFIX = "chat_message%s%s";
    String XML_SUFFIX = ".xml";

    String DEFAULT_URL = "http://localhost";
    String BROWSER_DEFAULT_URL = "https://www.baidu.com/";

    String STRING_CHAT = "聊天";
    String STRING_CHAT_BOOK = "通讯录";
    String STRING_CHAT_USER = "用户";
    String STRING_CHAT_LOADING = "收取中……";
    String STRING_CHAT_ERROR = "未连接";
}
