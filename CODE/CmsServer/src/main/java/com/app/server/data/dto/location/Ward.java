package com.app.server.data.dto.location;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ward extends Location {
    private District district;
}