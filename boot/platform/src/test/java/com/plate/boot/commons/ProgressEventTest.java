package com.plate.boot.commons;

import com.plate.boot.commons.exception.RestServerException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProgressEvent} (no Spring / container required).
 */
class ProgressEventTest {

    @Test
    void constructorSetsProcessedAndReqAndNotOk() {
        Object req = new Object();
        ProgressEvent event = new ProgressEvent(10L, req);

        assertThat(event.getProcessed()).isEqualTo(10L);
        assertThat(event.getReq()).isSameAs(req);
        assertThat(event.getIsOk()).isFalse();
    }

    @Test
    void ofCreatesInstance() {
        Object req = new Object();
        ProgressEvent event = ProgressEvent.of(5L, req);

        assertThat(event.getProcessed()).isEqualTo(5L);
        assertThat(event.getReq()).isSameAs(req);
        assertThat(event.getIsOk()).isFalse();
    }

    @Test
    void withMessageMarksOk() {
        ProgressEvent event = ProgressEvent.of(1L, null).withMessage("done");

        assertThat(event.getMessage()).isEqualTo("done");
        assertThat(event.getIsOk()).isTrue();
    }

    @Test
    void withResultSetsMessageResAndOk() {
        Object res = new Object();
        ProgressEvent event = ProgressEvent.of(1L, null).withResult("ok", res);

        assertThat(event.getMessage()).isEqualTo("ok");
        assertThat(event.getRes()).isSameAs(res);
        assertThat(event.getIsOk()).isTrue();
    }

    @Test
    void withErrorSetsMessageErrorAndNotOk() {
        RestServerException error = RestServerException.withMsg("boom", new RuntimeException("boom"));
        ProgressEvent event = ProgressEvent.of(1L, null).withError("failed", error);

        assertThat(event.getMessage()).isEqualTo("failed");
        assertThat(event.getError()).isSameAs(error);
        assertThat(event.getIsOk()).isFalse();
    }

    @Test
    void accessorsRoundTrip() {
        ProgressEvent event = new ProgressEvent();
        RestServerException error = RestServerException.withMsg("e", new RuntimeException("e"));
        Object req = new Object();
        Object res = new Object();

        event.setProcessed(7L);
        event.setIsOk(true);
        event.setMessage("msg");
        event.setReq(req);
        event.setRes(res);
        event.setError(error);

        assertThat(event.getProcessed()).isEqualTo(7L);
        assertThat(event.getIsOk()).isTrue();
        assertThat(event.getMessage()).isEqualTo("msg");
        assertThat(event.getReq()).isSameAs(req);
        assertThat(event.getRes()).isSameAs(res);
        assertThat(event.getError()).isSameAs(error);
    }

    @Test
    void toStringContainsFields() {
        ProgressEvent event = ProgressEvent.of(3L, null).withMessage("done");
        assertThat(event.toString()).contains("done");
    }

    @Test
    void equalsAndHashCodeBasedOnAllFields() {
        ProgressEvent a = ProgressEvent.of(1L, null).withMessage("done");
        ProgressEvent b = ProgressEvent.of(1L, null).withMessage("done");
        ProgressEvent c = ProgressEvent.of(2L, null).withMessage("done");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }
}
