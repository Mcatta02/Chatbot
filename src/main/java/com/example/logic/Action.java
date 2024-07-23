package com.example.logic;

import java.util.ArrayList;

public class Action {
    private String key;
    private ArrayList<String[]> slots;
    private String action;

    public Action(String key){
        this.key = key;
        this.slots = new ArrayList<String[]>();
    }

    public String getAction(){
        return action;
    }

    public void setAction(String action){
        this.action = action;
    }

    public String getKey(){
        return key;
    }

    public void addSlot(String[] slot){
        slots.add(slot);
    }


    public ArrayList<String[]> getSlots(){
        return slots;
    }

}
