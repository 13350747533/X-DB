package com.xmj.LSM.SStable;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xmj.LSM.Position;
import com.xmj.LSM.command.Command;
import com.xmj.LSM.command.RmCommand;
import com.xmj.LSM.command.SetCommand;
import com.xmj.LSM.utils.ConvertUtil;
import com.xmj.LSM.utils.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeMap;

public class SStable implements Closeable{

    private static final String RW = "rw";

    private final Logger LOGGER= LoggerFactory.getLogger(SStable.class);

    /**
     * 表索引信息
     */
    private TableMateInfo tableMateInfo;
    /**
     * 字段的稀疏索引
     */
    private TreeMap<String, Position> spareIndex;

    /**
     * 文件句柄
     */
    private RandomAccessFile tableFile;

    /**
     * 文件路径
     */
    private final String filePath;

    public SStable(String filePath,int partSize) {
        this.tableMateInfo=new TableMateInfo();
        this.tableMateInfo.setPartSize(partSize);
        this.filePath=filePath;
        try{
            this.tableFile=new RandomAccessFile(filePath,RW);
        }catch(Exception e){
            e.printStackTrace();
        }
        spareIndex=new TreeMap<>();
    }

    /**
     * 从内存表中构建sstable
     * @param filePath
     * @param PartSize
     * @param index
     * @return
     */
    public static SStable createFromIndex(String filePath,int PartSize,TreeMap<String,Command> index){
        SStable sStable = new SStable(filePath,PartSize);
        sStable.initFromIndex(index);
        return sStable;
    }

    /**
     * 从文件中构建SStable
     * @param filePath
     * @return
     */

    public static SStable createFromFile(String filePath){
        SStable sStable =new SStable(filePath,0);
        sStable.restoreFromFile();
        return sStable;
    }


    /**
     * 查询数据
     * @param key
     * @return
     */

    public Command query(String key){
        try{
            LinkedList<Position> spareKeyPositionList = new LinkedList<>();
            //最后一个小于key的位置
            Position lastSmallPosition = null;
            //第一个大于key的位置
            Position firstBigPosition = null;

            for(String k :spareIndex.keySet()){
                if(k.compareTo(key)<=0){
                    lastSmallPosition = spareIndex.get(k);
                }else{
                    firstBigPosition=spareIndex.get(k);
                    break;
                }
            }
            if(lastSmallPosition!=null){
                spareKeyPositionList.add(lastSmallPosition);
            }
            if(firstBigPosition!=null){
                spareKeyPositionList.add(firstBigPosition);
            }
            if(spareKeyPositionList.size()==0){
                return null;
            }
            LoggerUtil.info(LOGGER, "[SStable][restoreFromFiel][spareKeyPositionList]: {}", spareKeyPositionList);
            Position firstKeyPosition=spareKeyPositionList.getFirst();
            Position lastKeyPosition=spareKeyPositionList.getLast();
            long start=firstKeyPosition.getStart();
            long len;
            //是同一个
            if(firstKeyPosition.equals(lastKeyPosition)){
                len=firstKeyPosition.getLen();
            }else{
                len=firstKeyPosition.getLen()+lastKeyPosition.getLen()-start;
            }

            //如果key存在则必定在区间内，则分区间读取
            byte[] dataPart = new byte[(int) len];
            tableFile.seek(start);
            tableFile.read(dataPart);
            int Pstart=0;
            for(Position position : spareKeyPositionList){
                //这个方法???
                JSONObject dataPartJson = JSONObject.parseObject(new String(dataPart,Pstart,(int) position.getLen()));
                LoggerUtil.info(LOGGER, "[SStable][restoreFromFiel][dataPartJson]: {}", dataPartJson);
                //这个处理是否合理？
                if(dataPartJson.containsKey(key)){
                    JSONObject value=dataPartJson.getJSONObject(key);
                    return ConvertUtil.jsonToCommand(value);
                }
                Pstart+=(int) position.getLen();
            }
            return null;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文件中恢复SStable到内存
     */

    private void restoreFromFile(){
        try{
            //读取当前的索引信息
            TableMateInfo tableMateInfo=TableMateInfo.readFromFile(tableFile);
            LoggerUtil.info(LOGGER, "[SStable][restoreFromFiel][tableMateInfo]: {}", tableMateInfo);
            //按照索引来读取文件
            byte[] indexBytes = new byte[(int) tableMateInfo.getIndexLen()];
            tableFile.seek(tableMateInfo.getIndexStart());
            tableFile.read(indexBytes);
            String indexStr = new String(indexBytes,StandardCharsets.UTF_8);
            LoggerUtil.info(LOGGER, "[SStable][restoreFromFiel][indexStr]: {}", indexStr);
            spareIndex=JSONObject.parseObject(indexStr,
                    new TypeReference<TreeMap<String,Position>>(){
                    });
            //这里更新了索引信息，有什么用？
            this.tableMateInfo=tableMateInfo;
            LoggerUtil.info(LOGGER, "[SStable][restoreFromFiel][spareIndex]: {}", spareIndex);
        }catch(Exception e){
            e.printStackTrace();
        }
    }






    /**
     * 从内存表转换为SStable
     * @param index
     */

    private void initFromIndex(TreeMap<String, Command>index){
        try{
            JSONObject partData = new JSONObject(true);
            tableMateInfo.setIndexStart(tableFile.getFilePointer());
            for(Command command : index.values()) {
                //如果是set命令
                if(command instanceof SetCommand){
                    SetCommand set=(SetCommand) command;
                    partData.put(set.getKey(),set);
                }
                //如果是rm命令
                if(command instanceof RmCommand){
                    RmCommand rm=(RmCommand) command;
                    partData.put(rm.getKey(),rm);
                }

                //数据分段存储，便于稀疏索引查找，达到分段数量。开始写入数据段
                if(partData.size()>=tableMateInfo.getPartSize()){
                    writeDataPart(partData);
                }
            }
            if(partData.size()>0){
                writeDataPart(partData);
            }

            long datePartLen=tableFile.getFilePointer()- tableMateInfo.getDateStart();
            tableMateInfo.setDateLen(datePartLen);
            //保存稀疏索引


            //这样设计的话索引和数据似乎是间隔的？
            byte[] indexBytes = JSONObject.toJSONString(spareIndex).getBytes(StandardCharsets.UTF_8);
            tableMateInfo.setIndexStart(tableFile.getFilePointer());
            tableFile.write(indexBytes);
            tableMateInfo.setIndexLen(indexBytes.length);
            LoggerUtil.debug(LOGGER, "[SsTable][initFromIndex][sparseIndex]: {}", spareIndex);

            //保存文件索引
            //保存位置信息，看来是每写一次都会更新
            tableMateInfo.writeToFile(tableFile);
            LoggerUtil.info(LOGGER, "[SsTable][initFromIndex]: {},{}", filePath, tableMateInfo);


        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }


    /**
     * 将分区写入文件
     * @param partData
     * @throws IOException
     */

    private void writeDataPart(JSONObject partData) throws IOException {
        byte[] partDateBytes=partData.toString().getBytes(StandardCharsets.UTF_8);
        Long start=tableFile.getFilePointer();
        tableFile.write(partDateBytes);

        //记录该数据段的第一个key到稀疏索引中
        Optional<String> firstKey=partData.keySet().stream().findFirst();
        //避免空指针，实例存在就放入稀疏索引中
        firstKey.ifPresent(s -> spareIndex.put(s,new Position(start,partDateBytes.length)));
        partData.clear();
    }

    @Override
    public void close() throws IOException {

    }
}
