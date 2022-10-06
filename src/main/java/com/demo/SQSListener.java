package com.demo;


import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.kinesisvideo.parser.ebml.InputStreamParserByteSource;
import com.amazonaws.kinesisvideo.parser.mkv.StreamingMkvReader;
import com.amazonaws.kinesisvideo.parser.utilities.FragmentMetadataVisitor;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;


public class SQSListener implements RequestHandler<SQSEvent, String> {
    private static final Logger logger = LoggerFactory.getLogger(SQSListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int CHUNK_SIZE_IN_KB = 4;


    @Override
    public String handleRequest(SQSEvent event, Context context) {
        logger.error("STARTING......." + 2);
        try {
            logger.error(" received request : " + objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            logger.error(" Error happened where serializing the event", e);
        }
        logger.error(" received context: " + context.toString());

        try {
            event.getRecords().forEach(msg -> {
                logger.error("Received streaming message  : " + msg.getBody());
            });
            if (event.getRecords().size() != 1) {
                logger.error("Invalid number of records present in the SQS message body");
                throw new RuntimeException("Invalid number of records");
            }

            SQSEvent.SQSMessage sqsMessage = event.getRecords().get(0);
            String eventBody = sqsMessage.getBody();
            logger.error("SQS message body: {} ", eventBody);

            EventDetail eventDetail = objectMapper.readValue(eventBody, EventDetail.class);

            logger.error("eventDetail: {} ", eventDetail);


            String streamingStatus = eventDetail.getStreamingStatus();
            String transactionId = eventDetail.getTransactionId();
            String streamArn = eventDetail.getStreamArn();
            String startFragmentNumber = eventDetail.getStartFragmentNumber();
            String startTime = eventDetail.getStartTime();

            logger.error("transactionId: {} ", transactionId);
            logger.error("streamArn: {} ", streamArn);
            logger.error("startFragmentNumber: {} ", startFragmentNumber);
            logger.error("startTime: {} ", startTime);

            if (streamingStatus.equals("ENDED")) {
                logger.error("Skipping event ");
                return "{ \"result\": \"Success\" }";
            }


            Path saveAudioFilePath = Paths.get("/tmp",
                    transactionId + "_" + DATE_FORMAT.format(new Date()) + ".raw");
            FileOutputStream fileOutputStream = new FileOutputStream(saveAudioFilePath.toString());

            InputStream kvsInputStream = KVSUtils.getInputStreamFromKVS(streamArn, Regions.US_EAST_1, startFragmentNumber,
                    getAWSCredentials());
            logger.error("got  kvsInputStream");

            StreamingMkvReader streamingMkvReader = StreamingMkvReader
                    .createDefault(new InputStreamParserByteSource(kvsInputStream));
            logger.error("got  streamingMkvReader");

            KVSTransactionIdTagProcessor tagProcessor = new KVSTransactionIdTagProcessor(transactionId);
            FragmentMetadataVisitor fragmentVisitor = FragmentMetadataVisitor.create(Optional.of(tagProcessor));
            try {
                logger.error("Getting output buffer");

                while (true) {
                    ByteBuffer outputBuffer = KVSUtils.getByteBufferFromStream(streamingMkvReader, fragmentVisitor, tagProcessor,
                            CHUNK_SIZE_IN_KB);
                    logger.error(" in OutputBuffer while loop {}", outputBuffer.remaining());

                    if (outputBuffer.remaining() > 0) {
                        //Write audioBytes to a temporary file as they are received from the stream
                        byte[] audioBytes = new byte[outputBuffer.remaining()];
                        outputBuffer.get(audioBytes);
                        fileOutputStream.write(audioBytes);
                    } else {
                        logger.error(" in OutputBuffer all done..");
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error(" Exception", e);


            } finally {
                logger.error(" In finally block");

                // Upload the raw audio regardless of any exception thrown in the middle
                closeFileAndUploadRawAudio(kvsInputStream, fileOutputStream, saveAudioFilePath, transactionId, startTime);
            }


//            handler.handleRequest(sqsMessage.getBody());
        } catch (Exception e) {
            logger.error(" *************************** ERROR : ", e);
            return "{ \"result\": \"Failed\" }";
        }
        logger.error(" Returning Success");


        return "{ \"result\": \"Success\" }";
    }


    private static AWSCredentialsProvider getAWSCredentials() {
        return DefaultAWSCredentialsProviderChain.getInstance();
    }

    private void closeFileAndUploadRawAudio(InputStream kvsInputStream, FileOutputStream fileOutputStream,
                                            Path saveAudioFilePath, String transactionId, String startTime) throws IOException {
        logger.error("closeFileAndUploadRawAudio");
        try {
            kvsInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            logger.error("[{}] Failed to close KVS or file streams due to ", e);
        } finally {
            // Always upload the raw audio file to S3
            if (new File(saveAudioFilePath.toString()).length() > 0) {
                logger.error("Uploading to S3 ");

                AudioUtils.uploadRawAudio(Regions.US_EAST_1, "callrecordings-us-east-1-801011379177", "RECORDINGS_KEY_PREFIX",
                        saveAudioFilePath.toString(), transactionId, startTime, false, getAWSCredentials());
            } else {
                logger.info("Skipping upload to S3. Audio file has 0 bytes: " + saveAudioFilePath);
            }
        }
    }

}
