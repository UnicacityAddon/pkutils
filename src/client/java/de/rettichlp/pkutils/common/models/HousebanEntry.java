package de.rettichlp.pkutils.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class HousebanEntry {

    private String playerName;
    private String issuerPlayerName;
    private List<String> reasons;
    private LocalDateTime unbanDateTime;
}
