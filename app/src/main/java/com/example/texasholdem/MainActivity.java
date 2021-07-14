package com.example.texasholdem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
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

public class MainActivity extends AppCompatActivity {
    private static final int NUMBER_OF_CARDS_DEALT = 9;
    private static final int MIN_RAISE_AMOUNT = 1;
    private static final int BEGINNING_BALANCE = 20;
    private static final String[] SUIT_SYMBOLS = {"\n\u2667", "\n\u2662","\n\u2661","\n\u2664"};

    private int computerCard_1, computerCard_2, playerCard_1, playerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard, numberOfChips, raisedAmount, potValue;
    private boolean turnCardHidden, riverCardHidden;

    private TextView txt_computerCard_1, txt_computerCard_2, txt_playerCard_1, txt_playerCard_2, txt_flopCard_1, txt_flopCard_2, txt_flopCard_3, txt_turnCard, txt_riverCard;
    private TextView txt_pot, txt_chips, txt_computer_winner, txt_player_winner, txt_computer_tie, txt_player_tie;
    private Button btn_raise, btn_fold, btn_playAgain;
    private ImageView icon_up, icon_down;
    private NumberPicker numberPicker;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        dealCards();

        btn_raise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousChipsBalance = numberOfChips;
                int previousPotValue = potValue;
                raisedAmount = numberPicker.getValue();
                numberOfChips=numberOfChips-raisedAmount;
                potValue = potValue + raisedAmount * 2;

                disableButtons();
                runnable = () -> valueAnimation(previousChipsBalance, previousPotValue);

