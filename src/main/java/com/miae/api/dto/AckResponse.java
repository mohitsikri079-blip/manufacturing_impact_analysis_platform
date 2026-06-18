package com.miae.api.dto;

/**
 * Data transfer object for acknowledging the receipt and processing of an upsert request. 
  */
public record AckResponse(String status, String id) {

    public static AckResponse upserted(String id) {
        return new AckResponse("UPSERTED", id);
    }
}
