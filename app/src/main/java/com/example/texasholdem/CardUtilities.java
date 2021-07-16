package com.example.texasholdem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CardUtilities {
    public static final int HAND_SIZE = 5;
    public static final String[] SUIT_SYMBOLS = {"\u2667", "\u2662","\u2661","\u2664"};

    private CardUtilities() {
    }

    /** Convert a card from numbers 1-52 to 1-13(Ace to King) **/
    public static int findRank(int card){
        return (card-1)%13+1;
    }

    /** Find the suit of a card (0 is club, 1 is diamond, 2 is heart, 3 is spade) **/
    public static int findSuit(int card){
        return (card-1) /13;
    }

     /** Check if a set of cards is a straight. The input is a sorted set of cards numbered between 1 to 13. **/
    public static boolean checkStraight(int[] sortedHand){
        for(int i=0;i<sortedHand.length-1;i++){
            if(sortedHand[i+1]-sortedHand[i]!=1) return false;
        }
        return true;
    }

     /** Check if a set of cards is a flush. The input is a set of cards numbered between 1 to 52. **/
    public static boolean checkFlush(int[] cards){
        int suit = findSuit(cards[0]);

        for(int i=1;i<cards.length;i++){
            if(findSuit(cards[i])!=suit) return false;
        }
        return true;
    }


     /** Return the ranking of a hand (1-Royal flush, 2-Straight flush, 3-Four of a kind, 4-Full house, 5-Flush, 6-Straight, 7-Three of a kind, 8-Two pairs, 9-Pairs, 10-High card)
      The input is a sorted set of cards numbered between 1 to 13. */
    public static int findHandRanking(int[] sortedHand, Boolean isFlush){
        boolean isStraight = checkStraight(sortedHand);

        // A set of unique cards in the hand
        Set<Integer> set = new HashSet<>();
        for(int card:sortedHand) set.add(card);

        // Check Royal Flush and Straight flush
        if(isFlush && sortedHand[0]==1 && sortedHand[1]==10 && sortedHand[2]==11 && sortedHand[3]==12 && sortedHand[4]==13) return 1;
        if(isFlush && isStraight) return 2;

        // Check Four of A Kind and Full House
        if(set.size()==2){
            if(sortedHand[0]!=sortedHand[1] || sortedHand[3]!=sortedHand[4]) return 3; // Four of a kind
            return 4;  //Full house
        }

        // Check Flush and Straight
        if(isFlush) return 5;
        if(isStraight) return 6;

        // Check Three Of A Kind and Two Pairs
        if(set.size()==3){
            if(sortedHand[0]==sortedHand[2] || sortedHand[1]==sortedHand[3] || sortedHand[2]==sortedHand[4]) return 7; //Three of a kind
            return 8; //Two pairs
        }

        // Check Pair
        if(set.size()==4) return 9;

        // High card
        return  10;
    }


    /** Compare two hands (0-tie, 1-firstHand is stronger, 2-secondHand is stronger)
     The cards in each input hands are numbers from 1 to 52 **/
    public static int compareTwoHands(int[] firstHand, int[] secondHand){
        // Convert the hands from 1-52 to 1-13 and sort both hands
        int[] sortedFirstHand = {findRank(firstHand[0]), findRank(firstHand[1]),findRank(firstHand[2]),findRank(firstHand[3]), findRank(firstHand[4])};
        int[] sortedSecondHand = {findRank(secondHand[0]), findRank(secondHand[1]),findRank(secondHand[2]),findRank(secondHand[3]), findRank(secondHand[4])};
        Arrays.sort(sortedFirstHand);
        Arrays.sort(sortedSecondHand);

        // Find the ranking for each hand
        int firstHandRanking = findHandRanking(sortedFirstHand, checkFlush(firstHand));
        int secondHandRanking = findHandRanking(sortedSecondHand, checkFlush(secondHand));

        // If the hands have different ranking, determine which one is stronger
        // If the hands have the same ranking, determine who has the kicker(next highest card)
        if(firstHandRanking<secondHandRanking) return 1;
        if(firstHandRanking>secondHandRanking) return 2;

        // Both hands are royal flush
        if(firstHandRanking == 1){
            return 0;
        }

        // Both hands are straight flush
        if(firstHandRanking == 2){
            if(sortedFirstHand[0]==sortedSecondHand[0]) return 0;
            if(sortedFirstHand[0]>sortedSecondHand[0]) return 1;
            return 2;
        }

        // Both hands are four of a kind or full house
        if(firstHandRanking == 3 || firstHandRanking == 4){
            if(sortedFirstHand[2]>sortedSecondHand[2]) return 1;
            return 2;
        }

        // Both hands are flush
        if(firstHandRanking == 5){
            for(int i=firstHand.length-1;i>=0;i--){
                if(sortedFirstHand[i]>sortedSecondHand[i]) return 1;
                if(sortedFirstHand[i]<sortedSecondHand[i]) return 2;
            }
            return 0;
        }

        // Both hands are straight
        if(firstHandRanking == 6){
            if(sortedFirstHand[0]==sortedSecondHand[0]) return 0;
            if(sortedFirstHand[0]>sortedSecondHand[0]) return 1;
            return 2;
        }


        // Both hands are three of a kind
        if(firstHandRanking == 7){
            if(sortedFirstHand[2]>sortedSecondHand[2]) return 1;
            if(sortedFirstHand[2]<sortedSecondHand[2]) return 2;
        }

        // Both hands are two pairs
        if(firstHandRanking == 8){
            //Compare second pair
            if(sortedFirstHand[3]>sortedSecondHand[3]) return 1;
            if(sortedFirstHand[3]<sortedSecondHand[3]) return 2;

            //compare first pair
            if(sortedFirstHand[1]>sortedSecondHand[1]) return 1;
            if(sortedFirstHand[1]<sortedSecondHand[1]) return 2;

            //Both hands have the same two pairs; compare the remaining card
            if(sortedFirstHand[4]>sortedSecondHand[4]) return 1;
            if(sortedFirstHand[4]<sortedSecondHand[4]) return 2;
            if(sortedFirstHand[2]>sortedSecondHand[2]) return 1;
            if(sortedFirstHand[2]<sortedSecondHand[2]) return 2;
            if(sortedFirstHand[0]>sortedSecondHand[0]) return 1;
            if(sortedFirstHand[0]<sortedSecondHand[0]) return 2;

            return 0;
        }

        // Both hands have one pair and their pairs are different
        if(firstHandRanking==9){
            // Find the pair in both hands and compare them
            int pair_1 = sortedFirstHand[1];
            if(sortedFirstHand[2]==sortedFirstHand[3] || sortedFirstHand[3]==sortedFirstHand[4]) pair_1 = sortedFirstHand[3];

            int pair_2 = sortedSecondHand[1];
            if(sortedSecondHand[2]==sortedSecondHand[3] || sortedSecondHand[3]==sortedSecondHand[4]) pair_2 = sortedSecondHand[3];

            if(pair_1>pair_2) return 1;
            if(pair_1<pair_2) return 2;
        }

        // Both hands have the same one pair or both are high card
        for(int i=sortedFirstHand.length-1;i>=0;i--){
            if(sortedFirstHand[i]>sortedSecondHand[i]) return 1;
            if(sortedFirstHand[i]<sortedSecondHand[i]) return 2;
        }
        return 0;
    }


    /** Find the hand(1-52) with the highest ranking among a list of hands **/
    public static int[] findHighestHand(List<int[]> cards){
        int[] highest = cards.get(0);

        for(int i=1;i<cards.size();i++){
            int higherHand = CardUtilities.compareTwoHands(highest, cards.get(i));
            if(higherHand==2) highest = cards.get(i);
        }
        return highest;
    }

    /**  Given a set of cards(1-52), generate all the combinations of hands **/
    public static void generateHandCombinations(List<int[]>handCombinations, int[] cards){
        int[] hand = new int[HAND_SIZE];
        combinationUtil(handCombinations, cards, hand, 0, cards.length-1, 0, HAND_SIZE);
    }

    private static void combinationUtil(List<int[]>handCombinations, int[] cards, int[] hand, int start, int end, int index, int r){
        if(index==r){
            int[] newCombination = hand.clone();
            handCombinations.add(newCombination);
        }else{
            for(int i=start; i<=end && end-i+1>=r-index;i++){
                hand[index]=cards[i];
                combinationUtil(handCombinations, cards, hand, i+1, end, index+1, r);
            }
        }
    }

    /** Input is an array of cards(1-52). This method convert each card into its corresponding rank(1-13) and suit. The cards will then be sorted by its rank and the output is a string that concatenates all the cards. **/
    public static String cardToString(int[] hand){
        Map<Integer, List<Integer>> map = new TreeMap<>();
        String str = "";

        for (int j : hand) {
            int computerRank = findRank(j);
            int computerSuit = findSuit(j);

            if (map.get(computerRank) == null) {
                List<Integer> list = new ArrayList<>();
                list.add(computerSuit);
                map.put(computerRank, list);
            } else {
                map.get(computerRank).add(computerSuit);
            }

        }

        for(Map.Entry<Integer, List<Integer>> entry: map.entrySet()){
            for(int i:entry.getValue()){
                str = str+"   "+entry.getKey()+SUIT_SYMBOLS[i];
            }
        }

        return str;
    }
}
