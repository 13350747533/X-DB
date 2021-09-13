package com.xmj.LSM.Service;

import com.xmj.LSM.command.Command;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * kvDB的实现
 */
public class KVServiceImpl implements KVService{
    public static final String TABLE = ".table";
    public static final String WAL = "wal";
    public static final String RW_MODE = "rw";
    public static final String WAL_TMP = "walTmp";


    /**
     * 内存表
     */
    private TreeMap<String, Command> index;

    /**
     * 不可变内存表，用于持久化内存表中时暂存数据
     */
    private TreeMap<String, Command> immutableIndex;

    /**
     * ssTable列表
     */
//    private final LinkedList<SSTable> ssTables;
//
//    /**
//     * 数据目录
//     */
//    private final String dataDir;
//
//    /**
//     * 读写锁
//     */
//    private final ReadWriteLock indexLock;
//
//    /**
//     * 持久化阈值
//     */
//    private final int storeThreshold;
//
//    /**
//     * 数据分区大小
//     */
//    private final int partSize;

    /**
     * 暂存数据的日志句柄
     */
    private RandomAccessFile wal;

    /**
     * 暂存数据日志文件
     */
    private File walFile;


    @Override
    public void set(String key, String value) {

    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void rm(String key) {

    }

    @Override
    public void close() throws IOException {

    }
}
