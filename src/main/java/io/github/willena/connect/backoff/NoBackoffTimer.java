package io.github.willena.connect.backoff;


class NoBackoffTimer implements BackoffTimer {
    public long computeBackoffMillis(int attempts) {
        return 0L;
    }

    public String type() {
        return "no";
    }
}

