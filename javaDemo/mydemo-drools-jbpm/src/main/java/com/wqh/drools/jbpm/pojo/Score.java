package com.wqh.drools.jbpm.pojo;

import java.io.Serializable;


public class Score implements Serializable{

	private static final long serialVersionUID = 1L;

	private String name;
	
	private Integer score;
	
	private String desc;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "Score [name=" + name + ", score=" + score + ", desc=" + desc + "]";
	}

	
}
