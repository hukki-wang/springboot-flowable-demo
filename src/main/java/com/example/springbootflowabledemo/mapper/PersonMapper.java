package com.example.springbootflowabledemo.mapper;

import com.example.springbootflowabledemo.po.Person;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PersonMapper{

    Person findByUsername(@Param("username") String username);

    List<Person> findAll();

    void save(@Param("person") Person person);
}
