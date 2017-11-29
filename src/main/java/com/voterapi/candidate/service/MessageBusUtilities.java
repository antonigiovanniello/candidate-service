package com.voterapi.candidate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.voterapi.candidate.domain.Candidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MessageBusUtilities {

    private final Logger logger = LoggerFactory.getLogger(MessageBusUtilities.class);
    private IQueueClient queueSendClient;

    public MessageBusUtilities(Environment environment) {

        String connectionString = environment.getProperty("azure.service-bus.connection-string");
        String queueName = "candidates.queue";
        try {
            queueSendClient = new QueueClient(
                    new ConnectionStringBuilder(connectionString, queueName), ReceiveMode.PEEKLOCK);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ServiceBusException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageAzureServiceBus(Candidate candidate) {
        String message = serializeToJson(candidate);
        queueSendClient.sendAsync(new Message(message))
                .thenRunAsync(queueSendClient::closeAsync);
    }

    protected String serializeToJson(Candidate candidate) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";

        try {
            jsonInString = mapper.writeValueAsString(candidate);
        } catch (JsonProcessingException e) {
            logger.info(String.valueOf(e));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Serialized message payload: {}", jsonInString);
        }

        return jsonInString;
    }
}