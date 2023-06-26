package com.endava.cats.fuzzer.fields;

import com.endava.cats.args.FilesArguments;
import com.endava.cats.args.MatchArguments;
import com.endava.cats.fuzzer.executor.FieldsIteratorExecutor;
import com.endava.cats.http.HttpMethod;
import com.endava.cats.http.ResponseCodeFamily;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.model.CatsResponse;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.report.TestCaseExporter;
import com.endava.cats.report.TestCaseListener;
import com.endava.cats.util.CatsUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

@QuarkusTest
class ReplacePrimitivesWithArraysFieldsFuzzerTest {
    ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;
    @InjectSpy
    CatsUtil catsUtil;
    FieldsIteratorExecutor catsExecutor;
    private ReplacePrimitivesWithArraysFieldsFuzzer replacePrimitivesWithArraysFieldsFuzzer;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testCaseExporter", Mockito.mock(TestCaseExporter.class));
        catsExecutor = new FieldsIteratorExecutor(serviceCaller, testCaseListener, catsUtil, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        replacePrimitivesWithArraysFieldsFuzzer = new ReplacePrimitivesWithArraysFieldsFuzzer(catsExecutor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(replacePrimitivesWithArraysFieldsFuzzer.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(replacePrimitivesWithArraysFieldsFuzzer).hasToString("ReplacePrimitivesWithArraysFieldsFuzzer");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(replacePrimitivesWithArraysFieldsFuzzer.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfFieldObject() {
        FuzzingData data = Mockito.mock(FuzzingData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": {"inner": "inner"}}
                """);
        replacePrimitivesWithArraysFieldsFuzzer.fuzz(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldReplaceIfFieldPrimitive() {
        FuzzingData data = Mockito.mock(FuzzingData.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(CatsResponse.builder().body("{}").responseCode(200).build());

        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": 12}
                """);
        replacePrimitivesWithArraysFieldsFuzzer.fuzz(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamily.FOURXX));
    }
}