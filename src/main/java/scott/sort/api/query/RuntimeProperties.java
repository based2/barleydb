package scott.sort.api.query;

import java.io.Serializable;

/**
 * Runtime properties for a query execution
 * @author scott
 *
 */
public class RuntimeProperties implements Serializable {

    public static enum ScrollType {
        FORWARD_ONLY,
        SCROLL_SENSITIVE,
        SCROLL_INSENSITIVE
    }

    public static enum Concurrency {
        READ_ONLY,
        UPDATABLE
    }

    private static final long serialVersionUID = 1L;

    private Integer fetchSize;

    private ScrollType scrollType;

    private Concurrency concurrency;

    private Boolean executeInSameContext;

    public RuntimeProperties fetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }

    public RuntimeProperties override(RuntimeProperties props) {
        RuntimeProperties rp = new RuntimeProperties();
        rp.fetchSize = fetchSize != null ? fetchSize : props.fetchSize;
        rp.executeInSameContext = executeInSameContext != null ? executeInSameContext : props.executeInSameContext;
        rp.scrollType = scrollType != null ? scrollType : props.scrollType;
        rp.concurrency = concurrency != null ? concurrency : props.concurrency;
        return rp;
    }

    public RuntimeProperties executeInSameContext(boolean executeInSameContext) {
        this.executeInSameContext = executeInSameContext;
        return this;
    }

    public RuntimeProperties scrollType(ScrollType scrollType) {
        this.scrollType = scrollType;
        return this;
    }

    public RuntimeProperties concurrency(Concurrency concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    public Integer getFetchSize() {
        return fetchSize;
    }

    public Boolean getExecuteInSameContext() {
        return executeInSameContext;
    }

    public ScrollType getScrollType() {
        return scrollType;
    }

    public Concurrency getConcurrency() {
        return concurrency;
    }
}