package com.ca.mas;

public class ScenarioMasterInfo {

    String operation_type = "profile";
    int iteration = 1;
    boolean use_default = false;
    int threshold =10;

    public String getOperation_type() {
        return operation_type;
    }

    public void setOperation_type(String operation_type) {
        this.operation_type = operation_type;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public boolean isUse_default() {
        return use_default;
    }

    public void setUse_default(boolean use_default) {
        this.use_default = use_default;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
