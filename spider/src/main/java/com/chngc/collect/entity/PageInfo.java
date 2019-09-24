package com.chngc.collect.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 列表页 对象实体
 */

public class PageInfo implements Serializable {
    private static final long serialVersionUID = 3049099390942515318L;

    private List<String> urlList = new ArrayList<>();

    public List<String> getUrlList() {
        return urlList;
    }

    public void addUrlList(String url) {
        this.urlList.add(url);
    }
}
