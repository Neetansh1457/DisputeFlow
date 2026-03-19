package com.disputeflow.backend.dto.request;

import com.disputeflow.backend.enums.ReasonAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReasonCodeRequest {

    @NotBlank(message = "Reason code is required")
    private String reasonCode;

    @NotNull(message = "Action is required")
    private ReasonAction action;

    private String description;
}