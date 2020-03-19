package bestworkingconditions.biedaflix.server.vod.streamingServiceSource;

import bestworkingconditions.biedaflix.server.file.FileResource;
import bestworkingconditions.biedaflix.server.file.FileResourceContentStore;
import bestworkingconditions.biedaflix.server.file.FileResourceRepository;
import bestworkingconditions.biedaflix.server.common.properties.AppProperties;
import bestworkingconditions.biedaflix.server.vod.series.SeriesRepository;
import bestworkingconditions.biedaflix.server.vod.series.SeriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/streamingSources")
public class StreamingServiceSourceController {

    private final FileResourceContentStore contentStore;
    private final FileResourceRepository fileResourceRepository;
    private final StreamingServiceSourceRepository repository;
    private final StreamingServiceSourceMapper mapper;
    private final SeriesRepository seriesRepository;
    private final StreamingServiceSourceService streamingServiceSourceService;
    private final SeriesService seriesService;
    private final AppProperties appProperties;

    @Autowired
    public StreamingServiceSourceController(FileResourceContentStore contentStore, FileResourceRepository fileResourceRepository, StreamingServiceSourceRepository repository, StreamingServiceSourceMapper mapper, SeriesRepository seriesRepository, StreamingServiceSourceService streamingServiceSourceService, SeriesService seriesService, AppProperties appProperties) {this.contentStore = contentStore;
        this.fileResourceRepository = fileResourceRepository;
        this.repository = repository;
        this.mapper = mapper;
        this.seriesRepository = seriesRepository;
        this.streamingServiceSourceService = streamingServiceSourceService;
        this.seriesService = seriesService;
        this.appProperties = appProperties;
    }

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasAuthority('OP_ADMINISTRATE_SOURCES')")
    public ResponseEntity<?> addLogoFile(
            @PathVariable String id,
            @RequestParam(name = "file") MultipartFile file){
        return ResponseEntity.ok(mapper.toDTO(streamingServiceSourceService.setLogo(id,file)));
    }

    @PostMapping(value = "", consumes = {"application/json"})
    @PreAuthorize("hasAuthority('OP_ADMINISTRATE_SOURCES')")
    public ResponseEntity<?> addStreamingServiceSource(@RequestBody String name) {

        StreamingServiceSource source = new StreamingServiceSource();
        source.setName(name);

        return new ResponseEntity<>(mapper.toDTO(streamingServiceSourceService.create(source)),HttpStatus.CREATED);
    }

    @PatchMapping(value = "/{id}", consumes = {"application/json"})
    @PreAuthorize("hasAuthority('OP_ADMINISTRATE_SOURCES')")
    public ResponseEntity<?> updateStreamingServiceSource( @PathVariable String id,
                                                           @RequestParam(name = "name") String name) {

        StreamingServiceSource source = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "StreamingServiceSource of given id does not exist!"));

        if(name.isPresent()){
            checkIfNameIsAvailable(name.get(),Optional.of(id));
            source.setName(name.get());
        }

        if(logo.isPresent()){
            contentStore.setContent(source,logo.get().getInputStream());
        }

        source = repository.save(source);
        return new ResponseEntity<>(new StreamingServiceSourceResponse(source.getId(),source.getName(),getStreamingServiceURL(source)),HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/streamingSources/{id}")
    @PreAuthorize("hasAuthority('OP_ADMINISTRATE_SOURCES')")
    public ResponseEntity<?> deleteStreamingServiceSource(@PathVariable String id) {

        List<Series> associatedSeries = seriesRepository.findAllByStreamingServiceId(id);

        if(associatedSeries.size() > 0){

            List<SeriesLightResponse> lightResponses = new ArrayList<>();

            for(Series s : associatedSeries){
                SeriesLightResponse lightResponse = seriesService.seriesLightResponseFromSeries(s);
                lightResponses.add(lightResponse);
            }

            JSONObject response = new JSONObject();
            response.put("message","you cannot delete this StreamingSource, as it is used in the following associatedSeries");
            response.put("associatedSeries", lightResponses);

            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }else{
            repository.deleteById(id);
        }

        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/streamingSources")
    public ResponseEntity<?> getListOfAllStreamingServiceSources() throws MalformedURLException {
        List<StreamingServiceSource> streamingServiceSources = repository.findAll();
        List<StreamingServiceSourceResponse> response = new ArrayList<>();

        for(StreamingServiceSource source : streamingServiceSources){
            response.add(new StreamingServiceSourceResponse(source.getId(),source.getName(),getStreamingServiceURL(source)));
        }

        return ResponseEntity.ok(response);
    }

}
