package com.xmj.LSM.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RmCommand extends AbstractCommand{

    /**
     * 删除命令
     */
    private String key;

    public RmCommand(String key) {
        super(CommandTypeEnum.RM);
        this.key=key;
    }
}
