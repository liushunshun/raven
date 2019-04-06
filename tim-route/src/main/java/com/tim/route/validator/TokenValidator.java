package com.tim.route.validator;

import com.tim.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: TokenValidator
 **/
@Component
public class TokenValidator implements Validator {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean validate(String key) {
        String value = (String) redisTemplate.boundHashOps(key).get(key);
        return !StringUtils.isEmpty(value);
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.APP_ERROR_TOKEN_INVALID;
    }
}
