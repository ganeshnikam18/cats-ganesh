package com.endava.cats.fuzzer.fields.base;

import com.endava.cats.args.FilesArguments;
import com.endava.cats.generator.simple.StringGenerator;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.report.TestCaseListener;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

@QuarkusTest
class ExactValuesInFieldsFuzzerTest {

    private ExactValuesInFieldsFuzzer myBaseBoundaryFuzzer;

    @BeforeEach
    public void setup() {
        myBaseBoundaryFuzzer = new MyExactValueFuzzer(null, null, null);
    }

    @Test
    void shouldGetBoundaryValueForSchemaWithPattern() {
        Schema<String> schema = new StringSchema();
        schema.setPattern("[0-9]+");
        schema.setMaxLength(10);
        Object generated = myBaseBoundaryFuzzer.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches("[0-9]+");
    }

    @Test
    void shouldGetBoundaryValueForSchemaWithNoPattern() {
        Schema<String> schema = new StringSchema();
        schema.setMaxLength(10);
        Object generated = myBaseBoundaryFuzzer.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches(StringGenerator.ALPHANUMERIC_PLUS);
    }

    @Test
    void shouldGetBoundaryValueForSchemaWithPatternUsingRegexGen() {
        Schema<String> schema = new StringSchema();
        schema.setPattern("^(A-\\d{1,12})$");
        schema.setMaxLength(14);
        Object generated = myBaseBoundaryFuzzer.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches("^(A-\\d{1,12})$");
    }

    @Test
    void shouldGetNullBoundaryValueWhenNoBoundaries() {
        Schema<String> schema = new StringSchema();
        Object generated = myBaseBoundaryFuzzer.getBoundaryValue(schema);

        Assertions.assertThat(generated).isNull();
    }

    @Test
    void shouldGetBase64EncodeWhenByteArray() {
        Schema<byte[]> schema = new ByteArraySchema();
        schema.setMaxLength(10);
        Object generated = myBaseBoundaryFuzzer.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches("^[A-Za-z0-9+/=]+\\Z");
    }

    static class MyExactValueFuzzer extends ExactValuesInFieldsFuzzer {

        public MyExactValueFuzzer(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        protected String exactValueTypeString() {
            return null;
        }

        @Override
        protected Function<Schema, Number> getExactMethod() {
            return Schema::getMaxLength;
        }
    }
}
