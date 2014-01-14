/*
 * (C) 2007-2012 Alibaba Group Holding Limited.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * Authors:
 *   leiwen <chrisredfield1985@126.com> , boyan <killme2008@gmail.com>
 */
package com.taobao.diamond.util;

import java.util.List;
import java.util.Random;

import com.taobao.diamond.domain.DiamondConf;


public class RandomDiamondUtils {

    private List<DiamondConf> allDiamondConfs;
    private int retry_times;
    private int max_times;
    private int[] randomIndexSequence;
    private int currentIndex;

    public void init(List<DiamondConf> diamondConfs) {
        int len=diamondConfs.size();
        if(allDiamondConfs==null){
            allDiamondConfs=diamondConfs;
        }
        //最大访问次数为diamondConfs.size()
        max_times=len;
        //设置重试次数为0
        retry_times=0;
        //当前下标设置为0
        currentIndex=0;
        //初始化下标数组
        randomIndexSequence=new int[len];
        //赋值
        for(int i=0;i<len;i++){
            randomIndexSequence[i]=i;
        }
        // 1.长度为１直接返回
        if(len==1)
            return;
        // 2.长度为２,50%的概率换一下
        Random random=new Random();
        if(len==2 && random.nextInt(2)==1) 
        {
           int temp=randomIndexSequence[0];
           randomIndexSequence[0]=randomIndexSequence[1];
           randomIndexSequence[1]=temp;
           return;
        }
        // 3.随机产生一个0~n-2的下标,并将此下标的值与数组最后一个元素交换,进行2n次
        int times=2 * len;
        for(int j=0;j<times;j++){
            int selectedIndex=random.nextInt(len-1);
            //将随机产生下标的值与最后一个元素值交换
            int temp=randomIndexSequence[selectedIndex];
            randomIndexSequence[selectedIndex]=randomIndexSequence[len-1];
            randomIndexSequence[len-1]=temp;
        }
    }


    public int getRetry_times() {
        return retry_times;
    }

    public int getMax_times() {
        return max_times;
    }
    /**
     * 随机取得一个diamondServer配置对象
     * 
     * @param diamondConfs
     * @return DiamondConf diamondServer配置对象
     */
    public DiamondConf generatorOneDiamondConf(){
        DiamondConf diamondConf=null;
        //访问下标小于最后一个下标
        if(retry_times < max_times){
            //得到当前访问下标
            currentIndex=randomIndexSequence[retry_times];
            diamondConf = allDiamondConfs.get(currentIndex);
        }
        else{
            randomIndexSequence=null;    
        }
        retry_times++;
        return diamondConf;
    }


    public int[] getRandomIndexSequence() {
        return randomIndexSequence;
    }
    public String getSequenceToString(){
        StringBuilder sb=new StringBuilder();
        for(int i : this.randomIndexSequence)
            sb.append(i+"");
        return sb.toString();
    }
}
