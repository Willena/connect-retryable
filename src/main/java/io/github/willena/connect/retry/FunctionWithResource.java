package io.github.willena.connect.retry;


@FunctionalInterface
public interface FunctionWithResource<ResourceT, ReturnT> {
    ReturnT apply(ResourceT param1ResourceT) throws Exception;
}