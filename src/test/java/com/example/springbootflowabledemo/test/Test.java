package com.example.springbootflowabledemo.test;

import com.example.springbootflowabledemo.bo.PendingTravelFormBO;

public class Test {

    public static void main(String[] args) {
        PendingTravelFormBO.FormBusinessData.BaseForm baseForm = new PendingTravelFormBO.FormBusinessData.BaseForm();
        baseForm.setTest("cccd");
        baseForm = PendingTravelFormBO.FormBusinessData.BaseForm.builder().trip("ddd").build();
        System.out.println(baseForm.toString());
        baseForm.setTest("cds");
        System.out.println(baseForm.toString());
    }
}
