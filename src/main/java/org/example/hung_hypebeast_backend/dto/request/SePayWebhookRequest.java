package org.example.hung_hypebeast_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SePayWebhookRequest {

    @NotBlank(message = "trackingToken không được để trống")
    private String trackingToken;
}

