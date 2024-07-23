package com.example.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;


public class Bayes {
    ArrayList<String> prompts;
    ArrayList<String> actions;
    ArrayList<String> classes;
    ArrayList<Double> logPrior;
    ArrayList<ArrayList<Double>> logLikelihood;
    ArrayList<String> vocabulary;

    public Bayes(){
        Dataset data = new Dataset();
        prompts = data.getAllRules();
        actions = data.getAllActions();
        logPrior = new ArrayList<Double>();
        classes = new ArrayList<String>(new HashSet<>(actions));
        logLikelihood = new ArrayList<ArrayList<Double>>();
        HashSet<String> vocab = new HashSet<String>();
        for(int i = 0; i < prompts.size(); i++){
            vocab.addAll(Arrays.asList(prompts.get(i).split("\\s+")));
        }
        vocabulary = new ArrayList<String>(vocab);
        trainBayes();
    }

     /*
    public static void main(String[] args) {
        Bayes ba = new Bayes();
        System.out.println(ba.getPromptAnswer("how is the weather in maastricht"));
    }
*/
   
    public static void main(String[] args) {
        Dataset data = new Dataset();
        ArrayList<String> promptData = data.getAllRules();
        ArrayList<String> actionData = data.getAllActions();

        promptData.add("what lectures do I have on monday at 9");
        actionData.add("We start the week with math");

        promptData.add("what lectures monday at 9");
        actionData.add("We start the week with math");

        promptData.add("what lectures I have monday at 9");
        actionData.add("We start the week with math");

        promptData.add("where spacebox");
        actionData.add("is in the first floor");

        promptData.add("on monday what lectures do i have at 9");
        actionData.add("We start the week with math");

        promptData.add("on monday what lectures do i have at 12");
        actionData.add("On monday noon we have Theoratical Computer S");

        promptData.add("on monday what i have at 12");
        actionData.add("On monday noon we have Theoratical Computer S");

        promptData.add("on saturday what lectures are there");
        actionData.add("There are no lectures on saturday");

        promptData.add("tomorrow in berlin how is the weather going to be");
        actionData.add("It will be sunny.");

        Bayes bayes = new Bayes();
        int count = 0;
        for(int i = 0; i < promptData.size(); i++){
            System.out.println("Iteration: " + i);
            System.out.println("Prompt: " + promptData.get(i));
            System.out.println("Correct Answer: " + actionData.get(i));
            System.out.println("Bayes Answer: " + bayes.getPromptAnswer(promptData.get(i)));
            System.out.println();
            System.out.println();
            if(bayes.getPromptAnswer(promptData.get(i)).equalsIgnoreCase(actionData.get(i))){
                count++;
            }
        }
        System.out.println("Accuracy: " + (double) count/promptData.size());
    }

    public String getPromptAnswer(String prompt){
        ArrayList<Double> sum = new ArrayList<Double>();
        String[] words = prompt.split("\\s+");
        for(int i = 0; i < classes.size(); i++){
            double s = logPrior.get(i);
            for(String word : words){
                if(vocabulary.contains(word)){
                    s += logLikelihood.get(i).get(vocabulary.indexOf(word));
                }
            }
            sum.add(s);
        }
        return classes.get(sum.indexOf(Collections.max(sum)));
    }

    private void trainBayes(){
        for(int i = 0; i < classes.size(); i++){
            double nDoc = prompts.size();
            int nC = Collections.frequency(actions, classes.get(i));
            logPrior.add(Math.log((double)nC/nDoc));

            ArrayList<String> documents = new ArrayList<String>();
            for(int j = 0; j < actions.size(); j++){
                if(actions.get(j).equalsIgnoreCase(classes.get(i))){
                    documents.add(prompts.get(j));
                }
            }

            double countSum = 0;
            for(String word : vocabulary){
                countSum += countOccurences(word, documents);
            }
            ArrayList<Double> wLL = new ArrayList<Double>();
            for(String word : vocabulary){
                double count = countOccurences(word, documents);
                wLL.add(Math.log((count + 10)/(countSum + vocabulary.size() * 10)));
            }
            logLikelihood.add(wLL);
        }

    }

    private double countOccurences(String word, ArrayList<String> strings){
        double count = 0;
        for(String words : strings){
            for(String w : words.split("\\s+")){
                if(w.equalsIgnoreCase(word)){
                    count += 1;
                }
            }
        }
        return count;
    }
}
