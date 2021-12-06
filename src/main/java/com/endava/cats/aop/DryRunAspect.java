package com.endava.cats.aop;

import com.endava.cats.args.FilterArguments;
import com.endava.cats.model.CatsResponse;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.util.CatsUtil;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Map;
import java.util.TreeMap;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Aspect used to suspend CATS logic when running in dryRun mode.
 * The aspect will suppress all calls to the service and any reporting.
 * As Quarkus does not support true AOP. To keep the code as clean as possible @DryRun was used to annotate
 * classes which are required to suspend their execution.
 */
@DryRun
@Interceptor
public class DryRunAspect {

    private static final PrettyLogger LOGGER = PrettyLoggerFactory.getLogger(DryRunAspect.class);
    private final Map<String, Integer> paths = new TreeMap<>();
    @Inject
    FilterArguments filterArguments;
    private int counter;

    public Object startSession(InvocationContext context) throws Exception {
        Object result = context.proceed();
        CatsUtil.setCatsLogLevel("OFF");
        return result;
    }

    public Object dontInvokeService(InvocationContext context) {
        return CatsResponse.empty();
    }

    public Object dontWriteTestCase(InvocationContext context) {
        return null;
    }

    public Object getErrors(InvocationContext context) {
        return 0;
    }

    public Object endSession(InvocationContext context) {
        System.out.print("\n");
        CatsUtil.setCatsLogLevel("INFO");
        LOGGER.note("Number of tests that will be run with this configuration: {}", paths.values().stream().reduce(0, Integer::sum));
        paths.forEach((s, integer) -> LOGGER.star(ansi().fgBrightYellow().bold().a(" -> path {}: {} tests").toString(), s, integer));
        return null;
    }

    public Object report(InvocationContext context) {
        Object data = context.getParameters()[1];
        if (data instanceof FuzzingData) {
            if (counter % 10000 == 0) {
                System.out.print(".");
            }
            paths.merge(((FuzzingData) data).getPath(), 1, Integer::sum);
        } else {
            paths.merge("contract-level", 1, Integer::sum);
        }
        counter++;
        return null;
    }

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        if (filterArguments.isDryRun()) {
            if (context.getMethod().getName().startsWith("report")) {
                return report(context);
            }
            if (context.getMethod().getName().startsWith("endSession")) {
                return endSession(context);
            }
            if (context.getMethod().getName().startsWith("startSession")) {
                return startSession(context);
            }
            if (context.getMethod().getName().startsWith("call")) {
                return dontInvokeService(context);
            }
            if (context.getMethod().getName().startsWith("getErrors")) {
                return getErrors(context);
            }
            if (context.getMethod().getName().startsWith("writeTestCase")) {
                return dontWriteTestCase(context);
            }
        }
        return context.proceed();
    }
}
