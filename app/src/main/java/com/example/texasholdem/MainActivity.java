package com.example.texasholdem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    private static final String[] suitSymbols= {"\n\u2667", "\n\u2662","\n\u2661","\n\u2664"};

    private int computerCard_1, computerCard_2, playerCard_1, playerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard;
    int[] highestComputerHand;
    int[] highestPlayerHand;
    int numberOfChips = BEGINNING_BALANCE;
    int raisedAmount =0;
    int totalBet=0;
    int pot = 0;
    boolean turnCardHidden=true;
    boolean riverCardHidden=true;

    private TextView txt_computerCard_1, txt_computerCard_2, txt_playerCard_1, txt_playerCard_2, txt_flopCard_1, txt_flopCard_2, txt_flopCard_3, txt_turnCard, txt_riverCard;
    private TextView txt_pot, txt_total_bet, txt_chips, txt_computer_winner, txt_player_winner, txt_computer_tie, txt_player_tie;
    private NumberPicker numberPicker;
    private Button btn_raise, btn_fold, btn_playAgain;
    private ImageView icon_up, icon_down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        play();

        btn_raise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                raisedAmount = numberPicker.getValue();
                totalBet = totalBet + raisedAmount;
                pot = pot + raisedAmount * 2;
                numberOfChips=numberOfChips-raisedAmount;

                if(numberOfChips==0){
                    // End game and determine the winner if it's an all-in bet
                    txt_computerCard_1.setText(Card.findRank(computerCard_1)+suitSymbols[Card.findSuit(computerCard_1)]);
                    txt_computerCard_2.setText(Card.findRank(computerCard_2)+suitSymbols[Card.findSuit(computerCard_2)]);
                    txt_turnCard.setText(Card.findRank(turnCard) + suitSymbols[Card.findSuit(turnCard)]);
                    txt_riverCard.setText(Card.findRank(riverCard) + suitSymbols[Card.findSuit(riverCard)]);
                    txt_total_bet.setText(String.valueOf(totalBet));
                    txt_pot.setText(String.valueOf(pot));
                    determineWinner();

                }else if(turnCardHidden) {
                    turnCardHidden = false;
                    txt_turnCard.setText(Card.findRank(turnCard) + suitSymbols[Card.findSuit(turnCard)]);
                    txt_total_bet.setText(String.valueOf(totalBet));
                    txt_pot.setText(String.valueOf(pot));
                    txt_chips.setText(String.valueOf(numberOfChips));

                    //The second bet should be equal to or greater than the previous bet. If after the first bet, chips left is not enough for the second bet, then the player needs to go all-in in the second bet.
                    numberPicker.setMinValue(Math.min(numberOfChips, raisedAmount));
                    numberPicker.setMaxValue(numberOfChips);
                    numberPicker.setValue(numberPicker.getMinValue());
                    icon_down.setVisibility(View.INVISIBLE);
                    if(numberOfChips<=raisedAmount) icon_up.setVisibility(View.INVISIBLE);

                }else if(riverCardHidden){
                    riverCardHidden=false;
                    txt_computerCard_1.setText(Card.findRank(computerCard_1)+suitSymbols[Card.findSuit(computerCard_1)]);
                    txt_computerCard_2.setText(Card.findRank(computerCard_2)+suitSymbols[Card.findSuit(computerCard_2)]);
                    txt_riverCard.setText(Card.findRank(riverCard) + suitSymbols[Card.findSuit(riverCard)]);
                    txt_total_bet.setText(String.valueOf(totalBet));
                    txt_pot.setText(String.valueOf(pot));

                    determineWinner();
                }
            }
        });

        btn_fold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_playAgain.setVisibility(View.VISIBLE);
                btn_fold.setVisibility(View.INVISIBLE);
                btn_raise.setVisibility(View.INVISIBLE);
            }
        });

        btn_playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { play();}
        });
    }

    private void play(){
        // Reset variables and views
        btn_playAgain.setVisibility(View.INVISIBLE);
        txt_computer_winner.setVisibility(View.INVISIBLE);
        txt_player_winner.setVisibility(View.INVISIBLE);
        txt_player_tie.setVisibility(View.INVISIBLE);
        txt_computer_tie.setVisibility(View.INVISIBLE);
        icon_down.setVisibility(View.INVISIBLE);

        btn_fold.setVisibility(View.VISIBLE);
        btn_raise.setVisibility(View.VISIBLE);
        icon_up.setVisibility(View.VISIBLE);

        turnCardHidden=true;
        riverCardHidden=true;
        totalBet=0;
        pot=0;
        raisedAmount =0;

        txt_total_bet.setText(String.valueOf(totalBet));
        txt_pot.setText(String.valueOf(pot));
        txt_chips.setText(String.valueOf(numberOfChips));

        dealCards();

        //Show cards
        txt_playerCard_1.setText(Card.findRank(playerCard_1)+suitSymbols[Card.findSuit(playerCard_1)]);
        txt_playerCard_2.setText(Card.findRank(playerCard_2)+suitSymbols[Card.findSuit(playerCard_2)]);
        txt_flopCard_1.setText(Card.findRank(flopCard_1)+suitSymbols[Card.findSuit(flopCard_1)]);
        txt_flopCard_2.setText(Card.findRank(flopCard_2)+suitSymbols[Card.findSuit(flopCard_2)]);
        txt_flopCard_3.setText(Card.findRank(flopCard_3)+suitSymbols[Card.findSuit(flopCard_3)]);
        txt_computerCard_1.setText("?");
        txt_computerCard_2.setText("?");
        txt_turnCard.setText("?");
        txt_riverCard.setText("?");

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

    // DEAL CARDS
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
    }


    // CHECK WHETHER A RANDOMLY SELECTED CARD IS DEALT ALREADY
    private boolean isDealt(int cardSelected, int[] cardsDealt){
        for (int card : cardsDealt) {
            if (cardSelected == card) return true;
        }
        return false;
    }


    private void determineWinner(){
        btn_playAgain.setVisibility(View.VISIBLE);
        btn_fold.setVisibility(View.INVISIBLE);
        btn_raise.setVisibility(View.INVISIBLE);

        //Cards dealt to the computer and player
        int[] computerCards = {computerCard_1, computerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard};
        int[] playerCards = {playerCard_1, playerCard_2, flopCard_1, flopCard_2, flopCard_3, turnCard, riverCard};

        //Determine the strongest hand that the computer has
        List<int[]> computerHandCombinations = new ArrayList<>();
        Card.generateHandCombinations(computerHandCombinations, computerCards);
        highestComputerHand = Card.findHighestHand(computerHandCombinations);

        //Determine the strongest hand that the player has
        List<int[]> playerHandCombinations = new ArrayList<>();
        Card.generateHandCombinations(playerHandCombinations, playerCards);
        highestPlayerHand = Card.findHighestHand(playerHandCombinations);

        //Determine who has the stronger hand
        int winner = Card.compareTwoHands(highestComputerHand, highestPlayerHand);

        //Display the winner and update chips
        if(winner==0){
            numberOfChips=numberOfChips+totalBet;
            txt_player_tie.setVisibility(View.VISIBLE);
            txt_computer_tie.setVisibility(View.VISIBLE);
            txt_chips.setText(String.valueOf(numberOfChips));
        }else if(winner==1){
            txt_computer_winner.setVisibility(View.VISIBLE);
        }else{
            numberOfChips = numberOfChips+pot;
            txt_player_winner.setVisibility(View.VISIBLE);

            int previousChipBalance = Integer.parseInt(txt_chips.getText().toString());

            ValueAnimator animator = ValueAnimator.ofInt(previousChipBalance, numberOfChips);
            animator.setDuration(2000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    txt_chips.setText(animator.getAnimatedValue().toString());
                    txt_pot.setText(String.valueOf(numberOfChips - Integer.parseInt(animator.getAnimatedValue().toString())));
                }
            });
            animator.start();
        }

        // If the player lost all the chips, reset the number of chips.
        if(numberOfChips==0){
            numberOfChips=BEGINNING_BALANCE;

            AlertDialog dialogBuilder = new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setMessage("You've lost all your chips :( \nNumber of chips will be reset to "+numberOfChips+".")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            play();
                        }
                    })
                    .show();
        }
    }


    private void initViews(){
        //Cards
        txt_computerCard_1=findViewById(R.id.computerHoleCard1);
        txt_computerCard_2=findViewById(R.id.computerHoleCard2);
        txt_playerCard_1=findViewById(R.id.playerHoleCard1);
        txt_playerCard_2=findViewById(R.id.playerHoleCard2);
        txt_flopCard_1=findViewById(R.id.flop1);
        txt_flopCard_2=findViewById(R.id.flop2);
        txt_flopCard_3=findViewById(R.id.flop3);
        txt_turnCard=findViewById(R.id.turn);
        txt_riverCard=findViewById(R.id.river);

        //Buttons
        btn_raise=findViewById(R.id.btn_raise);
        btn_fold=findViewById(R.id.btn_fold);
        btn_playAgain=findViewById(R.id.playAgain);

        //Text
        txt_computer_winner=findViewById(R.id.txt_winner_computer);
        txt_player_winner=findViewById(R.id.txt_winner_player);
        txt_computer_tie=findViewById(R.id.txt_tie_computer);
        txt_player_tie=findViewById(R.id.txt_tie_player);
        txt_pot=findViewById(R.id.pot);
        txt_total_bet=findViewById(R.id.txt_total_bet);
        txt_chips =findViewById(R.id.txt_chips);

        numberPicker=findViewById(R.id.numberPicker);
        icon_up=findViewById(R.id.icon_up);
        icon_down=findViewById(R.id.icon_down);
    }
}
