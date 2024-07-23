package com.example.logic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.File;

public class CYKHandler {

    ArrayList<HashMap<String, ArrayList<String>>> rules;
    ArrayList<ArrayList<Action>> actions;

    public CYKHandler() {
        rules = readRules();
        rules.replaceAll(this::convertToCNF);
        actions = readActions();
    }

    public String retrieveAnswer(String prompt) {
        String output = null;
        prompt = cleanWord(prompt);
        SpellChecker spellChecker = new SpellChecker(prompt);
        spellChecker.generateDictionary(rules);

        ArrayList<String> correctedPrompts = spellChecker.correctedPrompts();

        for (int i = 0; i < rules.size(); i++) {
            for (String curPrompt : correctedPrompts) {
                CYK run = new CYK(rules.get(i), actions.get(i), curPrompt);
                if (run.belongs()) {
                    output = run.getAction();
                }
            }
        }

        if (output != null) {
            return output;
        } else {
            for (String curPrompt : correctedPrompts) {
                String tempRead = read(curPrompt);
                if (isTerminal(tempRead)) {
                    output = tempRead;
                }
            }
        }

        if (output != null) {
            return output;
        } else {
            for (int i = 0; i < rules.size(); i++) {
                for (String curPrompt : correctedPrompts) {
                    CYK run = new CYK(rules.get(i), actions.get(i), read(curPrompt));
                    if (run.belongs()) {
                        output = run.getAction();
                    }
                }
            }
        }

        if (output != null) {
            return output;
        } else {
            for (String curPrompt : correctedPrompts) {
                String tempRead = read(curPrompt);
                if (isTerminal(tempRead)) {
                    output = tempRead;
                }
            }
        }

        if (output == null) {
            return "I dunno :P";
        }

        return output;
    }

    public String bayesDataset(String prompt) {
        prompt = cleanWord(prompt);
        SpellChecker spellChecker = new SpellChecker(prompt);
        spellChecker.generateDictionary(rules);
        for (int i = 0; i < rules.size(); i++) {
            ArrayList<String> correctedPrompts = spellChecker.correctedPrompts();
            for (String curPrompt : correctedPrompts) {
                CYK run = new CYK(rules.get(i), actions.get(i), curPrompt);
                if (run.belongs()) {
                    return run.getAction();
                }
            }
        }
        return null;
    }


    public String retrieveMergedAnswer(String message, String slots) {
        String[] splitSlots = cleanWord(slots).split(" ");
        ArrayList<File> files = getAllQuestions();
        HashMap<String, ArrayList<String>> terminalMap = new HashMap<>();
        int min = Integer.MAX_VALUE;
        String finalContent = "";

        for (File file : files) {
            CSVReader reader = new CSVReader(file);
            ArrayList<String> ruleContent = reader.getRuleContent();

            if (terminalMap.isEmpty()) {
                terminalMap = reader.getTerminalMap();
            }

            for (String content : ruleContent) {
                int compare = comparePrompts(message, content, terminalMap);
//                int compare = checker.getLevenshteinDistance(message, content);
                if (compare < min) {
                    finalContent = content;
                    min = compare;
                    terminalMap = reader.getTerminalMap();
                }
            }
        }

        String mergedPrompt = mergeContent(finalContent, message, splitSlots, terminalMap);

        return retrieveAnswer(mergedPrompt);
    }

    private String mergeContent(String finalContent, String message, String[] slots, HashMap<String, ArrayList<String>> terminalMap) {
        String[] splitFinalContent = finalContent.split(" ");
        HashMap<String, String> existingSlots = getExistingSlotsFromMessage(message, terminalMap);

        for (int i = 0; i < splitFinalContent.length; i++) {
            String word = splitFinalContent[i];
            if (isTerminal(word)) {
                String oldSlot = existingSlots.get(word.toLowerCase());
                if (oldSlot != null) {
                    splitFinalContent[i] = oldSlot.replaceAll("\\s", "");
                } else {
                    for (String slot : slots) {
                        HashMap<String, String> newSlots = getExistingSlotsFromMessage(slot, terminalMap);
                        String newSlot = newSlots.get(word.toLowerCase());

                        if (newSlot != null) {
                            splitFinalContent[i] = newSlot.replaceAll("\\s", "");
                        }
                    }
                }
            }
        }

        return String.join(" ", splitFinalContent);
    }

    private HashMap<String, String> getExistingSlotsFromMessage(String message, HashMap<String, ArrayList<String>> terminalMap) {
        HashMap<String, String> output = new HashMap<>();

        for (Map.Entry<String, ArrayList<String>> entry : terminalMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> terminals = entry.getValue();

            for (String word : terminals) {
                if (isTerminalInString(message, word)) {
                    output.put(key, word);
                }
            }
        }

        return output;
    }

