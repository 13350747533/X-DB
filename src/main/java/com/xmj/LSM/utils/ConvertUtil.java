package com.xmj.LSM.utils;

import com.alibaba.fastjson.JSONObject;
import com.xmj.LSM.command.Command;
import com.xmj.LSM.command.CommandTypeEnum;
import com.xmj.LSM.command.RmCommand;
import com.xmj.LSM.command.SetCommand;

public class ConvertUtil {

    /**
     * JSON转Command对象
     */

    private static final String TYPE = "type";

    public static Command jsonToCommand(JSONObject value){
        if(value.getString(TYPE).equals(CommandTypeEnum.SET.name())){
            return value.toJavaObject(SetCommand.class);
        }else if(value.getString(TYPE).equals(CommandTypeEnum.RM.name())){
            return value.toJavaObject(RmCommand.class);
        }

        return null;
    }

}
