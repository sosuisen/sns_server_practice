package com.example.message.dto;

import module java.base;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.RestQuery;
import com.example.constraint.MessageConstraints;
import com.example.error.*;

import jakarta.validation.constraints.*;

public class MessageListParams {
    @RestQuery
    @Schema(format = "date-time", examples = "2026-06-21T00:00:00Z")
    private String since;

    private Instant sinceTime = null;

    @RestQuery
    @Min(value = MessageConstraints.MIN_LIST_LIMIT, message = ErrorCode.LIMIT_INVALID)
    @Max(value = MessageConstraints.MAX_LIST_LIMIT, message = ErrorCode.LIMIT_INVALID)
    private Integer limit;

    public Instant since() {
        if (since != null) {
            try {
                this.sinceTime = Instant.parse(since);
            } catch (DateTimeParseException e) {
                throw new InvalidUriParameterException("Invalid since parameter: " + since,
                        ErrorCode.SINCE_INVALID);
            }
        }
        return sinceTime;
    }

    public Integer limit() {
        return limit;
    }
}
