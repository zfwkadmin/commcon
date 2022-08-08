package com.zqazfl.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @随机生成选房顺序号
 * @version 1.0
 * @author Duan_mz
 * @date 2020-12-15
 */

public class RandomrUtil {
    //传入最大顺序号和已被选用的顺序List
    public static int getRandomrNum(int max,List<Integer> exclude){

        if (exclude==null){
            return (int) (Math.random()*max+1);
        }else{
            List<Integer> list2=new ArrayList<>();//待抽取数的List

            for (int i = 1; i <= max ; i++) {
                if (!(exclude.contains(i))){
                    list2.add(i);
                }
            }
            Integer[] arr2 = new Integer[list2.size()];
            list2.toArray(arr2);

            int index = (int) (Math.random() * arr2.length);
            int rand = arr2[index];
            return rand;
        }
    }

    public static int getRandomrNum(int max){
        return getRandomrNum(max,null);
    }

    // 随机Int[]数组生成，给定随机数个数k，生成1-k的随机序列
    public static Integer[] getRandomArr(int k) {
        //生成0~k之间的数组
        int n=k;
        Integer[] x = new Integer[n];
        for (int i = 0; i < n; i++) {
            x[i] = i+1;
        }

        //开始随机 k 个不重复数出来
        Random random = new Random();
        for (int i = 0; i < k; i++) {
            // t : i 至 n 的随机数
            // 目的：不再随机出已置换出去 的数 的下标
            int t = random.nextInt(n - i) + i;
            // 交换x[i]，x[t]的值
            int temp = x[i];
            x[i] = x[t];
            x[t] = temp;
        }
        return x;
    }

    // 随机数生成，随机范围为给定List集合
    public static int getInteger(List<Integer> list) {
        Integer[] arr =new Integer[list.size()];
        list.toArray(arr);
        //产生0-(arr.length-1)的整数值,也是数组的索引
        int index = (int) (Math.random() * arr.length);
        int rand = arr[index];
        return rand;
    }

}
