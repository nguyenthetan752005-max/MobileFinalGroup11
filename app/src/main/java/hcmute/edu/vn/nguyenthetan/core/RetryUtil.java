package hcmute.edu.vn.nguyenthetan.core;

import java.io.IOException;

public final class RetryUtil {

    public interface Retryable<T> {
        T execute() throws IOException;
    }

    private RetryUtil() {
    }

    public static <T> T retryWithBackoff(
            Retryable<T> block,
            int times,
            long initialDelayMillis,
            long maxDelayMillis,
            double factor
    ) throws IOException {
        if (times <= 1) {
            return block.execute();
        }

        long currentDelay = initialDelayMillis;
        IOException lastException = null;
        for (int attempt = 0; attempt < times - 1; attempt++) {
            try {
                return block.execute();
            } catch (IOException exception) {
                lastException = exception;
                try {
                    Thread.sleep(currentDelay);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", interruptedException);
                }
                currentDelay = Math.min((long) (currentDelay * factor), maxDelayMillis);
            }
        }
        if (lastException != null) {
            return block.execute();
        }
        return block.execute();
    }
}
