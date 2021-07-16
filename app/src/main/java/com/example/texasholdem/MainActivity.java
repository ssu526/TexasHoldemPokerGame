package com.example.texasholdem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.texasholdem.CardUtilities.SUIT_SYMBOLS;

public class MainActivity extends AppCompatActivity {
    private static final int NUMBER_OF_CARDS_DEALT = 9;
    private static final int MIN_RAISE_AMOUNT = 1;
    private static final int BEGINNING_BALANCE = 20;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String HIGHEST_SCORE = "highestScore";
    private static final String CURRENT_SCORE = "currentScore";

    private static SharedPreferences sharedPreferences;
    private int computerCard_1, computerCard_2, playerCard_1, playerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard, numberOfChips, raisedAmount, potValue;
    private boolean turnCardHidden, riverCardHidden;

    private TextView txt_computerCard_1, txt_computerCard_2, txt_playerCard_1, txt_playerCard_2, txt_flopCard_1, txt_flopCard_2, txt_flopCard_3, txt_turnCard, txt_riverCard;
    private TextView txt_pot, txt_chips, txt_computer_winner, txt_player_winner, txt_computer_tie, txt_player_tie, txt_highestComputerHand, txt_highestPlayerHand;
    private Button btn_raise, btn_fold, btn_playAgain;
    private ImageView icon_up, icon_down;
    private NumberPicker numberPicker;
    private Runnable runnable;
    private int[] highestComputerHand, highestPlayerHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        numberOfChips=sharedPreferences.getInt(CURRENT_SCORE,BEGINNING_BALANCE);
        initViews();
        dealCards();

        btn_playAgain.setOnClickListener(v -> dealCards());

        btn_fold.setOnClickListener(v -> {
            btn_playAgain.setVisibility(View.VISIBLE);
            btn_fold.setVisibility(View.INVISIBLE);
            btn_raise.setVisibility(View.INVISIBLE);
            updateCurrentScore();
        });
        
