package bestworkingconditions.biedaflix.server.controller;

import bestworkingconditions.biedaflix.server.model.*;
import bestworkingconditions.biedaflix.server.model.request.EpisodeRequest;
import bestworkingconditions.biedaflix.server.repository.FileResourceContentStore;
import bestworkingconditions.biedaflix.server.repository.SeriesRepository;
import bestworkingconditions.biedaflix.server.service.TorrentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
public class EpisodeController {

    private final SeriesRepository repository;
    private final TorrentService torrentService;
    private final FileResourceContentStore fileResourceContentStore;

    @Autowired
    public EpisodeController(SeriesRepository repository, TorrentService torrentService, FileResourceContentStore fileResourceContentStore) {
        this.repository = repository;
        this.torrentService = torrentService;
        this.fileResourceContentStore = fileResourceContentStore;
    }

    private Season getRequestedSeasonOrCreate(Series series, int seasonNumber){

        for (Season s : series.getSeasons()){
            if(s.getSeasonNumber() == seasonNumber)
                return s;
        }

        Season newSeason = new Season(seasonNumber);
        series.getSeasons().add(newSeason);
        return newSeason;
    }

    @PostMapping("/addSubtitles")
    public ResponseEntity<Object> AddSubtitles(@NotBlank @RequestParam String seriesId,
                                          @NotNull  @RequestParam int season,
                                          @NotNull @RequestParam int episode,
                                          @Valid @NotNull @RequestParam Episode.SubtitlesLanguage language ,
                                          @NotNull @RequestParam MultipartFile subtitles) throws IOException {

        Optional<Series> requestedSeries = repository.findById(seriesId);

        if(requestedSeries.isPresent()) {
            Series series = requestedSeries.get();

            EpisodeSubtitles subs = new EpisodeSubtitles(subtitles.getContentType(),series.getFolderName(),season,episode,language);
            fileResourceContentStore.setContent(subs,subtitles.getInputStream());

            series.getSeasons().get(season).getEpisodes().get(episode).getSubtitles().put(language,subs.getFilePath());

            return new ResponseEntity<>(HttpStatus.CREATED);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Series of given id does not exist in database");
        }
    }

    @PostMapping("/episode")
    public ResponseEntity<List<TorrentInfo>> AddEpisode(@Valid @RequestBody EpisodeRequest request) {
        Series series = repository.findById(request.getSeriesId())
                                                   .orElseThrow(() ->
                                                           new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found!"));

        Episode newEpisode = new Episode(request.getEpisodeNumber(),request.getName(),request.getReleaseDate());

        Season currentSeason = getRequestedSeasonOrCreate(series,request.getSeasonNumber());

        if(currentSeason.getEpisodes().stream().anyMatch(t->t.getName().equals(request.getName())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Episode of given name already exists in database");

        if(currentSeason.getEpisodes().stream().noneMatch(t -> t.getEpisodeNumber() == newEpisode.getEpisodeNumber())) {
            currentSeason.getEpisodes().add(newEpisode);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Episode of given number already exists in database!");
        }

        repository.save(series);

        torrentService.addTorrent(series.getName(),request,newEpisode);

        List<TorrentInfo> info = torrentService.getTorrentsInfo();

        return ResponseEntity.ok(info);
    }

    @GetMapping("/status")
    public ResponseEntity<List<TorrentInfo>> checkStatus(){
        List<TorrentInfo> info = torrentService.getTorrentsInfo();
        return ResponseEntity.ok(info);
    }
}
