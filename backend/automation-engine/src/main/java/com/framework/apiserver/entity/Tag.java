package com.framework.apiserver.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    private String name;

    public Tag() {}
    public Tag(String name) { this.name = name; }

}
