package uk.gov.pay.ledger.util.fixture;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import uk.gov.pay.ledger.event.model.Event;
import uk.gov.pay.ledger.event.model.ResourceType;

import java.time.ZonedDateTime;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.pay.ledger.event.model.ResourceType.PAYOUT;

public class QueuePayoutEventFixture implements QueueFixture<QueuePayoutEventFixture, Event> {
    private String sqsMessageId;
    private ResourceType resourceType = PAYOUT;
    private String resourceExternalId = "resource_external_id";
    private ZonedDateTime eventDate = ZonedDateTime.parse("2020-05-13T18:45:00.000000Z");
    private String eventType = "PAYOUT_CREATED";
    private String eventData = "{\"event_data\": \"event data\"}";

    private QueuePayoutEventFixture() {
    }

    public static QueuePayoutEventFixture aQueuePayoutEventFixture() {
        return new QueuePayoutEventFixture();
    }

    public QueuePayoutEventFixture withResourceExternalId(String resourceExternalId) {
        this.resourceExternalId = resourceExternalId;
        return this;
    }

    public QueuePayoutEventFixture withEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    public QueuePayoutEventFixture withDefaultEventDataForEventType(String eventType) {
        switch (eventType) {
            case "PAYOUT_CREATED":
                eventData = new GsonBuilder().create()
                        .toJson(ImmutableMap.builder()
                                .put("amount", 1000)
                                .put("estimated_arrival_date_in_bank", "2020-05-13T18:45:33.000000Z")
                                .put("gateway_status", "pending")
                                .put("destination_type", "bank_account")
                                .put("statement_descriptor", "SERVICE NAME")
                                .build());
                break;
            default:
                eventData = new GsonBuilder().create()
                        .toJson(ImmutableMap.of("event_data", "event_data"));
        }
        return this;
    }

    @Override
    public Event toEntity() {
        return new Event(0L, sqsMessageId, resourceType, resourceExternalId, EMPTY, eventDate, eventType, eventData);
    }

    @Override
    public QueuePayoutEventFixture insert(AmazonSQS sqsClient) {
        this.sqsMessageId = QueueEventFixtureUtil.insert(sqsClient, eventType, eventDate, resourceExternalId,
                EMPTY, resourceType, eventData);
        return this;
    }

    public PactDslJsonBody getAsPact() {
        return QueueEventFixtureUtil.getAsPact(eventType, eventDate, resourceExternalId,
                EMPTY, resourceType, eventData);
    }
}
