package com.helios.cctv.dto.cctv.response;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResponse(
        String jobId,
        String state,
        Integer progress,
        String message
) {}

