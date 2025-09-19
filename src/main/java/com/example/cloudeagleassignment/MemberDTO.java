package com.example.cloudeagleassignment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private String accountId;
    private String email;
    private String displayName;

    public MemberDTO(String accountId, String email, String displayName) {
        this.accountId = accountId;
        this.email = email;
        this.displayName = displayName;
    }
}