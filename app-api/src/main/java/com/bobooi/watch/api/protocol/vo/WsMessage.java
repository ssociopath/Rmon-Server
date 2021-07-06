package com.bobooi.watch.api.protocol.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bobo
 * @date 2021/6/29
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WsMessage {
    private String type;
    private String from;
    private String to;
    private String content;
}
