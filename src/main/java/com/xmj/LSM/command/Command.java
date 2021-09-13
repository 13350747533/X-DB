package com.xmj.LSM.command;

/**
 * 存储于SStable的
 */
public interface Command {

    /**
     * 获取数据
     * @return
     */
    String getKey();
}
