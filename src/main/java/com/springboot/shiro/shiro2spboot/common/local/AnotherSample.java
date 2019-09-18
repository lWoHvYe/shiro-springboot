package com.springboot.shiro.shiro2spboot.common.local;

import com.springboot.shiro.shiro2spboot.common.util.DateTimeUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * 抽卡模拟
 * 将抽卡简化成随机取一个1000的样本中的数，取到指定的算抽中
 * 在取到需要的时，会将与其同样的从期望中一并移除
 * <p>
 * 使用多线程时，有时需关注其他线程的完成情况
 * 采用Feature的方式，正在调整同步数据,已确定其他功能部分的完成
 */
//@SpringBootTest
public class AnotherSample {

    private Logger logger4j = LoggerFactory.getLogger(AnotherSample.class);

    private volatile Map<String, Integer> countMap = new Hashtable<>() {
        {
            put("s50", 0);
            put("s100", 0);
            put("s150", 0);
            put("s200", 0);
            put("s250", 0);
            put("s300", 0);
            put("s350", 0);
            put("s400", 0);
            put("s450", 0);
            put("s500", 0);
            put("other", 0);
        }
    };


    public AnotherSample() {
    }

    @Test
    public void test() throws NoSuchAlgorithmException {
        long start = DateTimeUtil.getCurMilli();
        AnotherSample sample = new AnotherSample();
//        创建随机数
//        池子集合
        List<List<Integer>> lists = new ArrayList<>();
//        池子1 2% 2% 2.5% 2.5% 2.5%
        List<Integer> list1 = Arrays.asList(20, 20, 25, 25, 25);

//        池子2 2% 1.8% 1.8% 2.5% 5%
        List<Integer> list2 = Arrays.asList(20, 18, 25, 50);
        lists.add(list1);
        lists.add(list2);
        SimuCallable simuCallable = new SimuCallable(lists);
//            因为把结果输出放在主线程，所以需要设计主线程等待其他线程结束
        CompletableFuture<String> work1 = CompletableFuture.supplyAsync(() ->
                simuCallable.call()
        );
        CompletableFuture<String> work2 = CompletableFuture.supplyAsync(() ->
                simuCallable.call()
        );
        CompletableFuture<String> work3 = CompletableFuture.supplyAsync(() ->
                simuCallable.call()
        );
//        CompletableFuture<String> work4 = CompletableFuture.supplyAsync(() ->
//                simuCallable.call()
//        );
//        CompletableFuture<String> work5 = CompletableFuture.supplyAsync(() ->
//                simuCallable.call()
//        );

        CompletableFuture<Void> result = CompletableFuture.allOf(work1, work2, work3);
        result.join();
        sample.printResult();
        long end = DateTimeUtil.getCurMilli();
        System.out.println(end - start);
    }



    /**
     * 输出模拟结果
     */
    private void printResult() {
        System.out.println("输出结果");

        logger4j.info("50次以内：" + countMap.get("s50") * 0.0001 + "%;");
        logger4j.info("100次以内：" + countMap.get("s100") * 0.0001 + "%;");
        logger4j.info("150次以内：" + countMap.get("s150") * 0.0001 + "%;");
        logger4j.info("200次以内：" + countMap.get("s200") * 0.0001 + "%;");
        logger4j.info("250次以内：" + countMap.get("s250") * 0.0001 + "%;");
        logger4j.info("300次以内：" + countMap.get("s300") * 0.0001 + "%;");
        logger4j.info("350次以内：" + countMap.get("s350") * 0.0001 + "%;");
        logger4j.info("400次以内：" + countMap.get("s400") * 0.0001 + "%;");
        logger4j.info("450次以内：" + countMap.get("s450") * 0.0001 + "%;");
        logger4j.info("500次以内：" + countMap.get("s500") * 0.0001 + "%;");
        logger4j.info("500次以上：" + countMap.get("other") * 0.0001 + "%;");
        System.out.println("总计模拟:" + (countMap.get("s50") + countMap.get("s100") + countMap.get("s150") + countMap.get("s200")
                + countMap.get("s250") + countMap.get("s300") + countMap.get("s350") + countMap.get("s400") + countMap.get("s450")
                + countMap.get("s500") + countMap.get("other")) + "次");
    }


