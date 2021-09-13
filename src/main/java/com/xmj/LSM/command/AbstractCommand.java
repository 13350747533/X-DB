package com.xmj.LSM.command;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbstractCommand implements Command{
    /**
     * 命令的类型
     */
    private CommandTypeEnum type;

    public AbstractCommand(CommandTypeEnum type){
        this.type=type;
    }


    public String toStrign(){
        return JSON.toJSONString(this);
    }

    @Override
    public String getKey() {
        return null;
    }
}
