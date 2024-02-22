package com.palgona.palgona.dto;

public record MileageChargeRequest(
    Integer amount,
    String category
) {
}
