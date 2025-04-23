package Equals6.Client.Interfaces;

import java.util.List;

import Equals6.Common.Card;

public interface ICardEvents extends IGameEvents {
    void onReceiveCards(List<Card> cards);

    void onDiscardCard(Card card);

    void onReset();
}
