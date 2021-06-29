package com.bobooi.watch.api.protocol.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;

/**
 * @author bobo
 * @date 2021/6/26
 */

@Message
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePacket {
    private byte type;
    private byte result;
    private int id;
    private byte flag;
    private byte[] content;
}
