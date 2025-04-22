package Equals6.Client.Interfaces;

import Equals6.Common.TimerType;

public interface ITimeEvents extends IClientEvents {
    /**
     * The current time of a timer
     * 
     * @param timerType The specifc timer
     * @param time      The time (use -1 to reset/cancel/stop)
     */
    void onTimerUpdate(TimerType timerType, int time);
}