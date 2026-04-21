package hcmute.edu.vn.nguyenthetan.ui.lesson.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AudioRecordingManager {

    private static final int SPEAKING_SAMPLE_RATE = 16000;

    public interface Listener {
        void onRecordingStarted();
        void onRecordingStopped(File audioFile);
        void onRecordingFailed();
        void onPermissionDenied();
    }

    private final Context context;
    private final Listener listener;

    private AudioRecord audioRecord;
    private File pendingRecordingFile;
    private Thread recordingThread;
    private volatile boolean isRecordingAudio;
    private int audioBufferSize;

    public AudioRecordingManager(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public boolean isRecording() {
        return isRecordingAudio;
    }

    public void startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            listener.onPermissionDenied();
            return;
        }

        releaseRecorder(false);
        audioBufferSize = AudioRecord.getMinBufferSize(
                SPEAKING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (audioBufferSize <= 0) {
            listener.onRecordingFailed();
            return;
        }

        pendingRecordingFile = new File(context.getCacheDir(), "speaking_" + System.currentTimeMillis() + ".wav");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SPEAKING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBufferSize
        );

        try {
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                releaseRecorder(false);
                listener.onRecordingFailed();
                return;
            }
            writeEmptyWaveHeader(pendingRecordingFile);
            audioRecord.startRecording();
            isRecordingAudio = true;
            recordingThread = new Thread(() -> writeAudioToWaveFile(pendingRecordingFile), "speaking-wav-recorder");
            recordingThread.start();
            listener.onRecordingStarted();
        } catch (SecurityException e) {
            releaseRecorder(false);
            listener.onPermissionDenied();
        } catch (IOException | RuntimeException e) {
            releaseRecorder(false);
            listener.onRecordingFailed();
        }
    }

    public void stopRecording() {
        File audioFile = pendingRecordingFile;
        releaseRecorder(false); // Does not delete it
        if (audioFile != null && audioFile.exists()) {
            listener.onRecordingStopped(audioFile);
        } else {
            listener.onRecordingFailed();
        }
    }

    public void releaseRecorder(boolean deletePendingRecording) {
        isRecordingAudio = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException ignored) {
            }
            audioRecord.release();
            audioRecord = null;
        }
        if (recordingThread != null) {
            try {
                recordingThread.join(500L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            recordingThread = null;
        }
        if (deletePendingRecording && pendingRecordingFile != null) {
            if (pendingRecordingFile.exists()) {
                pendingRecordingFile.delete();
            }
        }
        pendingRecordingFile = null;
    }

    private void writeAudioToWaveFile(File outputFile) {
        byte[] buffer = new byte[audioBufferSize];
        long totalAudioLen = 0L;
        try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
            raf.seek(44L);
            while (isRecordingAudio && audioRecord != null) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    raf.write(buffer, 0, read);
                    totalAudioLen += read;
                }
            }
            updateWaveHeader(raf, totalAudioLen);
        } catch (IOException ignored) {
        }
    }

    private void writeEmptyWaveHeader(File outputFile) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
            byte[] header = new byte[44];
            header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
            header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
            header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
            header[16] = 16; header[20] = 1; header[22] = 1;
            writeIntLE(header, 24, SPEAKING_SAMPLE_RATE);
            writeIntLE(header, 28, SPEAKING_SAMPLE_RATE * 2);
            header[32] = 2; header[34] = 16;
            header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
            raf.write(header);
        }
    }

    private void updateWaveHeader(RandomAccessFile raf, long totalAudioLen) throws IOException {
        long totalDataLen = totalAudioLen + 36L;
        raf.seek(4L);
        writeIntLE(raf, totalDataLen);
        raf.seek(40L);
        writeIntLE(raf, totalAudioLen);
    }

    private void writeIntLE(byte[] target, int offset, long value) {
        target[offset] = (byte) (value & 0xff);
        target[offset + 1] = (byte) ((value >> 8) & 0xff);
        target[offset + 2] = (byte) ((value >> 16) & 0xff);
        target[offset + 3] = (byte) ((value >> 24) & 0xff);
    }

    private void writeIntLE(RandomAccessFile raf, long value) throws IOException {
        raf.write((byte) (value & 0xff));
        raf.write((byte) ((value >> 8) & 0xff));
        raf.write((byte) ((value >> 16) & 0xff));
        raf.write((byte) ((value >> 24) & 0xff));
    }
}
