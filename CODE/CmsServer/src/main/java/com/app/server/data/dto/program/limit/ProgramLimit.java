package com.app.server.data.dto.program.limit;

import com.app.server.data.dto.program.Program;
import com.app.server.data.dto.program.ProgramConditionType;
import com.app.server.data.dto.program.ProgramOfferType;
import com.app.server.data.dto.program.product.ProgramProductGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProgramLimit {
    private int id;
    private ProgramConditionType programConditionType;
    private ProgramOfferType programOfferType;
    private List<ProgramProductGroup> ltProgramProductGroup;
    private int level;
    private Program program;

    public ProgramLimit() {
        ltProgramProductGroup = new ArrayList<>();
    }
}