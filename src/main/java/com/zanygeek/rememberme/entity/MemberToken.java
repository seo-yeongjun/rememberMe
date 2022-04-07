package com.zanygeek.rememberme.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

//회원가입시 메일 확인을 위한 토큰
@Getter
@Setter
@Entity
@Table(name = "memberToken")
public class MemberToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String confirmToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    private String userId;

    public MemberToken(String userId) {
        this.userId = userId;
        createdDate = new Date();
        confirmToken = UUID.randomUUID().toString();
    }

    public MemberToken() {
    }
}