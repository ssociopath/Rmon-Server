package com.bobooi.watch.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * @author bobo
 * @date 2021/6/27
 */

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Pc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull
    private String mac;
    @NotNull
    private String password;
}
