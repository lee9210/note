package com.example.demo.cache;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GuavaCache {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AtomicInteger i = new AtomicInteger(0);
            //缓存接口这里是LoadingCache，LoadingCache在缓存项不存在时可以自动加载缓存
        LoadingCache<String, JSONObject> keyCache
                    //CacheBuilder的构造函数是私有的，只能通过其静态方法newBuilder()来获得CacheBuilder的实例
                    = CacheBuilder.newBuilder()
                    //设置并发级别为8，并发级别是指可以同时写缓存的线程数
                    .concurrencyLevel(8)
                    //设置写缓存后1秒钟过期
                    .expireAfterWrite(1, TimeUnit.SECONDS)
                    //设置缓存容器的初始容量为10
                    .initialCapacity(10)
                    //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
                    .maximumSize(100)
                    //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
                    .build(new CacheLoader<String, JSONObject>() {
                           @Override
                           public JSONObject load(String key) {
                               i.addAndGet(i.get()+1);
                               JSONObject object = new JSONObject();
                               object.put("i",i.get());
                               return object;
                           }
                        }
                    );
        for (int j =0; j < 10 ;j++) {
            Thread.sleep(1000);
            System.out.println(keyCache.get("e").getString("i"));
        }
    }

}
