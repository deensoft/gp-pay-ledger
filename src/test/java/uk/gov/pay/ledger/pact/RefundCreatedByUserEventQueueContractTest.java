package uk.gov.pay.ledger.pact;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import uk.gov.pay.ledger.event.model.ResourceType;
import uk.gov.pay.ledger.rule.AppWithPostgresAndSqsRule;
import uk.gov.pay.ledger.rule.SqsTestDocker;
import uk.gov.pay.ledger.transaction.dao.TransactionDao;
import uk.gov.pay.ledger.util.DatabaseTestHelper;
import uk.gov.pay.ledger.util.fixture.QueueRefundEventFixture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.dropwizard.testing.ConfigOverride.config;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.pay.ledger.util.fixture.QueueRefundEventFixture.aQueueRefundEventFixture;
import static uk.gov.pay.ledger.util.fixture.TransactionFixture.aTransactionFixture;

public class RefundCreatedByUserEventQueueContractTest {
    @Rule
    public MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);

    @Rule
    public AppWithPostgresAndSqsRule appRule = new AppWithPostgresAndSqsRule(
            config("queueMessageReceiverConfig.backgroundProcessingEnabled", "true")
    );

    private byte[] currentMessage;

    private QueueRefundEventFixture refundFixture = aQueueRefundEventFixture()
            .withResourceType(ResourceType.REFUND)
            .withEventType("REFUND_CREATED_BY_USER")
            .withRefundedBy(RandomStringUtils.randomAlphanumeric(14))
            .withDefaultEventDataForEventType("REFUND_CREATED_BY_USER");

    @Pact(provider = "connector", consumer = "ledger")
    public MessagePact createRefundCreatedByUserEventPact(MessagePactBuilder builder) {
        Map<String, String> metadata = new HashMap();
        metadata.put("contentType", "application/json");

        return builder
                .expectsToReceive("a refund created by user message")
                .withMetadata(metadata)
                .withContent(refundFixture.getAsPact())
                .toPact();
    }

    @Test
    @PactVerification({"connector"})
    public void test() throws Exception {
        aTransactionFixture()
                .withExternalId(refundFixture.getParentResourceExternalId())
                .insert(appRule.getJdbi());

        appRule.getSqsClient().sendMessage(SqsTestDocker.getQueueUrl("event-queue"), new String(currentMessage));

        DatabaseTestHelper dbHelper = DatabaseTestHelper.aDatabaseTestHelper(appRule.getJdbi());
        TransactionDao transactionDao = new TransactionDao(appRule.getJdbi());
        await().atMost(1000, TimeUnit.SECONDS).until(
                () -> transactionDao.findTransactionByExternalId(refundFixture.getResourceExternalId()).isPresent()
        );

        Map<String, Object> event = dbHelper.getEventByExternalId(refundFixture.getResourceExternalId());
        JsonObject eventData = new JsonParser().parse(event.get("event_data").toString()).getAsJsonObject();

        assertThat(event.get("resource_external_id"), is(refundFixture.getResourceExternalId()));
        assertThat(event.get("parent_resource_external_id"), is(refundFixture.getParentResourceExternalId()));
        assertThat(eventData.get("amount").getAsLong(), is(refundFixture.getAmount()));
        assertThat(eventData.get("refunded_by").getAsString(), is(refundFixture.getRefundedBy()));
    }

    public void setMessage(byte[] messageContents) {
        currentMessage = messageContents;
    }
}
