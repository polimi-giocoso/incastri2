package it.gbresciani.poligame.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import it.gbresciani.poligame.model.Syllable;
import it.gbresciani.poligame.model.Word;

public class Helper {

    /**
     * Choose a given number of syllables from the db. It chooses making sure that at least one word can be composed
     * with the choosen syllables.
     *
     * @param num the number of syllables to choose
     */
    public static List<Syllable> chooseSyllables(int num){
        List<Syllable> syllables = new ArrayList<>();
        List<Word> availWords = Word.listAll(Word.class);
        List<Word> usedWords = new ArrayList<>();
        Random rnd = new Random();

        // Get a first random word and removing from the available words
        int rndIndex = rnd.nextInt(availWords.size());
        Word firstRndWord = availWords.get(rndIndex);
        usedWords.add(firstRndWord);
        availWords.remove(rndIndex);
        // Add its syllables to the chosen list and reducing the number to find
        syllables.add(new Syllable(firstRndWord.getSyllable1()));
        syllables.add(new Syllable(firstRndWord.getSyllable2()));
        num -= 2;

        if(num == 0){
            return syllables;
        }

        for(Iterator<Syllable> its = syllables.iterator(); its.hasNext(); ){
            Syllable s = its.next();
            // Sillable trovate
            if(num == 0){
                return syllables;
            }
            String query =  "SELECT * FROM Word where(syllable1 = ? or syllable2 = ? )";

            // Find words with s syllable
            List<Word> sWords = Word.findWithQuery(Word.class, query, s.getVal(), s.getVal());

            for (int i = 0; i < sWords.size(); i++) {

                Word newWord = sWords.get(i);
                // If the word is new
                if(!usedWords.contains(newWord)){
                    // Add the new syllables
                    usedWords.add(newWord);
                    Syllable syllable1 = new Syllable(newWord.getSyllable1());
                    Syllable syllable2 = new Syllable(newWord.getSyllable2());
                    if(!syllables.contains(syllable1)){
                        syllables.add(syllable1);
                        num--;
                        if(num == 0){
                            return syllables;
                        }
                    }
                    if(!syllables.contains(syllable2)){
                        syllables.add(syllable2);
                        num--;
                        if(num == 0){
                            return syllables;
                        }
                    }
                }
            }
        }

        return syllables;
    }


    /**
     * Calculate the possible Words coming from the permutation of length k of a given list of Syllables
     *
     * @param syllables   The list of Syllables
     * @param k The length of words (in syllables)
     */
    public static List<Word> permuteSyllablesInWords(List<Syllable> syllables, int k) {
        ArrayList<ArrayList<Syllable>> result = new ArrayList<>();

        // Start from an empty list
        result.add(new ArrayList<Syllable>());

        for (int i = 0; i < syllables.size(); i++) {
            // List of list in current iteration of the array num
            List<ArrayList<Syllable>> current = new ArrayList<>();

            for (ArrayList<Syllable> l : result) {
                // # of locations to insert is largest index + 1
                for (int j = 0; j < l.size() + 1; j++) {
                    // + add num[i] to different locations
                    l.add(j, syllables.get(i));

                    ArrayList<Syllable> temp = new ArrayList<Syllable>(l);
                    current.add(temp);

                    // - remove num[i] add
                    l.remove(j);
                }
            }

            result = new ArrayList<ArrayList<Syllable>>(current);
        }

        // If k < n-1 remove n - k elements from each permutation to determine the ordered selection
        if(k < syllables.size() - 1) {
            for (ArrayList<Syllable> r : result) {
                r.subList(r.size() - k, r.size()).clear();
            }
        }

        //Removing duplicates
        Set resultSet = new HashSet(result);
        result.clear();
        result.addAll(resultSet);

        List<Word> resultWords = new ArrayList<>();

        // Transform syllable couples in words
        for (ArrayList<Syllable> r : result){
            String possibleWord = "";
            for (Syllable s : r){
                possibleWord += s.getVal();
            }
            List<Word> words = Word.find(Word.class, "lemma = ?", possibleWord);
            if(words.size() > 0){
                resultWords.add(words.get(0));
            }
        }

        return resultWords;
    }
}