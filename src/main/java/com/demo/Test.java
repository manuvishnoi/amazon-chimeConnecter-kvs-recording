package com.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public static void main(String[] args) throws JsonProcessingException {
        String str = "{  \"callId\": \"ac6cadba-cfc3-49d4-b122-cf5a4c8e509e\",  \"currentFragmentNumber\": \"91343852333181437428622142461759456544661700614\",  \"direction\": \"Inbound\",  \"endTime\": \"2022-10-04T21:29:51.831Z\",  \"fromNumber\": \"+16304562235\",  \"isCaller\": false,  \"startFragmentNumber\": \"91343852333181437344442219790353593609732156333\",  \"startTime\": \"2022-10-04T21:29:34.742Z\",  \"streamArn\": \"arn:aws:kinesisvideo:us-east-1:801011379177:stream/ChimeVoiceConnector-hul0ytfnsqnqjvaahbgg4-fb62f1a5-c069-49ba-8456-b2d7e5d51733/1664918707393\",  \"toNumber\": \"+16142546641\",  \"transactionId\": \"2766603b-f26d-4de1-8ee7-714fe028f923\",  \"voiceConnectorId\": \"hul0ytfnsqnqjvaahbgg4\",  \"streamingStatus\": \"ENDED\",  \"version\": \"0\"}";

        EventDetail ed = objectMapper.readValue(str, EventDetail.class);
        System.out.println(ed);
    }
}
