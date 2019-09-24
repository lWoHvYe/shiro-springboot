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
import java.util.concurrent.ExecutionException;

/**
 * @author Hongyan Wang
 * @packageName com.springboot.shiro.shiro2spboot.common.local
 * @className FutureSample
 * @description 抽卡模拟 将抽卡简化成随机取一个1000的样本中的数，取到指定的算抽中
 * 在取到需要的时，会将与其同样的从期望中一并移除
 * 由于模拟采用了随机数的方式，所以池子可以任意配置，不影响结果
 * 由于使用了多线程，所以需关注其他线程的完成情况
 * 采用Feature的方式，使用CompletableFuture的runAsync()构建没有返回的子线程，各子线程处理自身结果
 * 经过调整使用ThreadLocal修饰变量，简化线程内各函数的传值，但会一定程度上降低效率
 * 需尤其注意变量的作用范围问题
 * @date 2019/9/22 8:54
 */
//TODO 使用CompletableFuture,使用各子线程处理自身数据的方式，类似于ThreadSample的方法，尚未完成，当前存在严重的线程安全问题，待解决
//@SpringBootTest
public class FutureSample {

    private Logger logger4j = LoggerFactory.getLogger(FutureSample.class);

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


    /**
     * @return void
     * @description 方法主体，用于模拟调用，获取及输出模拟结果
     * @params []
     * @author Hongyan Wang
     * @date 2019/9/23 9:59
     */
    @Test
    @SuppressWarnings("unchecked")
    public void startWork() throws ExecutionException, InterruptedException {
        //   开启模拟线程数
        Integer threadCount = 10;
//        模拟次数
        Integer simCount = 10000;
//        记录开始时间
        long start = DateTimeUtil.getCurMilli();
        FutureSample futureSample = new FutureSample();
//        创建随机数
//        池子集合
        List<List<Integer>> lists = new ArrayList<>();
//        池子1 2% 2% 2.5% 2.5% 2.5%
        List<Integer> list1 = Arrays.asList(20, 20, 25, 25, 25);

//        池子2 2% 1.8% 1.8% 2.5% 5%
        List<Integer> list2 = Arrays.asList(20, 18, 18, 25, 50);
//        池子3 2% 2% 2.5% 5%
//        List<Integer> list3 = Arrays.asList(20, 20, 25, 50);
//        可以根据需求调整池子，将概率乘以1000即为预放入集合中的值，之后需要把池子放入总集
        lists.add(list1);
        lists.add(list2);
//        lists.add(list3);
//        设置模拟池子
        SimCallable simCallable = new SimCallable(lists, simCount / threadCount);
//        创建线程数组
        CompletableFuture<Void>[] futuresArray = new CompletableFuture[threadCount];
//            开启模拟线程，使用线程池的方式创建CompletableFuture
        for (int i = 0; i < threadCount; i++) {
//            创建模拟线程
            CompletableFuture<Void> future = CompletableFuture.runAsync(simCallable::call);
//            将线程放入线程数组
            futuresArray[i] = future;
        }

//        设置需等待的子线程
        CompletableFuture<Void> result = CompletableFuture.allOf(futuresArray);
//        等待线程完成
        result.join();

//         输出总结果
        futureSample.printResult(countMap, simCount / 100);
//         记录结束时间
        long end = DateTimeUtil.getCurMilli();
        System.out.println(end - start);

    }

    /**
     * @return void
     * @description 输出模拟结果
     * @params [countMap, simCount, totalCount]
     * @author Hongyan Wang
     * @date 2019/9/24 13:56
     */
    private void printResult(Map<String, Integer> countMap, Integer simCount) {

        System.out.println("输出结果");

        logger4j.info(String.format("50次以内：%s%%;", (double) countMap.get("s50") / simCount));
        logger4j.info(String.format("100次以内：%s%%;", (double) countMap.get("s100") / simCount));
        logger4j.info(String.format("150次以内：%s%%;", (double) countMap.get("s150") / simCount));
        logger4j.info(String.format("200次以内：%s%%;", (double) countMap.get("s200") / simCount));
        logger4j.info(String.format("250次以内：%s%%;", (double) countMap.get("s250") / simCount));
        logger4j.info(String.format("300次以内：%s%%;", (double) countMap.get("s300") / simCount));
        logger4j.info(String.format("350次以内：%s%%;", (double) countMap.get("s350") / simCount));
        logger4j.info(String.format("400次以内：%s%%;", (double) countMap.get("s400") / simCount));
        logger4j.info(String.format("450次以内：%s%%;", (double) countMap.get("s450") / simCount));
        logger4j.info(String.format("500次以内：%s%%;", (double) countMap.get("s500") / simCount));
        logger4j.info(String.format("500次以上：%s%%;", (double) countMap.get("other") / simCount));
        System.out.println("总计模拟:" + (countMap.get("s50") + countMap.get("s100") + countMap.get("s150") + countMap.get("s200")
                + countMap.get("s250") + countMap.get("s300") + countMap.get("s350") + countMap.get("s400") + countMap.get("s450")
                + countMap.get("s500") + countMap.get("other")) + "次");
    }