    private String read(String prompt) {
        StringBuilder output = new StringBuilder();
        output.append("Can you also give me the");
        ArrayList<File> files = getAllQuestions();
        HashMap<String, ArrayList<String>> finalTerminalMap = new HashMap<>();
        int min = Integer.MAX_VALUE;
        String finalPrompt = "";
        String finalContent = "";
        int counter = 0;

        for (File file : files) {
            CSVReader reader = new CSVReader(file);
            ArrayList<String> ruleContent = reader.getRuleContent();
            HashMap<String, ArrayList<String>> terminalMap = reader.getTerminalMap();

            for (String content : ruleContent) {

                int compare = comparePrompts(prompt, content, terminalMap);
                if (compare < min) {
                    finalContent = content;
                    finalPrompt = prompt;
                    min = compare;
                    finalTerminalMap = reader.getTerminalMap();
                }
            }
        }

        ArrayList<String> missingSlots = extractSlots(finalPrompt, finalContent, finalTerminalMap);
        if (missingSlots != null) {
            for (String slot : missingSlots) {
                output.append(", ").append(slot);
            }

        } else {
            return mergeContent(finalContent, finalPrompt, finalPrompt.split(" "), finalTerminalMap);
        }

        return output.toString();
    }

    private ArrayList<String> extractSlots(String prompt, String content, HashMap<String, ArrayList<String>> terminalMap) {
        ArrayList<String> slots = new ArrayList<>();
        String[] splitContent = content.toLowerCase().split(" ");

        for (String con : splitContent) {
            if (isTerminal(con)) {
                slots.add(con);
            }
        }

        Iterator<String> iterator = slots.iterator();
        while (iterator.hasNext()) {
            String slot = iterator.next();
            ArrayList<String> currentTerminal = terminalMap.get(slot);
            if (currentTerminal != null) {
                for (String ter : currentTerminal) {
                    if (isTerminalInString(prompt, ter)) {
                        iterator.remove();
                        break; // Exit the inner loop once a match is found
                    }
                }
            }
            if (slots.isEmpty()) {
                return null;
            }
        }

        return slots;
    }

    private int comparePrompts(String prompt, String content, HashMap<String, ArrayList<String>> terminalMap) {
        SpellChecker checker = new SpellChecker("");
        String[] splitPrompt = prompt.toLowerCase().split(" ");
        String[] splitContent = content.toLowerCase().split(" ");
        ArrayList<String> prunedContent = new ArrayList<>();
        ArrayList<String> prunedPrompt = new ArrayList<>(Arrays.asList(splitPrompt));

        for (String word : splitContent) {
            if (!isTerminal(word)) {
                prunedContent.add(word);
            }
        }

        for (Map.Entry<String, ArrayList<String>> entry : terminalMap.entrySet()) {
            ArrayList<String> terminals = entry.getValue();
            for (String word : terminals) {
                if (isTerminalInString(prompt, word)) {
                    prunedPrompt.remove(word.toLowerCase());
                }
            }
        }

        String finalContent = String.join(" ", prunedContent);
        String finalPrompt = String.join(" ", prunedPrompt);

        return checker.getLevenshteinDistance(finalPrompt, finalContent);
    }

