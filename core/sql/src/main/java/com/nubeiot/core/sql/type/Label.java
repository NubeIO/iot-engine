package com.nubeiot.core.sql.type;

import com.nubeiot.core.dto.JsonData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class Label implements JsonData {

    private String label;
    private String description;

}
