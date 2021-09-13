package com.xmj.LSM.Service;

import java.io.Closeable;

/**
 * KV存储接口定义
 */
public interface KVService extends Closeable {

    /**
     * 保存数据
     * @param key
     * @param value
     */
    void set(String key,String value);

    /**
     * 查询数据
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 删除数据
     * @param key
     */
    void rm(String key);

}
