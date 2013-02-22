package cucumber.runtime.arquillian.junit;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.arquillian.stream.NotCloseablePrintStream;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runner.RunWith;

import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

@RunWith(Arquillian.class)
public abstract class Cucumber {
    private final static PrintStream ORIGINAL_OUT = System.out;
    private final static PrintStream NOT_CLOSEABLE_OUT = new NotCloseablePrintStream(ORIGINAL_OUT);

    protected final RuntimeOptions runtimeOptions;
    
    public Cucumber() {
        runtimeOptions = new RuntimeOptions(new Properties(), "-m");
        runtimeOptions.strict = true;
    }

    @BeforeClass
    public static void setFakeOut() {
        System.setOut(NOT_CLOSEABLE_OUT);
    }

    @AfterClass
    public static void resetOut() {
        System.setOut(ORIGINAL_OUT);
    }

    @Test
    public void runFeatures() throws Exception {
        initializeRuntimeOptions();
        Runtime runtime = createRuntime();
        runtime.run();
        List<Throwable> errors = runtime.getErrors();
        try {
            if (!errors.isEmpty()) {
                throw new MultipleFailureException(errors);
            }
        } finally {
            destroyRuntime(runtime);
        }
    }
    
    protected ResourceLoader createResourceLoader() {
        return new MultiLoader(this.getClassLoader());
    }
    
    protected Runtime createRuntime() {
        return new Runtime(
            createResourceLoader(),
            getClassLoader(),
            runtimeOptions
        );
    }
    
    protected void destroyRuntime(Runtime runtime) {
        // intentionally empty
    }
    
    protected ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
    
    protected abstract void initializeRuntimeOptions();
}
