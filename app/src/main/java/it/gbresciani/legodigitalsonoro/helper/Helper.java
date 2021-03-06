package it.gbresciani.legodigitalsonoro.helper;

import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.gbresciani.legodigitalsonoro.model.Syllable;
import it.gbresciani.legodigitalsonoro.model.Word;

public class Helper {

    /**
     * Choose a given number of syllables from the db. It chooses making sure that at least one word can be composed
     * with the choosen syllables.
     *
     * @param num the number of syllables to choose
     */
    public static ArrayList<Syllable> chooseSyllables(int num) {
        ArrayList<Syllable> syllables = new ArrayList<>();
        List<Word> availWords = Word.listAll(Word.class);
        List<Word> usedWords = new ArrayList<>();
        Random rnd = new Random();

        // Get a first random word and removing from the available words
        int rndIndex = rnd.nextInt(availWords.size());
        Word firstRndWord = availWords.get(rndIndex);
        usedWords.add(firstRndWord);
        availWords.remove(rndIndex);
        // Add its syllables to the chosen list and reducing the number to find
        syllables.add(Syllable.find(Syllable.class, "val = ?", firstRndWord.getSyllable1()).get(0));
        syllables.add(Syllable.find(Syllable.class, "val = ?", firstRndWord.getSyllable2()).get(0));
        num -= 2;

        if (num == 0) {
            return syllables;
        }

        Collections.shuffle(syllables);

        for (ListIterator<Syllable> its = syllables.listIterator(); its.hasNext(); ) {
            Syllable s = its.next();
            // Sillable trovate
            if (num == 0) {
                return syllables;
            }
            String query = "SELECT * FROM Word where(syllable1 = ? or syllable2 = ? )";

            // Find words with s syllable
            List<Word> sWords = Word.findWithQuery(Word.class, query, s.getVal(), s.getVal());

            Collections.shuffle(sWords);

            for (int i = 0; i < sWords.size(); i++) {

                Word newWord = sWords.get(i);
                // If the word is new
                if (!usedWords.contains(newWord)) {
                    // Add the new syllables
                    usedWords.add(newWord);
                    Syllable syllable1 = Syllable.find(Syllable.class, "val = ?", newWord.getSyllable1()).get(0);
                    Syllable syllable2 = Syllable.find(Syllable.class, "val = ?", newWord.getSyllable2()).get(0);
                    if (!syllables.contains(syllable1)) {
                        its.add(syllable1);
                        num--;
                        if (num == 0) {
                            return syllables;
                        }
                    }
                    if (!syllables.contains(syllable2)) {
                        its.add(syllable2);
                        num--;
                        if (num == 0) {
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
     * @param syllables The list of Syllables
     * @param k         The length of words (in syllables)
     */
    public static ArrayList<Word> permuteSyllablesInWords(List<Syllable> syllables, int k) {
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
        if (k < syllables.size() - 1) {
            for (ArrayList<Syllable> r : result) {
                r.subList(r.size() - k, r.size()).clear();
            }
        }

        //Removing duplicates
        Set resultSet = new HashSet(result);
        result.clear();
        result.addAll(resultSet);

        ArrayList<Word> resultWords = new ArrayList<>();

        // Transform syllable couples in words
        for (ArrayList<Syllable> r : result) {
            String possibleWord = "";
            for (Syllable s : r) {
                possibleWord += s.getVal();
            }
            List<Word> words = Word.find(Word.class, "lemma = ?", possibleWord);
            if (words.size() > 0) {
                resultWords.add(words.get(0));
            }
        }

        return resultWords;
    }


    /**
     * Calculates the options.inSampleSize to decode the image in a smaller size to prevent java.lang.OutofMemoryError
     *
     * @param options   the options of the bitmap decoder containing height and width calculated with options.inJustDecodeBounds
     * @param reqWidth  the desired image width
     * @param reqHeight the desired image height
     * @return the size of the desired bitmap
     */
    public static int calculateBitmapSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Compare a state1 and state2 and return Words that are in state
     *
     * @param newState
     * @param oldState
     * @return ArrayList of new words
     */
    public static ArrayList<Word> getNewWordInState(GameState newState, GameState oldState) {
        ArrayList<Word> newWordsAvail = new ArrayList<>(newState.getWordsAvailable());
        ArrayList<Word> oldWordsAvail = new ArrayList<>(oldState.getWordsAvailable());

        oldWordsAvail.removeAll(newWordsAvail);

        return oldWordsAvail;
    }


    /**
     * Get a word given its lemma
     *
     * @param word The lemma of the word to find.
     * @return The Word if exists, null if it doesn't
     */
    public static Word wordByLemma(String word) {
        List<Word> wordFound = Word.find(Word.class, "lemma = ?", word);
        if (wordFound.size() > 0) {
            return wordFound.get(0);
        } else {
            return null;
        }
    }

    /**
     * Return if a Word exists, given a lemma
     *
     * @param word The lemma of the word to find.
     * @return The Word if exists, null if it doesn't
     */
    public static boolean wordExistsByLemma(String word) {
        List<Word> wordFound = Word.find(Word.class, "lemma = ?", word);
        return wordFound.size() > 0;
    }

    /**
     * Validate a string as an email
     *
     * @param email
     * @return If is valid
     */
    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
