package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Scheduled(cron = "0 */1 * * * ?")
    private void closeOrderTask(){
        log.info("关闭订单定时任务开始");
        long lockTimeOut = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.RedisLock.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis() + lockTimeOut));
        if (setnxResult != null && setnxResult.intValue() == 1){
            closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        }else{
            String lockValueStr = RedisShardedPoolUtil.get(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)){
               String getSetResult =  RedisShardedPoolUtil.getSet(lockValueStr,String.valueOf(System.currentTimeMillis()+lockTimeOut));
               if (getSetResult == null || StringUtils.equals(lockValueStr,getSetResult)){
                   //获取锁
                   closeOrder(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
               }else{
                   log.info("没有获取到分布式锁：{}",Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
               }
            }
            log.info("没有获取到分布式锁：{}",Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");
    }

    private void closeOrder(String lockName){
        //有效期5秒防止死锁
        RedisShardedPoolUtil.expire(lockName,5);
        log.info("获取{},ThreadName:{}",Const.RedisLock.CLOSE_ORDER_TASK_LOCK,Thread.currentThread());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("clost.order.task.time.hour","2"));
        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(Const.RedisLock.CLOSE_ORDER_TASK_LOCK);
        log.info("释放{},ThreadName:{}",Const.RedisLock.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("===============================");
    }
}
