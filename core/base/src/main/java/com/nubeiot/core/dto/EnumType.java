package com.nubeiot.core.dto;

import lombok.NonNull;

public interface EnumType extends JsonData {

    @NonNull String type();

}
