package com.helios.cctv.dto.cctv.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PreprocessRequest(
        @NotNull Long cctvId,
        Integer sec
) {}
