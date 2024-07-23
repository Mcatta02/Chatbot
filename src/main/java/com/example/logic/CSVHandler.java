package com.example.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVHandler{

    public static void main(String[] args) {
        HashMap<String, ArrayList<String>> map = CSVHandler.getRules(new File("Questions/CFG0.csv"));
        System.out.println("Done");
    }

    public static ArrayList<Action> getActions(File fileName){
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
        
        for(String line : lines){
            line = line.toLowerCase();
            String[] words = line.split(" ");
            if(words[0].equalsIgnoreCase("Action")){
                Action action = new Action(words[1]);
                String[] cleanWords = Arrays.copyOfRange(words, 3, words.length);
                int idx = 0;
                while(cleanWords[idx].charAt(0) == '<'){
                    action.addSlot(new String[]{cleanWords[idx], cleanWords[idx+1]});
                    idx += 2;
                }
                action.setAction(String.join(" ", Arrays.copyOfRange(cleanWords, idx, cleanWords.length)));
                result.add(action);
            }
        }
        return result;
    }

    public static HashMap<String, ArrayList<String>> getRules(File fileName){
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
        
        for(String line : lines){
            line = line.toLowerCase();
            String[] words = line.split(" ");
            String cleanLine = line.substring(6+words[1].length(), line.length());
            if(words[0].equalsIgnoreCase("Rule")){
                String[] entries = cleanLine.split("[|]");
                ArrayList<String> newEntries = new ArrayList<String>(Arrays.asList(entries));
                for(int i = 0; i < newEntries.size(); i++){
                    if(newEntries.get(i).charAt(0) == ' '){
                        newEntries.set(i, newEntries.get(i).substring(1, newEntries.get(i).length()));
                    }
                    if(newEntries.get(i).charAt(newEntries.get(i).length()-1) == ' '){
                        newEntries.set(i, newEntries.get(i).substring(0, newEntries.get(i).length()-1));
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
