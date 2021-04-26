package uk.gov.pay.ledger.transactionsummary.dao;

import com.google.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.ledger.transaction.state.TransactionState;

import java.time.ZonedDateTime;

public class TransactionSummaryDao {

    private static final String UPSERT_STRING = "INSERT INTO transaction_summary AS ts(gateway_account_id, type, " +
            " transaction_date, state, live, moto, total_amount_in_pence, no_of_transactions, total_fee_in_pence)" +
            " VALUES(:gatewayAccountId, :type, :transactionDate, :state, :live, :moto, :amountInPence, 1,0)" +
            " ON CONFLICT ON CONSTRAINT transaction_summmary_unique_key " +
            " DO UPDATE SET no_of_transactions = ts.no_of_transactions + 1, " +
            " total_amount_in_pence = :amountInPence + ts.total_amount_in_pence" +
            " WHERE ts.gateway_account_id = :gatewayAccountId " +
            " AND ts.transaction_date = :transactionDate " +
            " AND ts.state = :state " +
            " AND ts.type = :type" +
            " AND ts.live = :live" +
            " AND ts.moto = :moto";

    private static final String DEDUCT_TRANSACTION_SUMMARY = "UPDATE transaction_summary ts " +
            " SET no_of_transactions = ts.no_of_transactions - 1, " +
            " total_amount_in_pence = ts.total_amount_in_pence - :amountInPence, " +
            " total_fee_in_pence = ts.total_fee_in_pence - :feeInPence " +
            " WHERE ts.gateway_account_id = :gatewayAccountId " +
            " AND ts.transaction_date = :transactionDate " +
            " AND ts.state = :state " +
            " AND ts.type = :type" +
            " AND ts.live = :live" +
            " AND ts.moto = :moto";

    private static final String UPDATE_FEE = "UPDATE transaction_summary ts " +
            " SET total_fee_in_pence = ts.total_fee_in_pence + :feeInPence " +
            " WHERE ts.gateway_account_id = :gatewayAccountId " +
            " AND ts.transaction_date = :transactionDate " +
            " AND ts.state = :state " +
            " AND ts.type = :type" +
            " AND ts.live = :live" +
            " AND ts.moto = :moto";

    private final Jdbi jdbi;

    @Inject
    public TransactionSummaryDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void upsert(String gatewayAccountId, String transactionType, ZonedDateTime transactionDate,
                       TransactionState state, boolean live, boolean moto, Long amount) {
        jdbi.withHandle(handle ->
                handle.createUpdate(UPSERT_STRING)
                        .bind("gatewayAccountId", gatewayAccountId)
                        .bind("type", transactionType)
                        .bind("transactionDate", transactionDate)
                        .bind("state", state)
                        .bind("live", live)
                        .bind("moto", moto)
                        .bind("amountInPence", amount)
                        .execute()
        );
    }

    public void updateFee(String gatewayAccountId, String transactionType, ZonedDateTime transactionDate,
                          TransactionState state, boolean live, boolean moto, Long fee) {
        jdbi.withHandle(handle ->
                handle.createUpdate(UPDATE_FEE)
                        .bind("gatewayAccountId", gatewayAccountId)
                        .bind("type", transactionType)
                        .bind("transactionDate", transactionDate)
                        .bind("state", state)
                        .bind("live", live)
                        .bind("moto", moto)
                        .bind("feeInPence", fee)
                        .execute()
        );
    }

    public void deductTransactionSummaryFor(String gatewayAccountId, String transactionType,
                                            ZonedDateTime transactionDate, TransactionState state, boolean live,
                                            boolean moto, Long amount, Long fee) {
        jdbi.withHandle(handle ->
                handle.createUpdate(DEDUCT_TRANSACTION_SUMMARY)
                        .bind("gatewayAccountId", gatewayAccountId)
                        .bind("type", transactionType)
                        .bind("transactionDate", transactionDate)
                        .bind("state", state)
                        .bind("live", live)
                        .bind("moto", moto)
                        .bind("amountInPence", amount)
                        .bind("feeInPence", fee)
                        .execute()
        );
    }

}