    /**
     * @author Hongyan Wang
     * @description 模拟多线程相关类
     * @className SimCallable
     * @date 2019/9/23 9:53
     */
    class SimCallable implements Callable<Map<String, Integer>> {
        //        卡池集
        private List<List<Integer>> lists;
        //        总模拟次数
        private Integer simCount;

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

        private SimCallable(List<List<Integer>> lists, Integer simCount) {
            this.lists = lists;
            this.simCount = simCount;
        }

        /**
         * @return java.util.Map<java.lang.String, java.lang.Integer>
         * @description 不再使用同步变量，直接将各子线程结果返回，由主线程处理,
         * 池子是否乱序并不影响结果，若每次模拟都重新生成乱序池子将大幅降低效率，可以一个线程只使用一个乱序池子，但实际意义不大
         * @params []
         * @author Hongyan Wang
         * @date 2019/9/23 9:52
         */
        @Override
        public Map<String, Integer> call() {
//          记录本线程模拟结果集
            Map<String, Integer> countHashMap = new HashMap<>();
            try {
                Random random = SecureRandom.getInstanceStrong();
//              生成乱序池子
                int[] ranArray = ranArray();
                for (int j = 0; j < simCount; j++) {
//                开始模拟
                    int count = simulateWork(random, lists, ranArray);
                    //TODO 后续需对统计进行优化
//                将模拟结果放入集合中
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
            } catch (NoSuchAlgorithmException e) {
                logger4j.info(e.getMessage());
            }

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

            System.out.println("运行结束" + Thread.currentThread().getName() + " : " + (s50 + s100 + s150 + s200 + s250 + s300 + s350 + s400 + s450 + s500 + other) + "次");
            return countHashMap;
        }

        /**
         * @return int[]
         * @description 生成乱序不重复数组，作为模拟池
         * @params []
         * @author Hongyan Wang
         * @date 2019/9/23 9:51
         */
        private int[] ranArray() {
            var ranArrays = new int[1000];
            try {
                for (int i = 0; i < 1000; i++) ranArrays[i] = i + 1;
                Random r = SecureRandom.getInstanceStrong();
                for (int i = 0; i < 1000; i++) {
                    int in = r.nextInt(1000 - i) + i;
                    int t = ranArrays[in];
                    ranArrays[in] = ranArrays[i];
                    ranArrays[i] = t;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return ranArrays;
        }

        /**
         * @return int
         * @description 核心代码
         * 模拟抽卡，当前为单个池子，根据要求，生成数个不重复的数值集合，
         * 当结果在某个数值集合中时，从目标集合中移除其所在的集合
         * 当前使用连续生成数值的方式
         * @params [random, lists, ranArray]
         * @author Hongyan Wang
         * @date 2019/9/23 9:39
         */
        private int simulateWork(Random random, List<List<Integer>> lists, int[] ranArray) {
            //        抽卡数
            int count = 0;
//                存放单个池子目标集合，内部数个子集合
            List<List<Integer>> multiList = new ArrayList<>();
//                存放单个池子目标值的集合，主要为了避免每次模拟都要对multiList进行两次遍历
            List<Integer> numList = new ArrayList<>();
//            对池子进行模拟，抽完一个之后，再抽下一个
            for (List<Integer> list : lists) {
//                  生成目标数值的开始值
                int index = 1;
                for (Integer integer : list) {
//                      单个子集合
                    List<Integer> singleList = new ArrayList<>();
                    for (int i = 0; i < integer; i++) {
                        singleList.add(ranArray[index]);
                        index++;
                    }
                    numList.addAll(singleList);
                    multiList.add(singleList);
                }
                //TODO 模拟部分是花费时间最多的地方，是主要的优化部分
//            当目标值不为空时进行抽卡
                while (!numList.isEmpty()) {
//            开始抽卡
                    var result = random.nextInt(1000) + 1;
//                判断是否抽到目标卡
                    if (numList.contains(result))
//                    当抽到目标卡时，遍历目标子集合
                        for (List<Integer> integerList : multiList)
//                        判断目标是否在子集合中
                            if (integerList.contains(result))
//                            当目标在子集合中时，从目标集合中移除对应子集合内容
                                numList.removeAll(integerList);
//                抽卡次数+1
                    count++;
                }
//            抽完一池，置空子集合
                multiList.clear();
            }
            return count;
        }
    }
}
