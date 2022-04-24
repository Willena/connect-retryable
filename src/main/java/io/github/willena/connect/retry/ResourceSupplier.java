package io.github.willena.connect.retry;

public interface ResourceSupplier<ResourceT> {
    ResourceT get() throws Exception;
}
