package com.wqh.drools.jbpm.pojo;

public class Credit {
    private String uuid;

    private String result;

    public Credit(String uuid, String result) {
        this.uuid = uuid;
        this.result = result;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Credit{" +
                "uuid='" + uuid + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
