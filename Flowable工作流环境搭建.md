# 							Flowable工作流环境搭建

## 一、前言

了解flowable和activity的前世今生，本文以flowable6.5版本作为参考。

jBPM（java Business Process Management）：Tom Baeyens（汤姆 贝恩斯）于2003年发布，于2004 年加入JBoss，jBPM4引入了PVM；而后Tom Baeyens离开了JBoss，jBPM5 放弃了 jBPM 4，基于Drools Flow重头来过
Activiti：Tom Baeyens离开了JBoss后加入了Alfresco，于2010年推出了Activiti 5，Activiti 6移除了PVM
Camunda BPM：2012年Activiti的贡献者之一Camunda（卡蒙达），从Activiti 5项目fork出一个新项目，即Camunda BPM
Flowable：2016年Activiti的开发者之一Tijs Rademakers，从Activiti 6项目fork出一个新项目，即Flowable 6

## 二、flowable流程引擎的基本知识

### 1、术语

BPMN：Business Process Model and Notation，OMG推出的业务流程图标准
CMMN：Case Management Model and Notation，OMG发布的案例管理图标准，是对BPMN的扩展；它用声明式表示法来描述流程
DMN：Decision Modeling Notation，OMG发布的决策建模图标准，用于封装BPMN中的业务决策逻辑（业务规则）

### 2.flowable的基本构成

五个官方应用（包含于Flowable下载包里的wars目录）
flowable-modeler.war：流程定义管理
flowable-task.war：用户任务管理
flowable-idm.war：用户组权限管理
flowable-rest.war：流程引擎对外提供的API接口
flowable-admin.war：后台管理
流程设计器：将 flowable-modeler.war 和 flowable-idm.war部署到tomcat，即可得到网页版流程设计器（访问路径为 /flowable-modeler，默认账号为 admin/test ）

Flowable五大引擎（包含于Flowable下载包里的libs目录）
ProcessEngine（流程引擎）、DmnEngine（决策引擎）、IdmEngine（身份识别引擎）、ContentEngine（内容引擎）、FormEngine（表单引擎）



### 3.flowable表结构说明

![](D:\wanghui\组件\flowable工作流\flowable表模型.jpg)

应用首次启动时，Flowable会往数据库里添加一些表
ACT_RE_ *：RE代表repository。具有此前缀的表包含静态信息，例如流程定义和流程资源（图像，规则等）。
ACT_RU_ *：RU代表runtime。这些是包含运行时的流程实例，用户任务，变量，作业等的运行时数据的运行时表。Flowable仅在流程实例执行期间存储运行时数据，并在流程实例结束时删除记录。这使运行时表保持小而快。
ACT_HI_ *：HI代表history。这些是包含历史数据的表，例如过去的流程实例，变量，任务等
ACT_GE_ *：general数据，用于各种用例
ACT_ID_*：Idm的用户、组



## 三、搭建flowable/all-in-one环境

说明：用户画流程图设计

docker方式

1.下载镜像并运行

```shell
docker pull flowable/all-in-one:6.5.0
docker run -d --name flowable -p 8080:8080 --privileged=true -it flowable/all-in-one
```

2.去官网下载mysql驱动

```
官网https://dev.mysql.com/downloads/connector/j/
选择platform Independent版本8.0.29可以兼容mysql5.7数据库
或者直接服务器上wget下载如果支持的话
wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.29.tar.gz
```

将驱动上传到服务器

3.将驱动拷贝到docker虚拟机的tomcat中

```shell
docker cp /root/flowable/mysql-connector-java-8.0.29/mysql-connector-java-8.0.29.jar flowable-ui:/opt/tomcat/lib
```

4.进入docker虚拟机的tomcat容器中修改数据库配置

```
docker exec -it --user root flowable sh
```

5.修改docker容器中的tomcat的context.xml配置

