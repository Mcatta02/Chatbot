package com.example.logic;

import java.util.*;

public class SpellChecker {
    private Set<String> dictionary;
    private Map<Character, List<Character>> keyboardMap;
    private String userInput;
    private final int distanceThreshold = 4;
    private final int minWordLength = 3;

    public SpellChecker(String userInput) {
        this.userInput = userInput;
        this.dictionary = new HashSet<>();
        this.keyboardMap = new HashMap<>();
        createKeyboardMap();
    }

    public boolean checkSingleWord(String userInput, String terminalInput) {
        userInput = cleanWord(userInput);
        terminalInput = cleanWord(terminalInput);

        if (userInput.length() < 4) {
            return userInput.equalsIgnoreCase(terminalInput);
        }

        int distance = getLevenshteinDistance(userInput.toLowerCase(), terminalInput.toLowerCase());

        return distance < distanceThreshold;
    }

    public boolean isNotCorrectlySpelled(String word) {
        return !dictionary.contains(word.toLowerCase());
    }

    public List<String> getSuggestions(String word) {
        List<String> suggestions = new ArrayList<>();

        // If its a 3 or less letter word or contains numbers dont check
        if (!(word.length() < minWordLength && word.matches("[a-zA-Z]+"))) {

            // Check Levenshtein distance
            int minDistance = Integer.MAX_VALUE;
            for (String dictWord : dictionary) {
                int distance = getLevenshteinDistance(word, dictWord);
                if (distance < minDistance) {
                    minDistance = distance;
                    suggestions.clear();
                    suggestions.add(dictWord);
                } else if (distance == minDistance) {
                    suggestions.add(dictWord);
                }
            }
            if (minDistance > distanceThreshold) {
                suggestions.clear();
            }
//            System.out.println(minDistance);
        }

        return suggestions;
    }

    public ArrayList<String> correctedPrompts() {
        String[] splitInput = userInput.split(" ");
        LinkedHashSet<String> promptList = new LinkedHashSet<>();
        promptList.add(userInput);

        processPrompts(splitInput, promptList, 0, 0);
        ArrayList<String> output = new ArrayList<>(promptList);
        output = removeIncorrectPrompt(output);

        if (promptList.size() == 0) {
            promptList.add(userInput);
        }

        return output;
    }

    private HashSet<String> processPrompts(String[] prompt, LinkedHashSet<String> output, int index, int promptIndex) {
        if (index >= prompt.length) {
            return output;
        } else {
            for (; index < prompt.length; index++) {
                String word = prompt[index];
                if (isNotCorrectlySpelled(word) && word.length() >= minWordLength) {
                    ArrayList<String> currentOut = new ArrayList<>(output);
                    List<String> suggestions = getSuggestions(word);
                    System.out.printf(suggestions.size()+"+");
                    if (promptIndex != 0) {
                        for (int i = currentOut.size() - promptIndex; i < currentOut.size(); i++) {
                            for (String sug : suggestions) {
                                String[] tempOutput = currentOut.get(i).split(" ");
                                tempOutput[index] = sug;
                                output.add(String.join(" ", tempOutput));
                            }
                        }
                    } else {
                        for (String sug : suggestions) {
                            String[] tempOutput = currentOut.get(currentOut.size() - 1).split(" ");
                            tempOutput[index] = sug;
                            output.add(String.join(" ", tempOutput));
                        }
                    }

                    if (suggestions.size() > 1 && promptIndex > 0) {
                        promptIndex = promptIndex * suggestions.size();
                    } else if (suggestions.size() > 1 && promptIndex == 0){
                        promptIndex = promptIndex + suggestions.size();
                    }
                    return processPrompts(prompt, output, ++index, promptIndex); // Recursively process the updated prompt
                }
            }
        }

        return null;
    }


