package com.nubeiot.iotdata.unit;

import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface DataTypeCategory extends DataType {

    String DEFAULT = "ALL";

    <T extends DataType, V extends DataType> T convert(V v);

    @Override
    default @NonNull String type() {
        return ReflectionField.constantByName(this.getClass(), "TYPE");
    }

    interface Power extends DataTypeCategory {

        String TYPE = Strings.toSnakeCaseUC(Power.class.getSimpleName());

    }


    interface Pressure extends DataTypeCategory {

        String TYPE = Strings.toSnakeCaseUC(Pressure.class.getSimpleName());

    }


    interface Temperature extends DataTypeCategory {

        String TYPE = Strings.toSnakeCaseUC(Temperature.class.getSimpleName());

    }


    interface Velocity extends DataTypeCategory {

        String TYPE = Strings.toSnakeCaseUC(Velocity.class.getSimpleName());

    }


    interface Illumination extends DataTypeCategory {

        String TYPE = Strings.toSnakeCaseUC(Illumination.class.getSimpleName());

    }


    interface ElectricPotential extends DataTypeCategory {

        String TYPE = Strings.toSnakeCaseUC(ElectricPotential.class.getSimpleName());

    }

}
