package com.wqh.demo.drools.filter;

import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

/**
 *  AgendaFilter:实现对规则的控制接口
 * @author wanqh
 */
public class MyAgendaFilter implements AgendaFilter {


    private String ruleName;

    public MyAgendaFilter(String ruleName){
        this.ruleName = ruleName;
    }

    /**
     *
     * @param match 可以获取当前正在执行的对象和属性
     * @return
     */
    @Override
    public boolean accept(Match match) {
        return match.getRule().getName().equals(ruleName) ? true:false;
    }


    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
}
