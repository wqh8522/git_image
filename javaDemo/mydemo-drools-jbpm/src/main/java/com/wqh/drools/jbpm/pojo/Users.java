package com.wqh.drools.jbpm.pojo;

public class Users {

	private String name;
	
	private String age;
	
	private String sex;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	@Override
	public String toString() {
		return "Users{" +
				"name='" + name + '\'' +
				", age='" + age + '\'' +
				", sex='" + sex + '\'' +
				'}';
	}
}