```xml
<Resource auth="Container"
name="jdbc/flowableDS"
type="javax.sql.DataSource"
description="JDBC DataSource"
url="jdbc:mysql://{宿主机ip}:3306/demo?allowPublicKeyRetrieval=true&&amp;useSSL=false&&amp;characterEncoding=UTF-8"
driverClassName="com.mysql.cj.jdbc.Driver"
username="root"
password="root"
defaultAutoCommit="false"
initialSize="5"
maxWaitMillis="5000"
maxTotal="120"
maxIdle="5"/>
```

6.重启容器

```shell
docker restart flowable-ui
```







## 四、搭建独立flowable流程引擎环境

引入maven依赖（flowable+h2内存数据库）

1.初始化引擎

2.定义流程图xml，也就是流程定义

3.部署流程定义

4.启动流程定义

代码：

```java
package guoyu.com.init;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.*;

public class HolidayRequest {

    public static void main(String[] args) {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        //1.创建引擎
        ProcessEngine processEngine = cfg.buildProcessEngine();

        //2.部署流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment().name("test").addClasspathResource("processes/holiday-request.bpmn20.xml").deploy();
        System.out.println("部署deployId = " + deployment.getId());

        //3.查询流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        System.out.println("process definition name： " + processDefinition.getName());

        //4.启动流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Scanner scanner = new Scanner(System.in);
        System.out.println("who are you?");
        String employee = scanner.nextLine();

        System.out.println("how many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("why do you need them?");
        String description = scanner.nextLine();

        Map<String,Object> variables = new HashMap<>(8);
        variables.put("employee",employee);
        variables.put("nrOfHolidays",nrOfHolidays);
        variables.put("description",description);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holidayRequest",variables);
        System.out.println("流程实例id：" + processInstance.getProcessInstanceId());


        //5.查询流程实例
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("task size = " + tasks.size());
        for (Task task:tasks) {
            System.out.println("task name = " + task.getName());
        }
        System.out.println("which task would you complete?");
        int taskIndex = Integer.valueOf(scanner.nextLine());
        Task task = tasks.get(taskIndex-1);
        //查询任务的过程变量 task.getProcessVariables();
        Map<String,Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") +" wants " +processVariables.get("nrOfHolidays") + "of holidays,do you approve this?");

        //6.完成任务
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        variables = new HashMap<>(8);
        variables.put("approved",approved);
        taskService.complete(task.getId(),variables);

        //7.历史任务查询
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId())
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        for (HistoricActivityInstance activity:activityInstances) {
            System.out.println(activity.getActivityId()+"took"+activity.getDurationInMillis()+"mill");
        }

    }
}

```



## 五、springboot集成flowable流程引擎

### 1.开发环境说明

idea2021.2+maven3.6+flowable bpmn visualizer插件

flowable bpmn visualizer插件：用于画flowable流程图

### 2.搭建流程

springboot+flowable+mybatis+jdk1.8+mysql5.7

1.创建springboot项目

2.引入maven依赖

```
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!--springboot-flowable依赖-->
        <dependency>
            <groupId>org.flowable</groupId>
            <artifactId>flowable-spring-boot-starter</artifactId>
            <version>6.3.0</version>
        </dependency>
        <!--  h2测试数据库      -->
<!--        <dependency>-->
<!--            <groupId>com.h2database</groupId>-->
<!--            <artifactId>h2</artifactId>-->
<!--            <version>1.3.176</version>-->
<!--        </dependency>-->
        <!--  切换数据库为mysql      -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!--rest api支持-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!--    jpa-mybatis    -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.1</version>
        </dependency>
        <!--    jpa-hibernate    -->
<!--        <dependency>-->
<!--            <groupId>org.springframework.boot</groupId>-->
<!--            <artifactId>spring-boot-starter-data-jpa</artifactId>-->
<!--        </dependency>-->

    </dependencies>
```

3.flowable数据库配置

```
spring.datasource.url=jdbc:mysql://116.62.228.126:3306/flowable?characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=root

##mybatis-jba
mybatis.mapper-locations=classpath:mapper/*.xml

##flowable打印sql日志
logging.level.org.flowable.engine.impl.persistence.entity.*:DEBUG
logging.level.org.flowable.task.service.impl.persistence.entity.*:DEBUG
```

