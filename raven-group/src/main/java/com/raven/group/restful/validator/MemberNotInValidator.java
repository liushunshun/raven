package com.raven.group.restful.validator;

import com.raven.common.result.ResultCode;
import com.raven.group.restful.bean.model.GroupMemberModel;
import com.raven.group.restful.mapper.GroupMemberMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: UserValidator
 **/
@Component
public class MemberNotInValidator implements Validator {

    @Autowired
    private GroupMemberMapper memberMapper;

    @Override
    public boolean isValid(String groupId, List<String> members) {
        //members 中有一个成员 不在群组中，就算失败。 members需要是一个净删除列表
        Example example = new Example(GroupMemberModel.class);
        example.createCriteria()
            .andNotEqualTo("status", 2)
            .andEqualTo("groupId", groupId)
            .andIn("memberUid", members);
        List<GroupMemberModel> models = memberMapper.selectByExample(example);
        return models.size() == members.size();
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.GROUP_ERROR_MEMBER_NOT_IN;
    }
}