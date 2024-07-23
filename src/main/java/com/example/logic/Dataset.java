package com.example.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Dataset {

    static HashMap<String, ArrayList<String>> ruleMap = new HashMap<>();
    static ArrayList<Action> actionMap = new ArrayList<>();
    static ArrayList<String> ruleDataset = new ArrayList<>();
    static ArrayList<String> actionDataset = new ArrayList<>();
    static ArrayList<String> finalRules = new ArrayList<>();
    static ArrayList<String> finalActions = new ArrayList<>();

    public Dataset() {
        finalRules = new ArrayList<>();
        finalActions = new ArrayList<>();

        File folder = new File("Questions/");
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            ruleMap = getRules(file);
            actionMap = getActions(file);

            ruleDataset = makeRuleDataset(ruleMap.get("<action>"));
            actionDataset = makeActionDataset(cleanDataset(ruleDataset));

            finalRules.addAll(getRuleDataset());
            finalActions.addAll(getActionDataset());
        }
    }

    public ArrayList<String> getAllRules() {
        return finalRules;
    }

    public ArrayList<String> getAllActions() {
        return finalActions;
    }

    public ArrayList<String> getRuleDataset() {
        return cleanDataset(ruleDataset);
    }

    public ArrayList<String> getActionDataset() {
        return cleanDataset(actionDataset);
    }

    public static ArrayList<String> makeActionDataset(ArrayList<String> ruleSet) {
        ArrayList<String> actionSet = new ArrayList<>();
        CYKHandler handler = new CYKHandler();
        for (int i = 0; i < ruleSet.size(); i++) {
            String action = handler.bayesDataset(ruleSet.get(i));
            if (action == null) {
                ruleDataset.set(i, "");
            } else {
                actionSet.add(handler.bayesDataset(ruleSet.get(i)));
            }
        }
        return actionSet;
    }

    public static ArrayList<String> makeRuleDataset(ArrayList<String> currentOptions) {
        ArrayList<String> returnList = new ArrayList<>();

        for (String currentOption : currentOptions) {
            returnList.add(currentOption);
            String[] splitString = currentOption.split(" ");
            for (String splitWord : splitString) {
                if (splitWord.charAt(0) == '<') {
                    ArrayList<String> options = makeRuleDataset(ruleMap.get(splitWord));

                    // for every item in returnlist
                    for (int i = 0; i < returnList.size(); i++) {

                        // split into seperate words
                        String[] splitList = returnList.get(i).split(" ");

                        // go through all words of entry
                        for (int j = 0; j < splitList.length; j++) {

                            // if word is the terminal for which we found options
                            if (splitList[j].trim().equalsIgnoreCase(splitWord.trim())) {

                                returnList.set(i, "");

                                // go through all options for the terminal
                                for (String option : options) {
                                    if (!(option.trim().equals(""))) {
                                        splitList[j] = option.trim();
                                        StringBuilder updatedItem = new StringBuilder();
                                        for (String item : splitList) {
                                            updatedItem.append(item).append(" ");
                                        }
                                        returnList.add(updatedItem.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return returnList;
    }

    public static ArrayList<String> cleanDataset(ArrayList<String> inputSet) {

        for (int j = 0; j < inputSet.size(); j++) {
            if (inputSet.get(j).startsWith(" ")) {
                inputSet.set(j, inputSet.get(j).substring(1));
            }
        }
        for (int i = 0; i < inputSet.size(); i++) {
            if (inputSet.get(i).trim().equals("")) {
                inputSet.remove(i);
                i--;
            }
        }
        return inputSet;
    }

    public static void tokenize(ArrayList<String> inputData) {
        HashMap<String, Integer> vocabularyIndex = new HashMap<>();
        ArrayList<ArrayList<Integer>> tokenizedData = new ArrayList<>();

        int currentIndex = 0;
        for (String sentence : inputData) {
            String[] tokens = sentence.split("\\s+");
            ArrayList<Integer> indexedTokens = new ArrayList<>();
            for (String token : tokens) {
                if (!vocabularyIndex.containsKey(token)) {
                    vocabularyIndex.put(token, currentIndex);
                    currentIndex++;
                }
                indexedTokens.add(vocabularyIndex.get(token));
            }
            tokenizedData.add(indexedTokens);
        }

    }

    public static ArrayList<Action> getActions(File fileName) {
        ArrayList<Action> result = new ArrayList<Action>();
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            line = line.toLowerCase();
            String[] words = line.split(" ");
            if (words[0].equalsIgnoreCase("Action")) {
                Action action = new Action(words[1]);
                String[] cleanWords = Arrays.copyOfRange(words, 3, words.length);
                int idx = 0;
                while (cleanWords[idx].charAt(0) == '<') {
                    action.addSlot(new String[]{cleanWords[idx], cleanWords[idx + 1]});
                    idx += 2;
                }
                action.setAction(String.join(" ", Arrays.copyOfRange(cleanWords, idx, cleanWords.length)));
                result.add(action);
            }
        }

        return result;
    }

    public static HashMap<String, ArrayList<String>> getRules(File fileName) {
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            line = line.toLowerCase();
            String[] words = line.split(" ");
            String cleanLine = line.substring(6 + words[1].length(), line.length());
            if (words[0].equalsIgnoreCase("Rule")) {
                String[] entries = cleanLine.split("[|]");
                ArrayList<String> newEntries = new ArrayList<String>(Arrays.asList(entries));
                for (int i = 0; i < newEntries.size(); i++) {
                    if (newEntries.get(i).charAt(0) == ' ') {
                        newEntries.set(i, newEntries.get(i).substring(1, newEntries.get(i).length()));
                    }
                    if (newEntries.get(i).charAt(newEntries.get(i).length() - 1) == ' ') {
                        newEntries.set(i, newEntries.get(i).substring(0, newEntries.get(i).length() - 1));
                    }
                }
                result.put(words[1], newEntries);
            }
        }
        return result;
    }

    public static String cleanWord(String input) {
        return input.replaceAll("[^\\p{L}\\p{N}]+", "");
    }
}
