package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.PointRule;
import com.school.mapper.PointRuleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointRuleService extends ServiceImpl<PointRuleMapper, PointRule> {

    public List<PointRule> listEnabled() {
        return this.lambdaQuery()
                .eq(PointRule::getIsEnabled, 1)
                .list();
    }

    public PointRule getByCode(String ruleCode) {
        return this.lambdaQuery()
                .eq(PointRule::getRuleCode, ruleCode)
                .one();
    }
}
