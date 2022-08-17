package com.example.springbootflowabledemo.callback;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * service task拒绝或者通过回调
 * 我们还没有实现申请通过后执行的自动逻辑
 */
public class SendRejectionMail implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Send out rejection email for employee "
                + execution.getVariable("employee"));
    }
}