        btn_raise.setOnClickListener(this::raiseBet);
    }


    private void dealCards(){
        int[] cardsDealt = new int[NUMBER_OF_CARDS_DEALT];

        Random rand = new Random();
        int cardSelected = rand.nextInt(52)+1;

        for(int i=0;i<NUMBER_OF_CARDS_DEALT;i++){
            while(isDealt(cardSelected, cardsDealt)){
                cardSelected = rand.nextInt(52)+1;
            }
            cardsDealt[i]=cardSelected;
        }

        computerCard_1=cardsDealt[0];
        computerCard_2=cardsDealt[1];
        playerCard_1=cardsDealt[2];
        playerCard_2=cardsDealt[3];
        flopCard_1=cardsDealt[4];
        flopCard_2=cardsDealt[5];
        flopCard_3=cardsDealt[6];
        turnCard=cardsDealt[7];
        riverCard=cardsDealt[8];

        reset();
    }

    // CHECK WHETHER A RANDOMLY SELECTED CARD IS DEALT ALREADY
    private boolean isDealt(int cardSelected, int[] cardsDealt){
        for (int card : cardsDealt) {
            if (cardSelected == card) return true;
        }
        return false;
    }


    private void raiseBet(View v) {
        int previousChipsBalance = numberOfChips;
        int previousPotValue = potValue;
        raisedAmount = numberPicker.getValue();
        numberOfChips = numberOfChips - raisedAmount;
        potValue = potValue + raisedAmount * 2;

        disableButtons();
        runnable = () -> valueAnimation(previousChipsBalance, previousPotValue);

        if (numberOfChips == 0 || !turnCardHidden && riverCardHidden) {
            // If it's an "all-in" or it's the last round of betting, end game and determine the winner
            txt_computerCard_1.setText(getString(R.string.card, CardUtilities.findRank(computerCard_1), SUIT_SYMBOLS[CardUtilities.findSuit(computerCard_1)]));
            txt_computerCard_1.setBackground((AppCompatResources.getDrawable(MainActivity.this, R.drawable.rounded_corner)));
            txt_computerCard_2.setText(getString(R.string.card, CardUtilities.findRank(computerCard_2), SUIT_SYMBOLS[CardUtilities.findSuit(computerCard_2)]));
            txt_computerCard_2.setBackground((AppCompatResources.getDrawable(MainActivity.this, R.drawable.rounded_corner)));
            txt_turnCard.setText(getString(R.string.card, CardUtilities.findRank(turnCard), SUIT_SYMBOLS[CardUtilities.findSuit(turnCard)]));
            txt_turnCard.setBackground((AppCompatResources.getDrawable(MainActivity.this, R.drawable.rounded_corner)));
            txt_riverCard.setText(getString(R.string.card, CardUtilities.findRank(riverCard), SUIT_SYMBOLS[CardUtilities.findSuit(riverCard)]));
            txt_riverCard.setBackground((AppCompatResources.getDrawable(MainActivity.this, R.drawable.rounded_corner)));

            new Handler().postDelayed(runnable, 300);
            new Handler().postDelayed(this::showWinner, 1400);

        } else if (turnCardHidden) {
            txt_turnCard.setText(getString(R.string.card, CardUtilities.findRank(turnCard), SUIT_SYMBOLS[CardUtilities.findSuit(turnCard)]));
            txt_turnCard.setBackground((AppCompatResources.getDrawable(MainActivity.this, R.drawable.rounded_corner)));
            turnCardHidden = false;

            new Handler().postDelayed(runnable, 300);
            new Handler().postDelayed(this::enableButtons, 1400);

            /*Reset NumberPicker for the second bet. The second bet should be equal to or greater than the previous bet.
            If after the first bet, chips left is not enough for the second bet, then the player needs to go all-in in the second bet.*/
            numberPicker.setMinValue(Math.min(numberOfChips, raisedAmount));
            numberPicker.setMaxValue(numberOfChips);
            numberPicker.setValue(numberPicker.getMinValue());
            icon_down.setVisibility(View.INVISIBLE);
            if (numberOfChips <= raisedAmount) icon_up.setVisibility(View.INVISIBLE);
        }
    }

    private int determineWinner(){
        disableButtons();

        //Cards dealt to the computer and player
        int[] computerCards = {computerCard_1, computerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard};
        int[] playerCards = {playerCard_1, playerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard};

        //Determine the strongest hand that the computer has
        List<int[]> computerHandCombinations = new ArrayList<>();
        CardUtilities.generateHandCombinations(computerHandCombinations, computerCards);
        highestComputerHand = CardUtilities.findHighestHand(computerHandCombinations);

        //Determine the strongest hand that the player has
        List<int[]> playerHandCombinations = new ArrayList<>();
        CardUtilities.generateHandCombinations(playerHandCombinations, playerCards);
        highestPlayerHand = CardUtilities.findHighestHand(playerHandCombinations);

        //Return the winner
        return CardUtilities.compareTwoHands(highestComputerHand, highestPlayerHand);
    }


    private void showWinner(){
        //Determine winner, update the chip balance, and show the winner
        int winner = determineWinner();
        int previousChipBalance = numberOfChips;
        int previousPotBalance = potValue;
        potValue=0;

        txt_highestComputerHand.setText(CardUtilities.cardToString(highestComputerHand));
        txt_highestPlayerHand.setText(CardUtilities.cardToString(highestPlayerHand));

        if(winner==0){
            txt_player_tie.setVisibility(View.VISIBLE);
            txt_computer_tie.setVisibility(View.VISIBLE);
            numberOfChips=numberOfChips+previousPotBalance/2;
            runnable = () -> valueAnimation(previousChipBalance, previousPotBalance);
        }else if(winner==1){
            txt_computer_winner.setVisibility(View.VISIBLE);
            runnable = () -> valueAnimation(numberOfChips, previousPotBalance);
        }else{
            txt_player_winner.setVisibility(View.VISIBLE);
            numberOfChips = numberOfChips+ previousPotBalance;
            runnable = () -> valueAnimation(previousChipBalance, previousPotBalance);
        }

        new Handler().postDelayed(runnable, 1000);


        //Update SharedPreferences
        updateCurrentScore();
        if(numberOfChips>sharedPreferences.getInt(HIGHEST_SCORE,0)) updateHighestScore();

        // If the player lost all the chips, reset the number of chips.
        if(numberOfChips==0){
            new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setMessage("You've lost all your chips :( \nNumber of chips will be reset to "+BEGINNING_BALANCE+".")
                    .setPositiveButton("Close", (dialog, which) -> {
                        numberOfChips=BEGINNING_BALANCE;
                        updateCurrentScore();
                        dealCards();
                    })
                    .show();
        }else{
            new Handler().postDelayed(() -> {
                btn_playAgain.setVisibility(View.VISIBLE);
                btn_fold.setVisibility(View.INVISIBLE);
                btn_raise.setVisibility(View.INVISIBLE);
            }, 2200);

            new Handler().postDelayed(this::enableButtons, 2200);
        }
    }

    private void updateCurrentScore(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_SCORE, numberOfChips);
        editor.apply();
    }

    private void updateHighestScore(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HIGHEST_SCORE, numberOfChips);
        editor.apply();
        new AlertDialog.Builder(MainActivity.this).setMessage("GREAT JOB!\nNEW HIGH SCORE: "+numberOfChips).show();
    }


    private void valueAnimation(int previousChipsBalance, int previousPotValue){
        ValueAnimator animatorChips = ValueAnimator.ofInt(previousChipsBalance, numberOfChips);
        ValueAnimator animatorPot = ValueAnimator.ofInt(previousPotValue, potValue);
        animatorChips.setDuration(1000);
        animatorPot.setDuration(1000);

        animatorChips.addUpdateListener(animation -> txt_chips.setText(animation.getAnimatedValue().toString()));
        animatorPot.addUpdateListener(animation -> txt_pot.setText(animation.getAnimatedValue().toString()));
        animatorChips.start();
        animatorPot.start();
    }


    private void disableButtons(){
        btn_raise.setEnabled(false);
        btn_fold.setEnabled(false);

        btn_raise.setTextColor(getColor(R.color.disabledButtonText));
        btn_raise.setBackgroundColor(getColor(R.color.disabledButtonBackground));
        btn_fold.setTextColor(getColor(R.color.disabledButtonText));
        btn_fold.setBackgroundColor(getColor(R.color.disabledButtonBackground));
    }

    private void enableButtons(){
        btn_raise.setEnabled(true);
        btn_fold.setEnabled(true);

        btn_raise.setTextColor(getColor(R.color.white));
        btn_raise.setBackgroundColor(getColor(R.color.green));
        btn_fold.setTextColor(getColor(R.color.white));
        btn_fold.setBackgroundColor(getColor(R.color.red));
    }

    private void initViews(){
        txt_computerCard_1=findViewById(R.id.computerHoleCard1);
        txt_computerCard_2=findViewById(R.id.computerHoleCard2);
        txt_playerCard_1=findViewById(R.id.playerHoleCard1);
        txt_playerCard_2=findViewById(R.id.playerHoleCard2);
        txt_flopCard_1=findViewById(R.id.flop1);
        txt_flopCard_2=findViewById(R.id.flop2);
        txt_flopCard_3=findViewById(R.id.flop3);
        txt_turnCard=findViewById(R.id.turn);
        txt_riverCard=findViewById(R.id.river);
        btn_raise=findViewById(R.id.btn_raise);
        btn_fold=findViewById(R.id.btn_fold);
        btn_playAgain=findViewById(R.id.playAgain);
        txt_computer_winner=findViewById(R.id.txt_winner_computer);
        txt_player_winner=findViewById(R.id.txt_winner_player);
        txt_computer_tie=findViewById(R.id.txt_tie_computer);
        txt_player_tie=findViewById(R.id.txt_tie_player);
        txt_pot=findViewById(R.id.pot);
        txt_chips =findViewById(R.id.txt_chips);
        txt_highestComputerHand=findViewById(R.id.txt_highestComputerHand);
        txt_highestPlayerHand=findViewById(R.id.txt_highestPlayerHand);
        icon_up=findViewById(R.id.icon_up);
        icon_down=findViewById(R.id.icon_down);
        numberPicker=findViewById(R.id.numberPicker);

        int currentScore = sharedPreferences.getInt(CURRENT_SCORE,0);
        txt_chips.setText(String.valueOf(currentScore));

        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if(picker.getValue()==numberPicker.getMinValue()){
                icon_down.setVisibility(View.INVISIBLE);
            }else if(picker.getValue()==numberPicker.getMaxValue()){
                icon_up.setVisibility(View.INVISIBLE);
            }else{
                icon_down.setVisibility(View.VISIBLE);
                icon_up.setVisibility(View.VISIBLE);
            }
        });
    }

    private void reset(){
        raisedAmount =0;
        potValue =0;
        turnCardHidden=true;
        riverCardHidden=true;

        btn_playAgain.setVisibility(View.INVISIBLE);
        txt_computer_winner.setVisibility(View.INVISIBLE);
        txt_player_winner.setVisibility(View.INVISIBLE);
        txt_player_tie.setVisibility(View.INVISIBLE);
        txt_computer_tie.setVisibility(View.INVISIBLE);
        icon_down.setVisibility(View.INVISIBLE);

        icon_up.setVisibility(View.VISIBLE);
        btn_fold.setVisibility(View.VISIBLE);
        btn_raise.setVisibility(View.VISIBLE);
        txt_pot.setText(String.valueOf(potValue));
        txt_chips.setText(String.valueOf(numberOfChips));
        txt_highestComputerHand.setText("");
        txt_highestPlayerHand.setText("");

        txt_playerCard_1.setText(getString(R.string.card, CardUtilities.findRank(playerCard_1), SUIT_SYMBOLS[CardUtilities.findSuit(playerCard_1)]));
        txt_playerCard_2.setText(getString(R.string.card, CardUtilities.findRank(playerCard_2), SUIT_SYMBOLS[CardUtilities.findSuit(playerCard_2)]));
        txt_flopCard_1.setText(getString(R.string.card, CardUtilities.findRank(flopCard_1), SUIT_SYMBOLS[CardUtilities.findSuit(flopCard_1)]));
        txt_flopCard_2.setText(getString(R.string.card, CardUtilities.findRank(flopCard_2), SUIT_SYMBOLS[CardUtilities.findSuit(flopCard_2)]));
        txt_flopCard_3.setText(getString(R.string.card, CardUtilities.findRank(flopCard_3), SUIT_SYMBOLS[CardUtilities.findSuit(flopCard_3)]));
        txt_computerCard_1.setText(getString(R.string.unknown));
        txt_computerCard_2.setText(getString(R.string.unknown));
        txt_turnCard.setText(getString(R.string.unknown));
        txt_riverCard.setText(getString(R.string.unknown));
        txt_computerCard_1.setBackground(AppCompatResources.getDrawable(this, R.drawable.shape));
        txt_computerCard_2.setBackground(AppCompatResources.getDrawable(this, R.drawable.shape));
        txt_turnCard.setBackground(AppCompatResources.getDrawable(this, R.drawable.shape));
        txt_riverCard.setBackground(AppCompatResources.getDrawable(this, R.drawable.shape));

        enableButtons();

        //Reset NumberPicker
        numberPicker.setMinValue(MIN_RAISE_AMOUNT);
        numberPicker.setMaxValue(numberOfChips);
        numberPicker.setValue(MIN_RAISE_AMOUNT);
    }
}