    /**
     * 模拟多线程
     */
    class SimuCallable implements Callable<String> {

        private List<List<Integer>> lists;
        private int s50 = 0;
        private int s100 = 0;
        private int s150 = 0;
        private int s200 = 0;
        private int s250 = 0;
        private int s300 = 0;
        private int s350 = 0;
        private int s400 = 0;
        private int s450 = 0;
        private int s500 = 0;
        private int other = 0;

        private Random random = SecureRandom.getInstanceStrong();

        public SimuCallable(List<List<Integer>> lists) throws NoSuchAlgorithmException {
            this.lists = lists;
        }

        @Override
        public String call() {
            for (int j = 0; j < 1000; j++) {
//            开始模拟
                int count = simulate(random, lists);
//                将模拟结果放入集合中
                countNumber(count);
            }
            System.out.println("计算完成:" + Thread.currentThread().getName()
                    + ":" + s50 + ":" + s100 + ":" + s150 + ":" + s200 + ":" + s250 + ":" + s300
                    + ":" + s350 + ":" + s400 + ":" + s450 + ":" + s500 + ":" + other);
            countMap.put("s50", countMap.get("s50") + s50);
            countMap.put("s100", countMap.get("s100") + s100);
            countMap.put("s150", countMap.get("s150") + s150);
            countMap.put("s200", countMap.get("s200") + s200);
            countMap.put("s250", countMap.get("s250") + s250);
            countMap.put("s300", countMap.get("s300") + s300);
            countMap.put("s350", countMap.get("s350") + s350);
            countMap.put("s400", countMap.get("s400") + s400);
            countMap.put("s450", countMap.get("s450") + s450);
            countMap.put("s500", countMap.get("s500") + s500);
            countMap.put("other", countMap.get("other") + other);
            System.out.println("运行结束");
            return Thread.currentThread().getName();
        }

        private int simulate(Random random, List<List<Integer>> lists) {
            //        抽卡数
            int count = 0;
//              存放目标集合，内部数个子集合
            List<List<Integer>> mblist = new ArrayList<>();
//            存放目标值的集合
            List<Integer> dblist = new ArrayList<>();
            for (List<Integer> list : lists) {
//            生成目标数值的开始值
                int start = 1;
                for (Integer integer : list) {
//                单个子集合
                    List<Integer> zlist = new ArrayList<>();
                    for (int i = 0; i < integer; i++) {
                        zlist.add(start);
                        start++;
                    }
                    mblist.add(zlist);
                    dblist.addAll(zlist);
                }
//            当目标值不为空时进行抽卡
                while (!dblist.isEmpty()) {
//            开始抽卡
                    int result = random.nextInt(1000) + 1;
//                判断是否抽到目标卡
                    if (dblist.contains(result)) {
//                    当抽到目标卡时，遍历目标子集合
                        for (List<Integer> integerList : mblist) {
//                        判断目标是否在子集合中
                            if (integerList.contains(result)) {
//                            当目标在子集合中时，从目标集合中移除对应子集合内容
                                dblist.removeAll(integerList);
                            }
                        }
                    }
//                抽卡次数+1
                    count++;
                }
//            抽完一池，置空子集合
                mblist.clear();
            }
            return count;
        }

        public void countNumber(Integer count) {
            if (count <= 50) {
                s50++;
            } else if (count <= 100) {
                s100++;
            } else if (count <= 150) {
                s150++;
            } else if (count <= 200) {
                s200++;
            } else if (count <= 250) {
                s250++;
            } else if (count <= 300) {
                s300++;
            } else if (count <= 350) {
                s350++;
            } else if (count <= 400) {
                s400++;
            } else if (count <= 450) {
                s450++;
            } else if (count <= 500) {
                s500++;
            } else {
                other++;
            }
        }
    }
}
