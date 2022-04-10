package com.zanygeek.rememberme.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "photo")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String storedName;
    private String uploadName;

    @ManyToOne
    @JoinColumn(name="memorialId")
    private Memorial memorial;

    private String password;
    private Date fromName;
    private int obituaryId;
    private String url;
    private Boolean main;
}