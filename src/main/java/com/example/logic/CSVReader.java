package com.example.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CSVReader {
    private String action;
    private ArrayList<String> rules;
    private ArrayList<String> ruleContent;
    private HashMap<String, ArrayList<String>> terminalMap;
    private ArrayList<String> responses;
    private File file;
    private List<String> everything;

    public CSVReader(File file) {
        this.file = file;
        rules = new ArrayList<>();
        responses = new ArrayList<>();
        ruleContent = new ArrayList<>();
        terminalMap = new HashMap<>();
        read();
    }

    public void read() {
        everything = new ArrayList<>();
        HashMap<String, ArrayList<String>> contentMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            everything.add(br.readLine()); // Skip first line
            action = br.readLine().substring(4); // Action is always second line
            everything.add(action);

            String currentLine;
            while ((currentLine = br.readLine()) != null) {

                if (currentLine.startsWith("Rule")) {
                    rules.add(currentLine.substring(4));
                    everything.add(currentLine);
                    readRuleContent(contentMap, currentLine);

                } else if (currentLine.startsWith("Action")) {
                    responses.add(currentLine.substring(6));
                    everything.add(currentLine);
                }
            }
            mergeRuleContent(contentMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mergeRuleContent(HashMap<String, ArrayList<String>> contentMap) {
        Set<String> replacedSlots = new HashSet<>();
        removeTerminals(contentMap);

        for (HashMap.Entry<String, ArrayList<String>> entry : contentMap.entrySet()) {

            ArrayList<String> values = entry.getValue();

            for (String value : values) {

                String[] splitValue = value.split(" ");

                for (int j = 0; j < splitValue.length; j++) {

                    String word = splitValue[j];
                    if (contentMap.containsKey(word)) {

                        ArrayList<String> contentValues = contentMap.get(word);

                        for (String contentValue : contentValues) {
                            splitValue[j] = contentValue;
                            ruleContent.add(String.join(" ", splitValue));
                        }

                        replacedSlots.add(word);
                    } else if (word.contains("<") && word.contains(">")) {
                        ruleContent.add(String.join(" ", splitValue));
                    }
                }
            }
        }


        removeNonTerminalSlots(ruleContent, contentMap);
        removeReplacedSlots(ruleContent, contentMap, replacedSlots);
    }

    private void removeNonTerminalSlots(ArrayList<String> list, HashMap<String, ArrayList<String>> contentMap) {
        for (Map.Entry<String, ArrayList<String>> entry : contentMap.entrySet()) {
            list.removeIf(sentence -> sentence.contains(entry.getKey()));
        }
    }

    private void removeReplacedSlots(ArrayList<String> list, HashMap<String, ArrayList<String>> contentMap, Set<String> replacedSlots) {
        for (String replaced : replacedSlots) {
            ArrayList<String> content = contentMap.get(replaced);
            list.removeAll(content);
        }
    }

    private void removeTerminals(HashMap<String, ArrayList<String>> contentMap) {
        ArrayList<String> terminalKeys = new ArrayList<>();

        contentMap.forEach((key, values) -> {
            if (!(values.get(0).contains("<") && values.get(0).contains(">"))) {
                terminalKeys.add(key);
            }
        });

        terminalKeys.forEach(key -> {
            terminalMap.put(key.toLowerCase(), contentMap.get(key));
            contentMap.remove(key);
        });
    }

    private void readRuleContent(HashMap<String, ArrayList<String>> contentMap, String currentLine) {
        ArrayList<String> content = new ArrayList<>();
        String[] splitCurrentLine = currentLine.split(" ");
        StringBuilder substring = new StringBuilder();
        for (int i = 2; i < splitCurrentLine.length; i++) {
            if (!splitCurrentLine[i].equals("|")) {
                substring.append(splitCurrentLine[i]).append(" ");
            }

            if (splitCurrentLine[i].equals("|")) {
                substring.deleteCharAt(substring.length() - 1);
                content.add(substring.toString());
                substring = new StringBuilder();
            } else if (i == splitCurrentLine.length - 1) {
                substring.deleteCharAt(substring.length() - 1);
                content.add(substring.toString());
                substring = new StringBuilder();
            }
        }

        contentMap.put(splitCurrentLine[1], content);
    }

    public HashMap<String, ArrayList<String>> getTerminalMap() {
        return terminalMap;
    }

    public ArrayList<String> getRuleContent() {
        return ruleContent;
    }

    public String getAction() {
        return action;
    }

    public ArrayList<String> getRules() {
        return rules;
    }

    public ArrayList<String> getResponses() {
        return responses;
    }

    public String[] getEverything() {
        return everything.toArray(new String[0]);
    }
}
