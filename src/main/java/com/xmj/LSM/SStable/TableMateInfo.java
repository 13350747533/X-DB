package com.xmj.LSM.SStable;

import lombok.Data;

import java.io.RandomAccessFile;

/**
 * SStable的分区信息,其各个参数随着数据的扩充是在不断变化的
 */


@Data
public class TableMateInfo {

    /**
     * 版本号
     */
    private long version;

    /**
     * 数据区开始位置
     */
    private long dateStart;

    /**
     * 数据区长度
     */
    private long dateLen;

    /**
     * 索引区开始位置
     */
    private long indexStart;

    /**
     * 索引区长度
     */
    private long indexLen;

    /**
     * 分段大小
     */
    private long partSize;

    /**
     * 写入文件
     * @param file
     */
    public void writeToFile(RandomAccessFile file){
        try{
            file.writeLong(partSize);
            file.writeLong(dateStart);
            file.writeLong(dateLen);
            file.writeLong(indexStart);
            file.writeLong(indexLen);
            file.writeLong(version);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 从内存中读取一个tableMateInfo
     * @param file
     * @return
     */
    public static TableMateInfo readFromFile(RandomAccessFile file){
        TableMateInfo tableMateInfo=new TableMateInfo();
        try{
            Long fileLen=file.length();

            file.seek(fileLen-8);
            tableMateInfo.setVersion(file.readLong());

            file.seek((fileLen-16));
            tableMateInfo.setIndexLen(file.readLong());

            file.seek(fileLen-24);
            tableMateInfo.setIndexStart(file.readLong());

            file.seek(fileLen-32);
            tableMateInfo.setDateLen(file.readLong());

            file.seek(fileLen-40);
            tableMateInfo.setDateStart(file.readLong());

            file.seek(fileLen-48);
            tableMateInfo.setPartSize(file.readLong());

            return tableMateInfo;

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }


}
