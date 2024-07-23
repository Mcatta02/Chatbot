package com.example.logic;

import java.util.*;

public class CYK {

    boolean[][][] P;
    ArrayList<int[]>[][][] back;
    HashMap<String, ArrayList<String>> rules;
    final String[] words;
    private boolean belongs;
    private ArrayList<String[]> labels;
    private ArrayList<Action> actions;

    public CYK(HashMap<String, ArrayList<String>> rules, ArrayList<Action> actions, String sentence){
        this.rules = rules;
        this.actions = actions;
        this.words = sentence.split(" ");
        this.P = new boolean[words.length][words.length][rules.size()];
        this.back = new ArrayList[words.length][words.length][rules.size()];
        for(int i = 0; i < back.length; i++){
            for(int j = 0; j < back[0].length; j++){
                for(int k = 0; k < back[0][0].length; k++){
                    back[i][j][k] = new ArrayList<int[]>();
                }
            }
        }
        runCYK();
        this.belongs = P[P.length-1][0][indexOfNT("<s>")];
        if(belongs){
            this.labels = getLabels(words.length-1, 0, indexOfNT("<s>"));
        }
    }


    public boolean belongs(){
        return belongs;
    }

    private void runCYK(){
        for(int i = 0; i < P.length; i++){
            int j = 0;
            for(Map.Entry<String, ArrayList<String>> entry : rules.entrySet()){
                ArrayList<String> rhs = entry.getValue();
                for(String s : rhs){
                    if (words[i].equalsIgnoreCase(s)){
                        P[0][i][j] = true;
                    }
                }
                j++;
            }
        }

        for(int l = 1; l < P.length; l++){
            for(int s = 0; s < P.length-l; s++){
                for(int p = 0; p < l; p++){
                    for(Map.Entry<String, ArrayList<String>> entry : rules.entrySet()){
                        ArrayList<String> rhs = entry.getValue();
                        for(String i : rhs){
                            String[] ruleWords = i.split(" ");
                            if(ruleWords.length == 2){
                                if(P[p][s][indexOfNT(ruleWords[0])] && P[l-(p+1)][s+p+1][indexOfNT(ruleWords[1])]){
                                    P[l][s][indexOfNT(entry.getKey())] = true;
                                    int[] backTriplet = {p, indexOfNT(ruleWords[0]), indexOfNT(ruleWords[1])};
                                    back[l][s][indexOfNT(entry.getKey())].add(backTriplet);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private ArrayList<String[]> getLabels (int row, int col, int rule){
        ArrayList<String[]> result = new ArrayList<String[]>();
        if(row == 0){
            Set<String> keySet = rules.keySet();
            List<String> keyList = new ArrayList<>(keySet);
            result.add(new String[]{keyList.get(rule), words[col]});
            return result;
        }
        int[] node = back[row][col][rule].get(0);
        result.addAll(getLabels(node[0], col, node[1]));
        result.addAll(getLabels(row-(node[0]+1), col+node[0]+1, node[2]));
        return result;
    }

    private String getKey(int ruleIndexOne, int ruleIndexTwo){
        Set<String> keySet = rules.keySet();
        List<String> keyList = new ArrayList<>(keySet);
        String value = String.join(" ", new String[]{keyList.get(ruleIndexOne), keyList.get(ruleIndexTwo)});
        for(Map.Entry<String, ArrayList<String>> entry : rules.entrySet()){
            if(!entry.getKey().equalsIgnoreCase("<s>") && !entry.getKey().equalsIgnoreCase("<action>")){
                for(String s : entry.getValue()){
                    if(s.equalsIgnoreCase(value)){
                        return entry.getKey();
                    }
                }
            }
        }
        return "";
    }


    public String getAction(){
        String key = getKey(back[words.length-1][0][indexOfNT("<s>")].get(0)[1], back[words.length-1][0][indexOfNT("<s>")].get(0)[2]);
        ArrayList<String[]> slots = extractSlots();
        for(Action action : actions){
            if(key.equalsIgnoreCase(action.getKey()) && slotsMatch(slots, action.getSlots())){
                return action.getAction();
            }
        }
        return null;
    }

    private boolean slotsMatch(ArrayList<String[]> cykSlots, ArrayList<String[]> skillSlots){
        if(cykSlots.size() != skillSlots.size()){
            return false;
        }
        for(String[] cykPair : cykSlots){
            boolean slotFound = false;
            for(String[] skillPair : skillSlots){
                if(skillPair[1].equalsIgnoreCase(cykPair[1])){
                    slotFound = true;
                    break;
                }
            }
            if(!slotFound){
                return false;
            }
        }
        return true;
    }

    private ArrayList<String[]> extractSlots(){
        ArrayList<String[]> result = new ArrayList<String[]>();
        for(String[] pair : labels){
            if(!pair[0].substring(pair[0].length()-2, pair[0].length()-1).equals("T") && !isExtension(pair[0])){
                result.add(pair);
            }
        }
        return result;
    }

    private boolean isExtension(String slot){
        if(slot.length() < 6){
            return false;
        }
        if(slot.substring(slot.length()-4, slot.length()-1).equals("EXT")){
            return true;
        }
        return false;
    }

    private int indexOfNT(String nonTerminal){
        int index = 0;
        for (String key : rules.keySet()) {
            if (key.equalsIgnoreCase(nonTerminal)) {
                return index;
            }
            index++;
        }
        return -1;
    }

}
