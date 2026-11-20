package com.cms.electricityproject.domain;

/**
 * 能耗树节点更新请求的数据结构。
 */
public class TreeNodeUpdateRequest {
    private String id;
    private String name;
    private String extraInfo;
    private String pds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getPds() {
        return pds;
    }

    public void setPds(String pds) {
        this.pds = pds;
    }
}
