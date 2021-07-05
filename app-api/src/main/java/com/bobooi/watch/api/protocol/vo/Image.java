package com.bobooi.watch.api.protocol.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bobo
 * @date 2021/6/30
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    private String account;
    private byte[] content;
}
