package com.example.springbootflowabledemo.service;

import com.example.springbootflowabledemo.mapper.PersonMapper;
import com.example.springbootflowabledemo.po.Person;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MyService {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PersonMapper personMapper;

    /**
     * 启动流程(发起流程)
     */
    public void startProcess(String assignee){
        Person person = personMapper.findByUsername(assignee);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("person", person);
        runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    }

    /**
     * 完成指定任务
     * @param taskId
     */
    public void completeProcess(String taskId){
        HashMap<String, Object> variables = new HashMap<>();

        variables.put("checkResult", "驳回");
        taskService.complete(taskId, variables);
    }

    /**
     * 获取任务
     * @param assignee
     * @return
     */
    public List<Task> getTasks(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }

    public void createDemoUsers() {
        if (personMapper.findAll().size() == 0) {
            personMapper.save(new Person("jbarrez", "Joram", "Barrez", new Date()));
            personMapper.save(new Person("trademakers", "Tijs", "Rademakers", new Date()));
        }
    }

}
