package com.ca.mas;

public class ScenarioTestResult {

    private String result ;
    private Double benchmark;
    private Double currentRunTime;
    private String metric= "Time";
    private String unit= "second";
    private int id;
    private String name;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Double getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(Double benchmark) {
        this.benchmark = benchmark;
    }

    public Double getCurrentRunTime() {
        return currentRunTime;
    }

    public void setCurrentRunTime(Double currentRunTime) {
        this.currentRunTime = currentRunTime;
    }

    public int getTestId() {
        return id;
    }

    public void setTestId(int id) {
        this.id = id;
    }

    public String getTestName() {
        return name;
    }

    public void setTestName(String test_name) {
        this.name = test_name;
    }
}
