package com.ca.mas;

public class ScenarioInfo {
    private int id = -1;
    private String name = null;
    private int iteration = 1;
    private long benchmark = 0L;

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

    public long getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(long benchmark) {
        this.benchmark = benchmark;
    }
}