    private ArrayList<File> getAllQuestions() {
        String folderPath = "Questions";
        ArrayList<File> allFiles = new ArrayList<>();

        // Get a list of all the files in the folder
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();

        // Loop over each file in the folder and read the first line
        assert fileList != null;
        for (File file : fileList) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                allFiles.add(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return allFiles;
    }

    private boolean isTerminalInString(String prompt, String terminal) {
        String cleanTerminal = terminal.replaceAll("[^\\p{L}\\p{N}]+", "").toLowerCase();
        String[] splitPrompt = prompt.toLowerCase().split(" ");

        for (String word : splitPrompt) {
            if (word.equalsIgnoreCase(cleanTerminal)) {
                return true;
            }
        }
        return false;
    }

    private String cleanWord(String input) {
        return input.replaceAll("[^\\p{L}\\p{N}\\s]+", "");
    }

    private HashMap<String, ArrayList<String>> convertToCNF(HashMap<String, ArrayList<String>> rules) {
        HashMap<String, ArrayList<String>> newRules = new HashMap<String, ArrayList<String>>(rules);

        // START 
        //newRules.put("<start>", new ArrayList<String>(List.of("<s>")));

        //TERM
        HashMap<String, ArrayList<String>> placeHolder = new HashMap<String, ArrayList<String>>();
        HashMap<String, String> newNTs = new HashMap<String, String>();
        for (Map.Entry<String, ArrayList<String>> entry : newRules.entrySet()) {
            ArrayList<String> rhs = entry.getValue();
            for (int i = 0; i < rhs.size(); i++) {
                String[] w = rhs.get(i).split(" ");
                ArrayList<String> words = new ArrayList<String>(Arrays.asList(w));
                for (int j = 0; j < words.size(); j++) {
                    if (!(words.get(j).charAt(0) == '<') && words.size() > 1) {
                        if (newNTs.get(words.get(j)) == null) {
                            newNTs.put(words.get(j), "<" + words.get(j) + "T>");
                            placeHolder.put("<" + words.get(j) + "T>", new ArrayList<String>(List.of(words.get(j))));
                            words.set(j, "<" + words.get(j) + "T>");

                        } else {
                            words.set(j, newNTs.get(words.get(j)));
                        }
                    }
                }
                w = words.toArray(new String[words.size()]);
                rhs.set(i, String.join(" ", w));
            }
            entry.setValue(rhs);
        }
        newRules.putAll(placeHolder);

        // BIN
        placeHolder = new HashMap<String, ArrayList<String>>();
        int idx = 0;
        for (Map.Entry<String, ArrayList<String>> entry : newRules.entrySet()) {
            ArrayList<String> rhs = entry.getValue();
            for (int i = 0; i < rhs.size(); i++) {
                String[] w = rhs.get(i).split(" ");
                ArrayList<String> words = new ArrayList<String>(Arrays.asList(w));
                if (words.size() > 2) {
                    ArrayList<String> copy = new ArrayList<String>(words);

                    words.subList(1, words.size()).clear();
                    words.add("<" + words.get(0).substring(1, words.get(0).length() - 1) + String.valueOf(idx) + String.valueOf(i) + "0EXT>");
                    w = words.toArray(new String[words.size()]);
                    rhs.set(i, String.join(" ", w));

                    for (int j = 1; j < copy.size() - 2; j++) {
                        ArrayList<String> newRHS = new ArrayList<String>();
                        newRHS.add(copy.get(j) + " <" + copy.get(j).substring(1, copy.get(j).length() - 1) + String.valueOf(idx) + String.valueOf(i) + String.valueOf(j) + "EXT>");
                        placeHolder.put("<" + copy.get(j - 1).substring(1, copy.get(j - 1).length() - 1) + String.valueOf(idx) + String.valueOf(i) + String.valueOf(j - 1) + "EXT>", newRHS);
                    }

                    ArrayList<String> newRHS = new ArrayList<String>(List.of(copy.get(copy.size() - 2) + " " + copy.get(copy.size() - 1)));
                    placeHolder.put("<" + copy.get(copy.size() - 3).substring(1, copy.get(copy.size() - 3).length() - 1) + String.valueOf(idx) + String.valueOf(i) + String.valueOf(copy.size() - 3) + "EXT>", newRHS);
                }
            }
            entry.setValue(rhs);
            idx++;
        }
        newRules.putAll(placeHolder);

        // UNIT 
        boolean foundUnitRule;
        do {
            foundUnitRule = false;
            for (Map.Entry<String, ArrayList<String>> entry : newRules.entrySet()) {
                ArrayList<String> rhs = entry.getValue();
                for (int i = 0; i < rhs.size(); i++) {
                    String[] w = rhs.get(i).split(" ");
                    ArrayList<String> words = new ArrayList<String>(Arrays.asList(w));
                    if (words.size() == 1 && words.get(0).charAt(0) == '<') {
                        ArrayList<String> substitute = newRules.get(words.get(0));
                        rhs.remove(i);
                        rhs.addAll(substitute);
                        foundUnitRule = true;
                    }
                }
                entry.setValue(rhs);
            }
        } while (foundUnitRule);
        return newRules;
    }

    private ArrayList<HashMap<String, ArrayList<String>>> readRules() {
        ArrayList<HashMap<String, ArrayList<String>>> result = new ArrayList<HashMap<String, ArrayList<String>>>();
        File folder = new File("Questions/");
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            result.add(CSVHandler.getRules(file));
        }
        return result;
    }

    private ArrayList<ArrayList<Action>> readActions() {
        ArrayList<ArrayList<Action>> result = new ArrayList<ArrayList<Action>>();
        File folder = new File("Questions/");
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            result.add(CSVHandler.getActions(file));
        }
        return result;
    }

    private boolean isTerminal(String word) {
        return word.contains("<") && word.contains(">");
    }


}
