package NDFF.Client.Interfaces;

import NDFF.Common.Phase;

public interface IPhaseEvent extends IGameEvents {
    /**
     * Receives the current phase
     * 
     * @param phase
     */
    void onReceivePhase(Phase phase);
}