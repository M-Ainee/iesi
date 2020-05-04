package io.metadew.iesi.script.execution;

public class LookupResult {

    private String value;
    private String context;
    private String inputValue;

    // Constructors
    public LookupResult() {
    }


    // Getters and setters
    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }


    public String getContext() {
        return context;
    }


    public void setContext(String context) {
        this.context = context;
    }


    public String getInputValue() {
        return inputValue;
    }


    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }


}