4.启动springboot项目即可自动创建flowable相关的表

![](D:\wanghui\组件\flowable工作流\flowable表.png)

5.画流程图，采用flowable bpmn visualizer插件，需要存放在resource/processes目录下启动springboot项目即可自动部署

![](D:\wanghui\组件\flowable工作流\flowable流程图.png)

6.编写流程service类，其中启动的流程定义key是oneTaskProcess是<process id="oneTaskProcess">的id

![](D:\wanghui\组件\flowable工作流\启动流程service.png)

7.其中流程启动的加入了流程定义的变量person

![](D:\wanghui\组件\flowable工作流\流程变量.png)

8.其中流程变量可以在流程图xml中使用

![](D:\wanghui\组件\flowable工作流\xml使用流程变量.png)

9.发起流程（官方请假流程图）

![](D:\wanghui\组件\flowable工作流\发起流程详细.png)

flowable执行的sql

ACT_RE_PROCDEF表

ACT_HI_TASKINST表

ACT_HI_PROCINST表

ACT_HI_ACTINST表

ACT_RU_EXECUTION表

ACT_RU_TASK表

```
2022-07-21 16:18:17.958 DEBUG 4364 --- [nio-9999-exec-1] p.e.P.selectLatestProcessDefinitionByKey : ==>  Preparing: select * from ACT_RE_PROCDEF where KEY_ = ? and (TENANT_ID_ = '' or TENANT_ID_ is null) and DERIVED_FROM_ is null and VERSION_ = (select max(VERSION_) from ACT_RE_PROCDEF where KEY_ = ? and (TENANT_ID_ = '' or TENANT_ID_ is null)) 
2022-07-21 16:18:17.961 DEBUG 4364 --- [nio-9999-exec-1] p.e.P.selectLatestProcessDefinitionByKey : ==> Parameters: holidayRequest(String), holidayRequest(String)
2022-07-21 16:18:17.992 DEBUG 4364 --- [nio-9999-exec-1] p.e.P.selectLatestProcessDefinitionByKey : <==      Total: 1
2022-07-21 16:18:18.003 DEBUG 4364 --- [nio-9999-exec-1] o.f.e.i.p.e.ExecutionEntityManagerImpl   : Child execution Execution[ id '5008' ] - parent '5006' created with parent 5006
2022-07-21 16:18:18.110 DEBUG 4364 --- [nio-9999-exec-1] f.t.s.i.p.e.H.insertHistoricTaskInstance : ==>  Preparing: insert into ACT_HI_TASKINST ( ID_, REV_, TASK_DEF_ID_, PROC_DEF_ID_, PROC_INST_ID_, EXECUTION_ID_, SCOPE_ID_, SUB_SCOPE_ID_, SCOPE_TYPE_, SCOPE_DEFINITION_ID_, NAME_, PARENT_TASK_ID_, DESCRIPTION_, OWNER_, ASSIGNEE_, START_TIME_, CLAIM_TIME_, END_TIME_, DURATION_, DELETE_REASON_, TASK_DEF_KEY_, FORM_KEY_, PRIORITY_, DUE_DATE_, CATEGORY_, TENANT_ID_, LAST_UPDATED_TIME_ ) values ( ?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) 
2022-07-21 16:18:18.123 DEBUG 4364 --- [nio-9999-exec-1] f.t.s.i.p.e.H.insertHistoricTaskInstance : ==> Parameters: 5011(String), null, holidayRequest:1:5005(String), 5006(String), 5008(String), null, null, null, null, Approve or reject request(String), null, null, null, null, 2022-07-21 16:18:18.053(Timestamp), null, null, null, null, approveTask(String), null, 50(Integer), null, null, (String), 2022-07-21 16:18:18.053(Timestamp)
2022-07-21 16:18:18.172 DEBUG 4364 --- [nio-9999-exec-1] f.t.s.i.p.e.H.insertHistoricTaskInstance : <==    Updates: 1
2022-07-21 16:18:18.174 DEBUG 4364 --- [nio-9999-exec-1] .e.i.p.e.H.insertHistoricProcessInstance : ==>  Preparing: insert into ACT_HI_PROCINST ( ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PROC_DEF_ID_, START_TIME_, END_TIME_, DURATION_, START_USER_ID_, START_ACT_ID_, END_ACT_ID_, SUPER_PROCESS_INSTANCE_ID_, DELETE_REASON_, TENANT_ID_, NAME_, CALLBACK_ID_, CALLBACK_TYPE_ ) values ( ?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) 
2022-07-21 16:18:18.180 DEBUG 4364 --- [nio-9999-exec-1] .e.i.p.e.H.insertHistoricProcessInstance : ==> Parameters: 5006(String), 5006(String), null, holidayRequest:1:5005(String), 2022-07-21 16:18:17.995(Timestamp), null, null, null, startEvent(String), null, null, null, (String), null, null, null
2022-07-21 16:18:18.230 DEBUG 4364 --- [nio-9999-exec-1] .e.i.p.e.H.insertHistoricProcessInstance : <==    Updates: 1
2022-07-21 16:18:18.241 DEBUG 4364 --- [nio-9999-exec-1] p.e.H.bulkInsertHistoricActivityInstance : ==>  Preparing: insert into ACT_HI_ACTINST ( ID_, REV_, PROC_DEF_ID_, PROC_INST_ID_, EXECUTION_ID_, ACT_ID_, TASK_ID_, CALL_PROC_INST_ID_, ACT_NAME_, ACT_TYPE_, ASSIGNEE_, START_TIME_, END_TIME_, DURATION_, DELETE_REASON_, TENANT_ID_ ) values (?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) , (?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
2022-07-21 16:18:18.251 DEBUG 4364 --- [nio-9999-exec-1] p.e.H.bulkInsertHistoricActivityInstance : ==> Parameters: 5009(String), holidayRequest:1:5005(String), 5006(String), 5008(String), startEvent(String), null, null, null, startEvent(String), null, 2022-07-21 16:18:18.008(Timestamp), 2022-07-21 16:18:18.012(Timestamp), 4(Long), null, (String), 5010(String), holidayRequest:1:5005(String), 5006(String), 5008(String), approveTask(String), 5011(String), null, Approve or reject request(String), userTask(String), null, 2022-07-21 16:18:18.016(Timestamp), null, null, null, (String)
2022-07-21 16:18:18.302 DEBUG 4364 --- [nio-9999-exec-1] p.e.H.bulkInsertHistoricActivityInstance : <==    Updates: 2
2022-07-21 16:18:18.321 DEBUG 4364 --- [nio-9999-exec-1] o.f.e.i.p.e.E.bulkInsertExecution        : ==>  Preparing: insert into ACT_RU_EXECUTION (ID_, REV_, PROC_INST_ID_, BUSINESS_KEY_, PROC_DEF_ID_, ACT_ID_, IS_ACTIVE_, IS_CONCURRENT_, IS_SCOPE_,IS_EVENT_SCOPE_, IS_MI_ROOT_, PARENT_ID_, SUPER_EXEC_, ROOT_PROC_INST_ID_, SUSPENSION_STATE_, TENANT_ID_, NAME_, START_ACT_ID_, START_TIME_, START_USER_ID_, IS_COUNT_ENABLED_, EVT_SUBSCR_COUNT_, TASK_COUNT_, JOB_COUNT_, TIMER_JOB_COUNT_, SUSP_JOB_COUNT_, DEADLETTER_JOB_COUNT_, VAR_COUNT_, ID_LINK_COUNT_, CALLBACK_ID_, CALLBACK_TYPE_) values (?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) , (?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) 
2022-07-21 16:18:18.342 DEBUG 4364 --- [nio-9999-exec-1] o.f.e.i.p.e.E.bulkInsertExecution        : ==> Parameters: 5006(String), 5006(String), null, holidayRequest:1:5005(String), null, true(Boolean), false(Boolean), true(Boolean), false(Boolean), false(Boolean), null, null, 5006(String), 1(Integer), (String), null, startEvent(String), 2022-07-21 16:18:17.995(Timestamp), null, true(Boolean), 0(Integer), 0(Integer), 0(Integer), 0(Integer), 0(Integer), 0(Integer), 0(Integer), 0(Integer), null, null, 5008(String), 5006(String), null, holidayRequest:1:5005(String), approveTask(String), true(Boolean), false(Boolean), false(Boolean), false(Boolean), false(Boolean), 5006(String), null, 5006(String), 1(Integer), (String), null, null, 2022-07-21 16:18:18.003(Timestamp), null, true(Boolean), 0(Integer), 1(Integer), 0(Integer), 0(Integer), 0(Integer), 0(Integer), 0(Integer), 0(Integer), null, null
2022-07-21 16:18:18.394 DEBUG 4364 --- [nio-9999-exec-1] o.f.e.i.p.e.E.bulkInsertExecution        : <==    Updates: 2
2022-07-21 16:18:18.396 DEBUG 4364 --- [nio-9999-exec-1] o.f.t.s.i.p.e.TaskEntityImpl.insertTask  : ==>  Preparing: insert into ACT_RU_TASK (ID_, REV_, NAME_, PARENT_TASK_ID_, DESCRIPTION_, PRIORITY_, CREATE_TIME_, OWNER_, ASSIGNEE_, DELEGATION_, EXECUTION_ID_, PROC_INST_ID_, PROC_DEF_ID_, TASK_DEF_ID_, SCOPE_ID_, SUB_SCOPE_ID_, SCOPE_TYPE_, SCOPE_DEFINITION_ID_, TASK_DEF_KEY_, DUE_DATE_, CATEGORY_, SUSPENSION_STATE_, TENANT_ID_, FORM_KEY_, CLAIM_TIME_, IS_COUNT_ENABLED_, VAR_COUNT_, ID_LINK_COUNT_, SUB_TASK_COUNT_) values (?, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) 
2022-07-21 16:18:18.407 DEBUG 4364 --- [nio-9999-exec-1] o.f.t.s.i.p.e.TaskEntityImpl.insertTask  : ==> Parameters: 5011(String), Approve or reject request(String), null, null, 50(Integer), 2022-07-21 16:18:18.016(Timestamp), null, null, null, 5008(String), 5006(String), holidayRequest:1:5005(String), null, null, null, null, null, approveTask(String), null, null, 1(Integer), (String), null, null, true(Boolean), 0(Integer), 0(Integer), 0(Integer)
2022-07-21 16:18:18.458 DEBUG 4364 --- [nio-9999-exec-1] o.f.t.s.i.p.e.TaskEntityImpl.insertTask  : <==    Updates: 1
```

