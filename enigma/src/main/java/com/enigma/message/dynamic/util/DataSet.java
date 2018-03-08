package com.enigma.message.dynamic.util;


import com.enigma.message.dynamic.DynamicMessenger;

/**
 * @author tianyang
 *         M数据存储
 */
public class DataSet {

    public DynamicMessenger.ReceiverInfo receiverInfo;
    public Object[] args;

    public void recycle() {
        receiverInfo = null;
        args = null;
    }

    public static class DataSetPool extends Pools.SynchronizedPool<DataSet> {

        public DataSetPool(int maxPoolSize) {
            super(maxPoolSize);
        }

        @Override
        public DataSet acquire() {
            DataSet dataSet = super.acquire();
            if(dataSet == null){
                dataSet = new DataSet();
            }
            return dataSet;
        }
    }
}
