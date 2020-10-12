package io.taiji.wallet.data;

import com.networknt.taiji.utility.Converter;

public class UnitEntry {
    private String name;
    private Converter.Unit unit;
    private String shorty;

    public UnitEntry(String name, Converter.Unit unit, String shorty) {
        this.name = name;
        this.unit = unit;
        this.shorty = shorty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShorty() {
        return shorty;
    }

    public void setShorty(String shorty) {
        this.shorty = shorty;
    }

    public Converter.Unit getUnit() {
        return unit;
    }

    public void setUnit(Converter.Unit unit) {
        this.unit = unit;
    }
}