10.完成当前流程

完成任务代码

```java
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
```

1）从启动流程中的变量找到发起人的任务

![](D:\wanghui\组件\flowable工作流\发起流程的发起人实例变量.png)

2）改变量跟xml中的flowable:assignee对应

![](D:\wanghui\组件\flowable工作流\xml实例变量.png)

发起流程后assignee会落入insert into ACT_RU_TASK表中

11.审核任务

```java
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
```

1）审核通过（审核结果为xml定义的实例变量，由代码通过任务运行变量传递）

![](D:\wanghui\组件\flowable工作流\审核意见变量.png)

2）当前任务审批人为上一个任务提交的实例变量

![](D:\wanghui\组件\flowable工作流\任务审批人变量传递.png)

3）如果通过当前任务的审批，需要传递到下一个任务审批人（直到任务审批结束）

![](D:\wanghui\组件\flowable工作流\当前审批到下一个任务审批.png)

4）审批通过或者拒绝可以通过属性回调

![](D:\wanghui\组件\flowable工作流\xml属性回调定义.png)

回调类需要实现JavaDelegate

```java
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

```

12.任务流程的关键表总结、以及关键id

ACT_GE_BYTEARRAY、ACT_RE_DEPLOYMENT、ACT_RE_PROCDEF、ACT_RU_EXECUTION、ACT_RU_TASK、ACT_RU_VARIABLE

实例id，任务id，运行时变量











