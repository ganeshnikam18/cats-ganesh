package com.endava.cats.fuzzer.headers;

import com.endava.cats.Fuzzer;
import com.endava.cats.annotations.HeaderFuzzer;
import com.endava.cats.dsl.CatsDSLWords;
import com.endava.cats.http.ResponseCodeFamily;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.io.ServiceData;
import com.endava.cats.model.CatsHeader;
import com.endava.cats.model.CatsResponse;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.report.TestCaseListener;
import com.endava.cats.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
@HeaderFuzzer
public class DuplicateHeadersFuzzer implements Fuzzer {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(DuplicateHeadersFuzzer.class);
    private final ServiceCaller serviceCaller;
    private final TestCaseListener testCaseListener;

    public DuplicateHeadersFuzzer(ServiceCaller sc, TestCaseListener lr) {
        this.serviceCaller = sc;
        this.testCaseListener = lr;
    }

    @Override
    public void fuzz(FuzzingData data) {
        if (data.getHeaders().isEmpty()) {
            logger.skip("No headers to fuzz");
        }
        List<CatsHeader> headers = new ArrayList<>(data.getHeaders());
        CatsHeader header = CatsHeader.builder().name(CatsDSLWords.CATS_FUZZY_HEADER).required(false).value(CatsDSLWords.CATS_FUZZY_HEADER).build();

        if (headers.isEmpty()) {
            headers.add(header);
        }

        for (CatsHeader catsHeader : headers) {
            List<CatsHeader> finalHeadersList = new ArrayList<>(headers);
            finalHeadersList.add(catsHeader.copy());
            testCaseListener.createAndExecuteTest(logger, this, () -> process(data, finalHeadersList, catsHeader));
        }

    }

    private void process(FuzzingData data, List<CatsHeader> headers, CatsHeader targetHeader) {
        testCaseListener.addScenario(logger, "Add a duplicate header inside the request: name [{}], value [{}]. All other details are similar to a happy flow", targetHeader.getName(), targetHeader.getTruncatedValue());
        testCaseListener.addExpectedResult(logger, "Should get a 4XX response code");

        CatsResponse response = serviceCaller.call(ServiceData.builder().relativePath(data.getPath()).headers(headers)
                .payload(data.getPayload()).queryParams(data.getQueryParams()).httpMethod(data.getMethod()).contentType(data.getFirstRequestContentType()).build());

        testCaseListener.reportResult(logger, data, response, ResponseCodeFamily.FOURXX);
    }


    public String toString() {
        return ConsoleUtils.sanitizeFuzzerName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "send a 'happy' flow request and duplicate an existing header";
    }
}