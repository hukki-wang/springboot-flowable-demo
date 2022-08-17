package com.example.springbootflowabledemo.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class WorkFlowService {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    /**
     * 发起流程
     * @param processInstanceId
     * @param staffId
     */
    public void startProcess(String processInstanceId,String staffId){
        log.info("流程发起人：{}",staffId);
        Map<String,Object> variables = new HashMap<>();
        //指定发起人
        variables.put("staffId", staffId);
        variables.put("leaveTask","测试");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processInstanceId,variables);
        runtimeService.setVariable(processInstance.getId(), "name", "javaboy");
        runtimeService.setVariable(processInstance.getId(), "reason", "休息一下");
        runtimeService.setVariable(processInstance.getId(), "days", 10);

        log.info("流程实例ID：{}",processInstance.getProcessInstanceId());
    }

    public void completeTask(String staffId,String zuzhangId){
        //员工查找到自己的任务，然后提交给组长审批
        List<Task> list = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskId().desc().list();
        for (Task task : list) {
            log.info("任务 ID：{}；任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
            Map<String, Object> map = new HashMap<>();
            //提交给组长的时候，需要指定组长的 id
            map.put("zuzhangTask", zuzhangId);
            taskService.complete(task.getId(), map);
            log.info("运行到当前流程实例ID：{}",task.getProcessInstanceId());
        }

    }

    /**
     * 审批
     * @param assignee 当前任务审批人
     * @param approved 审批一件
     * @param nextAssignee 下一个审批人
     */
    public void approveTask(String assignee,String approved,String nextAssignee){
        List<Task> list = taskService.createTaskQuery().taskAssignee(assignee).orderByTaskId().desc().list();
        for (Task task : list) {
            log.info("组长 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            //组长审批的时候，如果是同意，需要指定经理的 id
            map.put("managerTask", nextAssignee);
            map.put("checkResult", approved);
            taskService.complete(task.getId(), map);
            log.info("运行到当前流程实例ID：{}",task.getProcessInstanceId());
        }
    }


}
