package Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Service to handle audio recording from the microphone.
 */
public class VoiceRecordingService {

    private TargetDataLine targetDataLine;
    private File audioFile;
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private boolean isRecording = false;

    /**
     * Starts recording audio to a temporary file.
     *
     * @return the temporary file where audio is being recorded
     * @throws LineUnavailableException if the microphone is unavailable
     */
    public File startRecording() throws LineUnavailableException {
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Line not supported");
        }

        targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(format);
        targetDataLine.start();

        isRecording = true;
        try {
            audioFile = File.createTempFile("voice_record_", ".wav");
            audioFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temp file for recording", e);
        }

        Thread recordingThread = new Thread(() -> {
            try {
                AudioInputStream ais = new AudioInputStream(targetDataLine);
                AudioSystem.write(ais, fileType, audioFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        recordingThread.start();
        return audioFile;
    }

    /**
     * Stops the recording process.
     */
    public void stopRecording() {
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
            isRecording = false;
        }
    }

    /**
     * Checks if recording is in progress.
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Returns the recorded file.
     */
    public File getRecordedFile() {
        return audioFile;
    }

    /**
     * Defines the audio format for recording.
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1; // Mono
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
