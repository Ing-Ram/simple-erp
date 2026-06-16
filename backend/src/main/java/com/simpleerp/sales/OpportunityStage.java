package com.simpleerp.sales;

/**
 * Pipeline stages of an opportunity.
 *
 * <pre>
 * PROSPECTING → QUALIFIED → PROPOSAL → NEGOTIATION → WON
 *        any open stage ─────────────────────────────→ LOST (requires a reason)
 * </pre>
 *
 * <p>Stages move only forward, except reopening a LOST opportunity back to its previous stage.
 */
public enum OpportunityStage {
    PROSPECTING, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST;

    /** True for the open pipeline stages (everything except the terminal WON / LOST). */
    public boolean isOpen() {
        return this != WON && this != LOST;
    }
}
