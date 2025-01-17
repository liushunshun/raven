package com.raven.common.utils;

/**
 * Author zxx Description 常量 Date Created on 2018/6/4
 */
public class Constants {

    /**
     * redis key
     */
    public static final String USER_TOKEN = "token_";

    public static final String USER_ROUTE_KEY = "user_route_key";

    public static final String GATEWAY_SERVER_ROUTE_KEY = "gateway_server_route_key_";

    public static final String PREFIX_CONVERSATION_ID = "converid_";

    public static final String PREFIX_CONVERSATION_LIST = "converlist_";

    public static final String PREFIX_MESSAGE_ID = "msg_";

    public static final String PREFIX_GROUP_MEMBER = "group_member_";

    public static final String PREFIX_USER_CID = "user_cid_";

    public static final String PREFIX_WAIT_USER_ACK_MID = "wait_user_ack_mid_";
    /*
     * Authentication.
     * */
    public static final String AUTH_APP_KEY = "AppKey";

    public static final String AUTH_NONCE = "Nonce";

    public static final String AUTH_TIMESTAMP = "Timestamp";

    public static final String AUTH_SIGNATURE = "Sign";

    public static final String AUTH_TOKEN = "Token";

    public static final long TOKEN_CACHE_DURATION = 1; // 7 days

    /**
     * Default cipher algorithm
     */
    public static final String DEFAULT_CIPHER_ALGORITHM = "DES";

    /**
     * Default separate sign.
     */
    public static final String DEFAULT_SEPARATES_SIGN = ":";

    /**
     * Global config.
     */
    public static final String CONFIG_TCP_PORT = "tcp-port";

    public static final String CONFIG_WEBSOCKET_PORT = "websocket-port";

    public static final String CONFIG_INTERNAL_PORT = "internal-port";

    public static final String CONFIG_GATEWAY_SERVER_NAME = "raven-gateway";

    // kafka topic
    public static final String KAFKA_TOPIC_SINGLE_MSG= "singleMsg";

    public static final String KAFKA_TOPIC_GROUP_MSG= "groupMsg";

}
