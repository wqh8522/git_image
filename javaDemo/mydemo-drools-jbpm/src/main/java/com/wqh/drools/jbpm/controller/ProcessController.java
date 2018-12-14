package com.wqh.drools.jbpm.controller;


import com.wqh.drools.jbpm.pojo.Credit;
import com.wqh.drools.jbpm.pojo.Users;
import org.apache.catalina.User;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("process")
public class ProcessController {

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);
    @Autowired
    @Qualifier("jbpmKieBase")
    private KieBase jbpmKieBase;

    @GetMapping("/test/{name}")
    public Credit testProcess(Users users) {
//        logger.info("请求参数：User{}",users);
//        long start = System.currentTimeMillis();
        KieSession kieSession = jbpmKieBase.newKieSession();
        kieSession.insert(users);
        ProcessInstance processInstance = kieSession.startProcess("com.process.TestProcess");
//        logger.info("规则流：{}",processInstance);
        kieSession.fireAllRules();
        QueryResults results = kieSession.getQueryResults("list all credits from working memory");
        Credit credit = null;
        for (QueryResultsRow row : results) {
            credit = (Credit) row.get("credit");
//            logger.info("规则流执行完成，，Credit:{}",credit.toString());
        }
        kieSession.dispose();
//        long end = System.currentTimeMillis();
//        logger.info("规则流执行时间：{}毫秒",end-start);
        return credit;

    }
}
