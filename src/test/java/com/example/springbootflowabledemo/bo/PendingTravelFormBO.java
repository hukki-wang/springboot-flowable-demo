package com.example.springbootflowabledemo.bo;

import lombok.*;

import java.util.List;

@Data
public class PendingTravelFormBO {


    @Data
    public static class FormBusinessData{


        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BaseForm{

            @Singular(value = "trip")
            private List<String> businessTrip;
            private String test;
        }
    }


}