    private ArrayList<String> removeIncorrectPrompt(ArrayList<String> prompt) {
        for (int i = 0; i < prompt.size(); i++) {
            String[] splitPrompt = prompt.get(i).split(" ");
            for (String word : splitPrompt) {
                if (isNotCorrectlySpelled(word) && word.length() >= minWordLength) {
                    prompt.remove(i);
                    i--;
                    break;
                }
            }
        }

        ArrayList<String> duplicatesRemoved = new ArrayList<>();
        HashSet<String> set = new HashSet<>();

        for (String element : prompt) {
            if (set.add(element)) {
                duplicatesRemoved.add(element);
            }
        }

        return duplicatesRemoved;
    }

    public int getLevenshteinDistance(String word, String dictWord) {
        word = word.toLowerCase();
        dictWord = dictWord.toLowerCase();
        int wordLength = word.length();
        int dictLength = dictWord.length();
        int[][] comparisonMatrix = new int[wordLength + 1][dictLength + 1];

        // Initialize comparison matrix
        for (int i = 1; i <= wordLength; i++) {
            comparisonMatrix[i][0] = i;
        }
        for (int j = 1; j <= dictLength; j++) {
            comparisonMatrix[0][j] = j;
        }

        // Fill comparison matrix
        for (int i = 1; i <= wordLength; i++) {
            for (int j = 1; j <= dictLength; j++) {
                // If characters are equal cost = 0, if characters are same symbol
                // check if they are keyboard neighbors if yes cost = 1 if no cost = 2, if they are different forms cost = 3
                int cost;
                if (word.charAt(i - 1) == dictWord.charAt(j - 1)) {
                    cost = 0; // letters are equal
                } else if ((Character.isLetter(word.charAt(i - 1)) && Character.isLetter(dictWord.charAt(j - 1)))
                        || (Character.isDigit(word.charAt(i - 1)) && Character.isDigit(dictWord.charAt(j - 1)))) {
                    // check if they are keyboard neighbors
                    if (keyboardMap.get(dictWord.charAt(j - 1)).contains(word.charAt(i - 1))) {
                        cost = 1; // letters are neighbors
                    } else {
                        cost = 2; // letters are not neighbors
                    }
                } else {
                    cost = 3; // letters are different forms
                }
                // Takes the minimum cost of the left, diagonal left and above cells then add the cost
                comparisonMatrix[i][j] = Math.min(comparisonMatrix[i - 1][j] + cost, Math.min(comparisonMatrix[i][j - 1] + cost, comparisonMatrix[i - 1][j - 1] + cost));
            }
        }

        // Returns final cell
        return comparisonMatrix[wordLength][dictLength];
    }

    private void createKeyboardMap() {
        keyboardMap.put('q', Arrays.asList('w', 'a', 's'));
        keyboardMap.put('w', Arrays.asList('q', 'e', 'a', 's', 'd'));
        keyboardMap.put('e', Arrays.asList('w', 'r', 's', 'd', 'f'));
        keyboardMap.put('r', Arrays.asList('e', 't', 'd', 'f', 'g'));
        keyboardMap.put('t', Arrays.asList('r', 'y', 'f', 'g', 'h'));
        keyboardMap.put('y', Arrays.asList('t', 'u', 'g', 'h', 'j'));
        keyboardMap.put('u', Arrays.asList('y', 'i', 'h', 'j', 'k'));
        keyboardMap.put('i', Arrays.asList('u', 'o', 'j', 'k', 'l'));
        keyboardMap.put('o', Arrays.asList('i', 'p', 'k', 'l'));
        keyboardMap.put('p', Arrays.asList('o', 'l'));
        keyboardMap.put('a', Arrays.asList('q', 'w', 's', 'z', 'x'));
        keyboardMap.put('s', Arrays.asList('q', 'w', 'e', 'a', 'd', 'z', 'x', 'c'));
        keyboardMap.put('d', Arrays.asList('w', 'e', 'r', 's', 'f', 'x', 'c', 'v'));
        keyboardMap.put('f', Arrays.asList('e', 'r', 't', 'd', 'g', 'c', 'v', 'b'));
        keyboardMap.put('g', Arrays.asList('r', 't', 'y', 'f', 'h', 'v', 'b', 'n'));
        keyboardMap.put('h', Arrays.asList('t', 'y', 'u', 'g', 'j', 'b', 'n', 'm'));
        keyboardMap.put('j', Arrays.asList('y', 'u', 'i', 'h', 'k', 'n', 'm'));
        keyboardMap.put('k', Arrays.asList('u', 'i', 'o', 'j', 'l', 'm'));
        keyboardMap.put('l', Arrays.asList('i', 'o', 'p', 'k'));
        keyboardMap.put('z', Arrays.asList('a', 's', 'x'));
        keyboardMap.put('x', Arrays.asList('a', 's', 'd', 'z', 'c'));
        keyboardMap.put('c', Arrays.asList('s', 'd', 'f', 'x', 'v'));
        keyboardMap.put('v', Arrays.asList('d', 'f', 'g', 'c', 'b'));
        keyboardMap.put('b', Arrays.asList('f', 'g', 'h', 'v', 'n'));
        keyboardMap.put('n', Arrays.asList('g', 'h', 'j', 'b', 'm'));
        keyboardMap.put('m', Arrays.asList('h', 'j', 'k', 'n'));
    }

