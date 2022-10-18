package com.demo;

import java.io.Serializable;

public class EventDetail implements Serializable {
    private String streamingStatus;
    private String transactionId;
    private String streamArn;
    private String startFragmentNumber;
    private String startTime;
    private String callId;
    private String direction;
    private String endTime;
    private String fromNumber;
    private boolean isCaller;
    private String toNumber;
    private String voiceConnectorId;
    private String version;


    public String getStreamingStatus() {
        return streamingStatus;
    }

    public void setStreamingStatus(String streamingStatus) {
        this.streamingStatus = streamingStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStreamArn() {
        return streamArn;
    }

    public void setStreamArn(String streamArn) {
        this.streamArn = streamArn;
    }

    public String getStartFragmentNumber() {
        return startFragmentNumber;
    }

    public void setStartFragmentNumber(String startFragmentNumber) {
        this.startFragmentNumber = startFragmentNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }

    public boolean isCaller() {
        return isCaller;
    }

    public void setCaller(boolean caller) {
        isCaller = caller;
    }

    public String getToNumber() {
        return toNumber;
    }

    public void setToNumber(String toNumber) {
        this.toNumber = toNumber;
    }

    public String getVoiceConnectorId() {
        return voiceConnectorId;
    }

    public void setVoiceConnectorId(String voiceConnectorId) {
        this.voiceConnectorId = voiceConnectorId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "EventDetail{" +
                "streamingStatus='" + streamingStatus + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", streamArn='" + streamArn + '\'' +
                ", startFragmentNumber='" + startFragmentNumber + '\'' +
                ", startTime='" + startTime + '\'' +
                ", callId='" + callId + '\'' +
                ", direction='" + direction + '\'' +
                ", endTime='" + endTime + '\'' +
                ", fromNumber='" + fromNumber + '\'' +
                ", isCaller=" + isCaller +
                ", toNumber='" + toNumber + '\'' +
                ", voiceConnectorId='" + voiceConnectorId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
