package de.rettichlp.pkutils.common.models.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MainConfig {

    private List<TodoEntry> todos = new ArrayList<>();
}