                if(numberOfChips==0 || !turnCardHidden && riverCardHidden){
                    // If it's an "all-in" or it's the last round of betting, end game and determine the winner
                    txt_computerCard_1.setText(Card.findRank(computerCard_1)+ SUIT_SYMBOLS[Card.findSuit(computerCard_1)]);
                    txt_computerCard_1.setBackgroundColor(getResources().getColor(R.color.cardBackground));
                    txt_computerCard_2.setText(Card.findRank(computerCard_2)+ SUIT_SYMBOLS[Card.findSuit(computerCard_2)]);
                    txt_computerCard_2.setBackgroundColor(getResources().getColor(R.color.cardBackground));
                    txt_turnCard.setText(Card.findRank(turnCard) + SUIT_SYMBOLS[Card.findSuit(turnCard)]);
                    txt_turnCard.setBackgroundColor(getResources().getColor(R.color.cardBackground));
                    txt_riverCard.setText(Card.findRank(riverCard) + SUIT_SYMBOLS[Card.findSuit(riverCard)]);
                    txt_riverCard.setBackgroundColor(getResources().getColor(R.color.cardBackground));

                    new Handler().postDelayed(runnable, 300);
                    new Handler().postDelayed((Runnable) () -> determineWinner(), 1400);

                }else if(turnCardHidden) {
                    txt_turnCard.setText(Card.findRank(turnCard) + SUIT_SYMBOLS[Card.findSuit(turnCard)]);
                    txt_turnCard.setBackgroundColor(getResources().getColor(R.color.cardBackground));
                    turnCardHidden = false;

                    new Handler().postDelayed(runnable, 300);
                    new Handler().postDelayed((Runnable) () -> enableButtons(), 1400);

                    /*Reset NumberPicker. The second bet should be equal to or greater than the previous bet.
                    If after the first bet, chips left is not enough for the second bet, then the player needs to go all-in in the second bet.*/
                    numberPicker.setMinValue(Math.min(numberOfChips, raisedAmount));
                    numberPicker.setMaxValue(numberOfChips);
                    numberPicker.setValue(numberPicker.getMinValue());
                    icon_down.setVisibility(View.INVISIBLE);
                    if(numberOfChips<=raisedAmount) icon_up.setVisibility(View.INVISIBLE);
                }
            }
        });

        btn_fold.setOnClickListener(v -> {
            btn_playAgain.setVisibility(View.VISIBLE);
            btn_fold.setVisibility(View.INVISIBLE);
            btn_raise.setVisibility(View.INVISIBLE);
        });

        btn_playAgain.setOnClickListener(v -> dealCards());
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

        txt_playerCard_1.setText(Card.findRank(playerCard_1)+ SUIT_SYMBOLS[Card.findSuit(playerCard_1)]);
        txt_playerCard_2.setText(Card.findRank(playerCard_2)+ SUIT_SYMBOLS[Card.findSuit(playerCard_2)]);
        txt_flopCard_1.setText(Card.findRank(flopCard_1)+ SUIT_SYMBOLS[Card.findSuit(flopCard_1)]);
        txt_flopCard_2.setText(Card.findRank(flopCard_2)+ SUIT_SYMBOLS[Card.findSuit(flopCard_2)]);
        txt_flopCard_3.setText(Card.findRank(flopCard_3)+ SUIT_SYMBOLS[Card.findSuit(flopCard_3)]);
        txt_computerCard_1.setText("?");
        txt_computerCard_2.setText("?");
        txt_turnCard.setText("?");
        txt_riverCard.setText("?");
        txt_computerCard_1.setBackground(getResources().getDrawable(R.drawable.shape));
        txt_computerCard_2.setBackground(getResources().getDrawable(R.drawable.shape));
        txt_turnCard.setBackground(getResources().getDrawable(R.drawable.shape));
        txt_riverCard.setBackground(getResources().getDrawable(R.drawable.shape));

        enableButtons();

        //Reset NumberPicker
        numberPicker.setMinValue(MIN_RAISE_AMOUNT);
        numberPicker.setMaxValue(numberOfChips);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(MIN_RAISE_AMOUNT);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(picker.getValue()==numberPicker.getMinValue()){
                    icon_down.setVisibility(View.INVISIBLE);
                }else if(picker.getValue()==numberPicker.getMaxValue()){
                    icon_up.setVisibility(View.INVISIBLE);
                }else{
                    icon_down.setVisibility(View.VISIBLE);
                    icon_up.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void determineWinner(){
        disableButtons();

        //Cards dealt to the computer and player
        int[] computerCards = {computerCard_1, computerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard};
        int[] playerCards = {playerCard_1, playerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard};

        //Determine the strongest hand that the computer has
        List<int[]> computerHandCombinations = new ArrayList<>();
        Card.generateHandCombinations(computerHandCombinations, computerCards);
        int[] highestComputerHand = Card.findHighestHand(computerHandCombinations);

        //Determine the strongest hand that the player has
        List<int[]> playerHandCombinations = new ArrayList<>();
        Card.generateHandCombinations(playerHandCombinations, playerCards);
        int[] highestPlayerHand = Card.findHighestHand(playerHandCombinations);

        //Determine who has the stronger hand
        int winner = Card.compareTwoHands(highestComputerHand, highestPlayerHand);

        //Show winner and update number of chips
        int previousChipBalance = numberOfChips;
        int previousPotBalance = potValue;
        potValue=0;

        if(winner==0){
            txt_player_tie.setVisibility(View.VISIBLE);
            txt_computer_tie.setVisibility(View.VISIBLE);
            numberOfChips=numberOfChips+previousPotBalance/2;
            runnable = () -> valueAnimation(previousChipBalance, previousPotBalance);
        }else if(winner==1){
            txt_computer_winner.setVisibility(View.VISIBLE);
            runnable = () -> valueAnimation(numberOfChips, previousPotBalance);;
        }else{
            txt_player_winner.setVisibility(View.VISIBLE);
            numberOfChips = numberOfChips+ previousPotBalance;
            runnable = () -> valueAnimation(previousChipBalance, previousPotBalance);
        }

        new Handler().postDelayed(runnable, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btn_playAgain.setVisibility(View.VISIBLE);
                btn_fold.setVisibility(View.INVISIBLE);
                btn_raise.setVisibility(View.INVISIBLE);
            }
        }, 2200);

        new Handler().postDelayed((Runnable) () -> enableButtons(), 2200);

        // IF THE PLAYER LOST ALL THE CHIPS, RESET THE NUMBER OF CHIPS
        if(numberOfChips==0){
            numberOfChips=BEGINNING_BALANCE;
            new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setMessage("You've lost all your chips :( \nNumber of chips will be reset to "+numberOfChips+".")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dealCards();
                        }
                    })
                    .show();
        }
    }

    private void valueAnimation(int previousChipsBalance, int previousPotValue){
        ValueAnimator animatorChips = ValueAnimator.ofInt(previousChipsBalance, numberOfChips);
        ValueAnimator animatorPot = ValueAnimator.ofInt(previousPotValue, potValue);
        animatorChips.setDuration(1000);
        animatorPot.setDuration(1000);

        animatorChips.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                txt_chips.setText(animation.getAnimatedValue().toString());
            }
        });

        animatorPot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                txt_pot.setText(animation.getAnimatedValue().toString());
            }
        });

        animatorChips.start();
        animatorPot.start();
    }

    private void disableButtons(){
        btn_raise.setEnabled(false);
        btn_fold.setEnabled(false);
        btn_playAgain.setEnabled(false);

        btn_raise.setTextColor(getResources().getColor(R.color.disabledButtonText));
        btn_fold.setTextColor(getResources().getColor(R.color.disabledButtonText));
        btn_playAgain.setTextColor(getResources().getColor(R.color.disabledButtonText));
    }

    private void enableButtons(){
        btn_raise.setEnabled(true);
        btn_fold.setEnabled(true);
        btn_playAgain.setEnabled(true);

        btn_raise.setTextColor(getResources().getColor(R.color.white));
        btn_fold.setTextColor(getResources().getColor(R.color.white));
        btn_playAgain.setTextColor(getResources().getColor(R.color.playAgainButtonText));
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
        numberPicker=findViewById(R.id.numberPicker);
        icon_up=findViewById(R.id.icon_up);
        icon_down=findViewById(R.id.icon_down);

        numberOfChips = BEGINNING_BALANCE;
    }
}
