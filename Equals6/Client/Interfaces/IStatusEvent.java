package Equals6.Client.Interfaces;

public interface IStatusEvent extends IGameEvents {
    void onUpdateAwayStatus(long clientId, boolean isAway);
    void onUpdateSpectatorStatus(long clientId, boolean isSpectator);
    
} 