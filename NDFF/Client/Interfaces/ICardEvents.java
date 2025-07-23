package NDFF.Client.Interfaces;

import java.util.List;

import NDFF.Common.Card;

public interface ICardEvents extends IGameEvents {
    void onCardAdded(Card card);

    void onCardRemoved(Card card);

    void onCardsSync(List<Card> cards);
}
