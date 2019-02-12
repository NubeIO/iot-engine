package com.nubeiot.buildscript.jooq

import com.nubeiot.buildscript.Strings

class DB {
    static TYPES = [
        varchar   : Strings.toRegexIgnoreCase("N?VARCHAR"),
        text      : Strings.toRegexIgnoreCase("TEXT|CLOB"),
        date      : Strings.toRegexIgnoreCase("DATE"),
        time      : Strings.toRegexIgnoreCase("TIME"),
        timestamp : Strings.toRegexIgnoreCase("TIMESTAMP"),
        timestampz: Strings.toRegexIgnoreCase("timestamp(\\([0-9]\\))? with time zone"),

    ]
    static COL_REGEX = [
        json     : Strings.toRegexIgnoreCase(".+_JSON\$"),
        jsonArray: Strings.toRegexIgnoreCase(".+(_JSON_ARRAY|_ARRAY)\$"),
        period   : Strings.toRegexIgnoreCase(".+(_PERIOD)\$"),
        duration : Strings.toRegexIgnoreCase(".+(_DURATION)\$"),
    ]
}
