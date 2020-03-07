package bestworkingconditions.biedaflix.server.service;

import bestworkingconditions.biedaflix.server.model.Episode;
import bestworkingconditions.biedaflix.server.model.EpisodeSubtitles;
import bestworkingconditions.biedaflix.server.model.EpisodeThumbs;
import bestworkingconditions.biedaflix.server.model.EpisodeVideo;
import bestworkingconditions.biedaflix.server.model.request.EpisodeRequest;
import bestworkingconditions.biedaflix.server.model.response.EpisodeFullResponse;
import bestworkingconditions.biedaflix.server.model.response.MediaFilesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EpisodeService {

    private final SeriesService seriesService;

    @Autowired
    public EpisodeService(SeriesService seriesService) {this.seriesService = seriesService;}


    public Episode episodeFromEpisodeRequest(EpisodeRequest episode){
        return new Episode(
                episode.getSeriesId(),
                episode.getSeasonNumber(),
                episode.getEpisodeNumber(),
                episode.getName(),
                episode.getReleaseDate()
        );
    }

    public EpisodeFullResponse episodeFullResponseFromEpisode(Episode ep){

        Map<String, String> videoSources = new HashMap<>();
        for (EpisodeVideo episodeVideo : ep.getVideoFiles()) {
            videoSources.put(episodeVideo.getVideoQuality()
                                         .getQuality(), seriesService.getSeriesResourceURL(episodeVideo));
        }

        Map<String, String> subtitles = new HashMap<>();
        for (EpisodeSubtitles episodeSubtitles : ep.getEpisodeSubtitles()) {
            subtitles.put(episodeSubtitles.getLanguage()
                                          .getValue(), seriesService.getSeriesResourceURL(episodeSubtitles));
        }

        List<MediaFilesResponse> thumbs = new ArrayList<>();
        for (EpisodeThumbs episodeThumbs : ep.getEpisodeThumbs()) {
            thumbs.add(new MediaFilesResponse(seriesService.getSeriesResourceURL(episodeThumbs)));
        }

        thumbs.sort(Comparator.comparing(MediaFilesResponse::getPath));


        return new EpisodeFullResponse(
                ep.getId(),
                ep.getEpisodeNumber(),
                ep.getName(),
                ep.getEpisodeStatus(),
                ep.getReleaseDate(),
                videoSources,
                subtitles,
                thumbs
        );
    }

}