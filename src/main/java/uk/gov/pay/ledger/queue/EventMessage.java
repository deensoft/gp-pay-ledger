package uk.gov.pay.ledger.queue;

import uk.gov.pay.ledger.event.model.Event;
import uk.gov.service.payments.commons.queue.model.QueueMessage;

public class EventMessage {
    private EventMessageDto eventDto;
    private QueueMessage queueMessage;

    public EventMessage(EventMessageDto eventDto, QueueMessage queueMessage) {
        this.eventDto = eventDto;
        this.queueMessage = queueMessage;
    }

    public static EventMessage of(EventMessageDto eventDto, QueueMessage queueMessage) {
        return new EventMessage(eventDto, queueMessage);
    }

    public String getQueueMessageId() {
        return queueMessage.getMessageId();
    }

    public Event getEvent() {
        return new Event(
                getQueueMessageId(),
                eventDto.getServiceId(),
                eventDto.isLive(),
                eventDto.getResourceType(),
                eventDto.getExternalId(),
                eventDto.getParentExternalId(),
                eventDto.getTimestamp(),
                eventDto.getEventType(),
                eventDto.getEventData(),
                eventDto.isReprojectDomainObject()
        );
    }

    public String getQueueMessageReceiptHandle() {
        return queueMessage.getReceiptHandle();
    }

    public EventMessageDto getEventDto() {
        return eventDto;
    }
}