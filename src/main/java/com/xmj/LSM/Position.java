package com.xmj.LSM;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor

public class Position {

    /**
     * 开始
     */
    private Long start;

    /**
     * 长度
     */
    private long len;
}
