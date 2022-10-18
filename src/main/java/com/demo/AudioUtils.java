/******************************************************************************
 *  Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved. 
 *  Licensed under the Apache License Version 2.0 (the 'License'). You may not
 *  use this file except in compliance with the License. A copy of the License
 *  is located at                                                            
 *
 *      http://www.apache.org/licenses/                                        
 *  or in the 'license' file accompanying this file. This file is distributed on
 *  an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 *  implied. See the License for the specific language governing permissions and
 *  limitations under the License.                                              
 ******************************************************************************/

package com.demo;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility class to download/upload audio files from/to S3
 *
 * <p>Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.</p>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public final class AudioUtils {

    private static final Logger logger = LoggerFactory.getLogger(AudioUtils.class);

    /**
     * Converts the given raw audio data into a wav file. Returns the wav file back.
     */
    private static File convertToWav(String audioFilePath) throws IOException, UnsupportedAudioFileException {
        File outputFile = new File(audioFilePath.replace(".raw", ".wav"));
        AudioInputStream source = new AudioInputStream(Files.newInputStream(Paths.get(audioFilePath)),
                new AudioFormat(8000, 16, 1, true, false), -1); // 8KHz, 16 bit, 1 channel, signed, little-endian
        AudioSystem.write(source, AudioFileFormat.Type.WAVE, outputFile);
        return outputFile;
    }

    /**
     * Saves the raw audio file as an S3 object
     *
     * @param region
     * @param bucketName
     * @param keyPrefix
     * @param audioFilePath
     * @param awsCredentials
     * @param eventDetail
     */
    public static S3UploadInfo uploadRawAudio(Regions region, String bucketName, String keyPrefix, String audioFilePath,
                                              String agentId, String contactId, boolean publicReadAcl,
                                              AWSCredentialsProvider awsCredentials, EventDetail eventDetail) {
        File wavFile = null;
        S3UploadInfo uploadInfo = null;

        try {

            AmazonS3Client s3Client = (AmazonS3Client) AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(awsCredentials)
                    .build();

            wavFile = convertToWav(audioFilePath);

            // upload the raw audio file to the designated S3 location
            String objectKey = eventDetail.getCallId() + "_" + eventDetail.getTransactionId() + "_" + eventDetail.getStartFragmentNumber();

            logger.info(String.format("Uploading Audio: to %s/%s from %s", bucketName, objectKey + ".wav", wavFile));
            PutObjectRequest request = new PutObjectRequest(bucketName, objectKey + ".wav", wavFile);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("audio/wav");
            metadata.addUserMetadata("callId", eventDetail.getCallId());
            metadata.addUserMetadata("direction", eventDetail.getDirection());
            metadata.addUserMetadata("transactionId", eventDetail.getTransactionId());
            metadata.addUserMetadata("toNumber", eventDetail.getToNumber());
            metadata.addUserMetadata("fromNumber", eventDetail.getFromNumber());
            metadata.addUserMetadata("startTime", eventDetail.getStartTime());
            metadata.addUserMetadata("streamArn", eventDetail.getStreamArn());
            metadata.addUserMetadata("event", eventDetail.toString());

            request.setMetadata(metadata);

            if (publicReadAcl) {
                request.setCannedAcl(CannedAccessControlList.PublicRead);
            }

            PutObjectResult s3result = s3Client.putObject(request);


            ObjectMapper mapper = new ObjectMapper();
            byte[] eventDetailByteArr = mapper.writeValueAsBytes(eventDetail);
            PutObjectRequest metadataRequest = new PutObjectRequest(bucketName, objectKey + ".metadata", new ByteArrayInputStream(eventDetailByteArr), new ObjectMetadata());
            s3Client.putObject(metadataRequest);


            logger.info("putObject completed successfully " + s3result.getETag());

            uploadInfo = new S3UploadInfo(bucketName, objectKey + ".wav", region);

        } catch (SdkClientException e) {
            logger.error("Audio upload to S3 failed: ", e);
            throw e;
        } catch (UnsupportedAudioFileException | IOException e) {
            logger.error("Failed to convert to wav: ", e);
        } finally {
            if (wavFile != null) {
                wavFile.delete();
            }
        }

        return uploadInfo;
    }
}
