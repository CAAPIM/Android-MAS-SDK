package com.ca.mas;

public class ScenarioInfo {
    private int id;
    private String name;
    private int iteration;
    private Double benchmark;
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public Double getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(Double benchmark) {
        this.benchmark = benchmark;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
