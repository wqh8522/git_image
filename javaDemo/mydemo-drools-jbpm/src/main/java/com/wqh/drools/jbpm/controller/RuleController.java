package com.wqh.drools.jbpm.controller;


import com.wqh.drools.jbpm.pojo.Credit;
import com.wqh.drools.jbpm.pojo.Users;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sound.midi.Soundbank;

@RestController
@RequestMapping("rule")
public class RuleController {

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);

    @Autowired
    @Qualifier("droolsKieBase")
    private KieBase droolsKieBase;

    @GetMapping("/test/{name}")
    public Credit testProcess(Users users){
//        long start = System.currentTimeMillis();
        KieSession kieSession = droolsKieBase.newKieSession();
        kieSession.insert(users);
        int i = kieSession.fireAllRules();
//        logger.info("触发规则数量:{}",i);
        QueryResults results = kieSession.getQueryResults("list all credits from working memory");
        Credit credit = null;
        for(QueryResultsRow row : results){
            if(row.get("credit") instanceof  Credit){
                credit = (Credit) row.get("credit");
//                logger.info("规则执行完成，，Credit:{}",credit.toString());
            }
        }
        kieSession.dispose();
//        long end = System.currentTimeMillis();
//        logger.info("规则执行时间：{}毫秒",end-start);
        return  credit;
    }
}
