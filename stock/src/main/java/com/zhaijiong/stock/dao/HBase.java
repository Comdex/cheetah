package com.zhaijiong.stock.dao;

import com.google.common.collect.Lists;
import com.zhaijiong.stock.Context;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * author: xuqi.xq
 * mail: xuqi.xq@alibaba-inc.com
 * date: 15-8-10.
 */
public class HBase {
    private static final Logger LOG = LoggerFactory.getLogger(StockDB.class);

    Context context;

    public HBase(Context context){
        this.context = context;
    }

    public void put(String tableName,List<Put> puts){
        HTableInterface table = context.getTable(tableName);
        try {
            table.put(puts);
        } catch (IOException e) {
            LOG.error("failed to save,table="+tableName+",puts="+puts.size());
        }finally {
            context.closeTable(table);
        }
    }

    public void put(String tableName,Put put){
        HTableInterface table = context.getTable(tableName);
        try {
            table.put(put);
        } catch (IOException e) {
            LOG.error("failed to save,table="+tableName);
        }finally {
            context.closeTable(table);
        }
    }

    public Result get(String tableName,Get get){
        HTableInterface table = context.getTable(tableName);
        try {
            return table.get(get);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            context.closeTable(table);
            LOG.error("failed to get,table="+tableName+",get="+get);
        }
        return new Result();
    }

    public Result[] get(String tableName,List<Get> gets){
        HTableInterface table = context.getTable(tableName);
        try {
            return table.get(gets);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            context.closeTable(table);
            LOG.error("failed to get,table="+tableName+",get="+gets.size());
        }
        return new Result[0];
    }

    public List<Result> scan(String tableName,Scan scan){
        HTableInterface table = context.getTable(tableName);
        List<Result> results = Lists.newLinkedList();
        try {
            ResultScanner scanner = table.getScanner(scan);
            for(Result result:scanner){
                results.add(result);
            }
        } catch (IOException e) {
            LOG.error("failed to scan,table="+tableName+",scan="+scan,e);
        }finally {
            context.closeTable(table);
        }
        return results;
    }
}
