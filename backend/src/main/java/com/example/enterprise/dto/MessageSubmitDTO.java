package com.example.enterprise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 留言提交数据传输对象
 * <p>用于前端用户提交咨询留言时的请求参数封装</p>
 */
@Data
public class MessageSubmitDTO {
    /** 留言人姓名，最长80个字符 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 80, message = "姓名不能超过80个字符")
    private String username;

    /** 留言人电话，最长50个字符 */
    @Size(max = 50, message = "电话不能超过50个字符")
    private String phone;

    /** 留言人邮箱，需符合邮箱格式，最长120个字符 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 120, message = "邮箱不能超过120个字符")
    private String email;

    /** 留言内容，最长2000个字符 */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 2000, message = "留言内容不能超过2000个字符")
    private String content;
}
