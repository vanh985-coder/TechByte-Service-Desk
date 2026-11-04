package com.techbyte.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 1000, message = "Nội dung bình luận không được vượt quá 1000 ký tự")
    private String content;

    public CreateCommentRequest() {
    }

    public CreateCommentRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

