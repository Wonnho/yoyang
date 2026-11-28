package com.yoyang.carer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Carer {

    public static final int NAME_LIMIT = 100;
    public static final int SEX_LIMIT = 30;
    public static final int DESCRIPTION_LIMIT = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NAME_LIMIT)
    private String name;

    @Column(nullable = false, length = SEX_LIMIT)
    private String sex;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = DESCRIPTION_LIMIT)
    private String description;

    @Column(nullable = false)
    private int careerYear;

    protected Carer() {
    }

    public Carer(String name, String sex, int age, String description, int careerYear) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.description = description;
        this.careerYear = careerYear;
    }
}
