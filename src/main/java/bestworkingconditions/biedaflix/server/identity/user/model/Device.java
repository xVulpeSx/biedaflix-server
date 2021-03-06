package bestworkingconditions.biedaflix.server.identity.user.model;

import org.springframework.data.annotation.Id;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
public class Device {

    @Id
    @Generated
    private String id;

    private String userId;

    private String deviceDetails;

    private List<String> ipList;

    private Date lastLoggedIn;
}