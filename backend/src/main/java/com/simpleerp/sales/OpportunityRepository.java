package com.simpleerp.sales;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for opportunities, including the pipeline dashboard aggregations. */
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /**
     * Σ(expected value × probability) over open opportunities, scaled by 100. The service divides by
     * 100 to get the weighted pipeline value — kept in SQL as BigDecimal to avoid float rounding.
     */
    @Query("""
            select coalesce(sum(o.expectedValue.amount * o.probability), 0)
            from Opportunity o
            where o.stage in (com.simpleerp.sales.OpportunityStage.PROSPECTING,
                              com.simpleerp.sales.OpportunityStage.QUALIFIED,
                              com.simpleerp.sales.OpportunityStage.PROPOSAL,
                              com.simpleerp.sales.OpportunityStage.NEGOTIATION)
            """)
    BigDecimal weightedPipelineTimes100();

    /** Count and value of open opportunities per stage — the funnel. */
    @Query("""
            select new com.simpleerp.sales.StageFunnel(o.stage, count(o), coalesce(sum(o.expectedValue.amount), 0))
            from Opportunity o
            where o.stage in (com.simpleerp.sales.OpportunityStage.PROSPECTING,
                              com.simpleerp.sales.OpportunityStage.QUALIFIED,
                              com.simpleerp.sales.OpportunityStage.PROPOSAL,
                              com.simpleerp.sales.OpportunityStage.NEGOTIATION)
            group by o.stage
            """)
    List<StageFunnel> funnel();

    /** Total value of opportunities won on or after the given date (e.g. quarter start). */
    @Query("""
            select coalesce(sum(o.expectedValue.amount), 0)
            from Opportunity o
            where o.stage = com.simpleerp.sales.OpportunityStage.WON and o.closedDate >= :since
            """)
    BigDecimal wonAmountSince(@Param("since") LocalDate since);

    /** Total value of all won opportunities — paired with the won count to get average deal size. */
    @Query("""
            select coalesce(sum(o.expectedValue.amount), 0)
            from Opportunity o
            where o.stage = com.simpleerp.sales.OpportunityStage.WON
            """)
    BigDecimal totalWonValue();

    /** Won opportunities closed on or after a date, for trailing monthly-won revenue. */
    @Query("""
            select new com.simpleerp.sales.WonAmount(o.closedDate, o.expectedValue.amount)
            from Opportunity o
            where o.stage = com.simpleerp.sales.OpportunityStage.WON and o.closedDate >= :since
            """)
    List<WonAmount> wonSince(@Param("since") LocalDate since);

    /** Count of opportunities in a stage decided on or after a date — drives win rate. */
    long countByStageAndClosedDateGreaterThanEqual(OpportunityStage stage, LocalDate since);

    /** Count of opportunities in a stage (e.g. WON for average deal size). */
    long countByStage(OpportunityStage stage);

    /** Open opportunities past their expected close date — money sitting still. */
    List<Opportunity> findByStageInAndExpectedCloseDateBefore(List<OpportunityStage> stages, LocalDate date);

    /** Closed deals (won and lost), most recently closed first. */
    List<Opportunity> findByStageInOrderByClosedDateDesc(List<OpportunityStage> stages);

    /** One salesperson's closed deals, most recently closed first. */
    List<Opportunity> findByStageInAndOwnerEmployeeIdOrderByClosedDateDesc(
            List<OpportunityStage> stages, Long ownerEmployeeId);

    /**
     * Per-salesperson rollup in one grouped query: won count and value, lost count, and open
     * weighted pipeline (×100). Conditional aggregation keeps it to a single pass over the table.
     */
    @Query("""
            select new com.simpleerp.sales.RepRollupRow(
                o.ownerEmployeeId,
                sum(case when o.stage = com.simpleerp.sales.OpportunityStage.WON then 1L else 0L end),
                coalesce(sum(case when o.stage = com.simpleerp.sales.OpportunityStage.WON
                                  then o.expectedValue.amount else 0 end), 0),
                sum(case when o.stage = com.simpleerp.sales.OpportunityStage.LOST then 1L else 0L end),
                coalesce(sum(case when o.stage in (com.simpleerp.sales.OpportunityStage.PROSPECTING,
                                                   com.simpleerp.sales.OpportunityStage.QUALIFIED,
                                                   com.simpleerp.sales.OpportunityStage.PROPOSAL,
                                                   com.simpleerp.sales.OpportunityStage.NEGOTIATION)
                                  then o.expectedValue.amount * o.probability else 0 end), 0))
            from Opportunity o
            group by o.ownerEmployeeId
            """)
    List<RepRollupRow> repRollup();
}

