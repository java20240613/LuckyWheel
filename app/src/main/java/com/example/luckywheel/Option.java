package com.example.luckywheel;

public class Option {

    private String name;
    private int weight;

    public Option(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }
}
