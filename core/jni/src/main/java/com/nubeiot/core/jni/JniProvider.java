package com.nubeiot.core.jni;

import com.nubeiot.core.component.UnitProvider;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JniProvider implements UnitProvider<JniUnit> {

    @Override
    public Class<JniUnit> unitClass() {
        return JniUnit.class;
    }

    @Override
    public JniUnit get() {
        return new JniUnit();
    }

}
