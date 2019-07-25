package com.aiwatch.postprocess;


import com.aiwatch.Logger;
import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VideoMerger {

    private static final Logger LOGGER = new Logger();

    public void appendVideo(String[] videoUris, String outputFilePath) throws IOException {
        long startTime = System.currentTimeMillis();
        LOGGER.d("in appendVideo() videos length is " + videoUris.length);
        List<Movie> inMovies = new ArrayList<Movie>();
        for (String videoUri : videoUris) {
            inMovies.add(MovieCreator.build(videoUri));
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        LOGGER.d("audioTracks size = " + audioTracks.size()
                + " videoTracks size = " + videoTracks.size());

        if (!audioTracks.isEmpty()) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (!videoTracks.isEmpty()) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        FileChannel fc = new RandomAccessFile(outputFilePath, "rw").getChannel();
        out.writeContainer(fc);
        fc.close();
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        LOGGER.d("completed merge of videos in " + timeElapsed);
    }
}