    public String cleanWord(String input) {
        return input.replaceAll("[^\\p{L}\\p{N}]+", "");
    }

    public void generateDictionary(ArrayList<HashMap<String, ArrayList<String>>> rules) {
        for (HashMap<String, ArrayList<String>> rule : rules) {
            for (Map.Entry<String, ArrayList<String>> entry : rule.entrySet()) {
                ArrayList<String> rhs = entry.getValue();
                for (String s : rhs) {
                    if (!s.contains("<") && !s.contains(">") && s.matches("[a-zA-Z]+") && s.length() >= minWordLength) {
                        dictionary.add(s.toLowerCase());
                    }
                }
            }
        }
    }

    private void setDictionary(Set<String> dictionary) {
        this.dictionary = dictionary;
    }

    public static void main(String[] args) {
//        SpellChecker checker = new SpellChecker("Which lectures are there on monday");
//        System.out.println(checker.getLevenshteinDistance("Which lectures are there on", "Which lectures are there on at"));
//        System.out.println(checker.getLevenshteinDistance("Which lectures are there on", "Which lectures are there at on"));
//        System.out.println(checker.getLevenshteinDistance("Which lectures are there on", "at on which lectures do i have"));
//        System.out.println(checker.getLevenshteinDistance("Which lectures are there on", "on at which lectures do i have"));
//        System.out.println();
//        System.out.println(checker.getLevenshteinDistance("on which lectures do have", "Which lectures are there on at"));
//        System.out.println(checker.getLevenshteinDistance("on which lectures do have", "Which lectures are there at on"));
//        System.out.println(checker.getLevenshteinDistance("on which lectures do have", "at on which lectures do i have"));
//        System.out.println(checker.getLevenshteinDistance("on which lectures do have", "on at which lectures do i have"));

        String[] correct = "there are many people who enjoy listening music and playing sports such football basketball and tennis they like outdoors explore nature and adventures they also enjoy socializing with friends and family and having fun their free time".split(" ");
        String misspelled = "ther are any peple who njoy liseng to musik and payng sportes succh as fotball bascketball and tenise they lik to go outors explor natuer and go on adventurs tey aso enjo socializin with frends and famly and hain funm in their fre time";
        String variant = "there are lots humans who like singing to songs while enjoying games such as basketball football plus golf they enjoy to go outside explain things and go on trips those also enjoy talking with family and friends and receiving enjoyment in our off days";

        SpellChecker checker = new SpellChecker(misspelled);
        Set<String> dictionary = new HashSet<>(List.of(correct));
        checker.setDictionary(dictionary);
        ArrayList<String> cor = checker.correctedPrompts();
        System.out.println("mis misspel: " + cor.size());
        System.out.println();

        SpellChecker checker1 = new SpellChecker(variant);
        checker1.setDictionary(dictionary);
        ArrayList<String> cor1 = checker1.correctedPrompts();
        System.out.println("mis variant: " + cor1.size());

//        for (String s : cor) {
//            System.out.println(s);
//        }

    }
}