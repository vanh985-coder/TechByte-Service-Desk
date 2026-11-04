package com.techbyte.servicedesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DashboardSummaryResponse {

    private long total;
    private long open;

    @JsonProperty("in_progress")
    private long inProgress;

    private long resolved;
    private long closed;
    private long completed;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long total, long open, long inProgress, long resolved, long closed) {
        this.total = total;
        this.open = open;
        this.inProgress = inProgress;
        this.resolved = resolved;
        this.closed = closed;
        this.completed = resolved + closed;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getOpen() {
        return open;
    }

    public void setOpen(long open) {
        this.open = open;
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = inProgress;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }

    public long getClosed() {
        return closed;
    }

    public void setClosed(long closed) {
        this.closed = closed;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }
}

