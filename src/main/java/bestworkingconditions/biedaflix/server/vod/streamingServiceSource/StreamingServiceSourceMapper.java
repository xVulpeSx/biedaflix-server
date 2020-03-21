package bestworkingconditions.biedaflix.server.vod.streamingServiceSource;

import bestworkingconditions.biedaflix.server.file.FileResourceMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = FileResourceMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface StreamingServiceSourceMapper {

    @Mapping(source = "logo", target = "path")
    StreamingServiceSourceResponse toDTO(StreamingServiceSource source);
    List<StreamingServiceSourceResponse> toDTOList(List<StreamingServiceSource> sources);

    StreamingServiceSource streamingServiceSourceFromRequest(StreamingServiceSourceRequest request);
}
