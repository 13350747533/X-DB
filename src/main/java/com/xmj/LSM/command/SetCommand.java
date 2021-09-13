package com.xmj.LSM.command;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetCommand extends AbstractCommand{
    /**
     * 数据key
     */
    private String key;

    /**
     * 数据value;
     */
    private String value;


    public SetCommand(String key,String value) {
        super(CommandTypeEnum.SET);
        this.key=key;
        this.value=value;

    }


}
