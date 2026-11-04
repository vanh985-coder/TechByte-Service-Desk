package com.techbyte.servicedesk.dto;

import java.util.List;

public class PagedResponse<T> {

    private List<T> items;
    private int page;
    private int limit;
    private long total;
    private int totalPages;

    public PagedResponse() {
    }

    public PagedResponse(List<T> items, int page, int limit, long total, int totalPages) {
        this.items = items;
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

