package com.raven.route.user.controller;

import static com.raven.common.utils.Constants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.raven.common.result.Result;
import com.raven.route.user.bean.param.GetAccessParam;
import com.raven.route.user.bean.param.GetTokenParam;
import com.raven.route.user.service.UserService;
import com.raven.route.utils.ClientType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author zxx Description 用户接口 Date Created on 2018/6/12
 */
@RestController
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户登录合法token
     */

    @GetMapping("/token")
    public Result getToken(@RequestHeader(AUTH_APP_KEY) String appKey,
        @RequestParam("uid") String uid) {
        log.info("getToken, app key {}, uid {}", appKey, uid);
        return userService.getToken(uid, appKey);
    }

    @PostMapping("/token")
    public Result getToken(@RequestBody GetTokenParam param) {
        log.info("getToken, app key {}, uid {}", param.getAppKey(), param.getUid());
        return userService.getToken(param.getUid(), param.getAppKey());
    }

    /**
     * 获取用户登录接入点
     * 只有web用，它会拿到 websocket 端口
     */
    @GetMapping("/access")
    public Result getAccessInfo(@RequestHeader(AUTH_APP_KEY) String appKey,
        @RequestHeader(AUTH_TOKEN) String token) {
        log.info("getToken, app key {}, token {}", appKey, token);
        return userService.getAccessInfo(appKey, token, ClientType.WEB);
    }

    /**
     * 获取用户登录接入点
     * mobile 或其他在用，它会拿到 tcp 端口
     */
    @PostMapping("/access")
    public Result getAccessInfo(@RequestBody GetAccessParam param) {
        log.info("getToken, app key {}, token {}", param.getAppKey(), param.getToken());
        return userService.getAccessInfo(param.getAppKey(), param.getToken(), ClientType.MOBILE);
    }
}
