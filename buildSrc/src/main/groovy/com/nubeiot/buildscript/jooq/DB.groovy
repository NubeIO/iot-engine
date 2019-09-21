package com.nubeiot.buildscript.jooq

import com.nubeiot.buildscript.Strings

class DB {
    static TYPES = [
        varchar   : Strings.toRegexIgnoreCase("N?VARCHAR"),
        text      : Strings.toRegexIgnoreCase("TEXT|CLOB"),
        date      : Strings.toRegexIgnoreCase("DATE"),
        time      : Strings.toRegexIgnoreCase("TIME"),
        timestamp : Strings.toRegexIgnoreCase("TIMESTAMP"),
        timestampz: Strings.toRegexIgnoreCase("TIMESTAMP(\\([0-9]\\))? WITH TIME ZONE"),
        array     : Strings.toRegexIgnoreCase("ARRAY"),
    ]
    static COL_REGEX = [
        json     : Strings.toRegexIgnoreCase(".+_JSON\$"),
        jsonArray: Strings.toRegexIgnoreCase(".+(_JSON_ARRAY|_ARRAY)\$"),
        period   : Strings.toRegexIgnoreCase(".+(_PERIOD)\$"),
        duration : Strings.toRegexIgnoreCase(".+(_DURATION)\$"),
        timeAudit: Strings.toRegexIgnoreCase("TIME_AUDIT"),
        label    : Strings.toRegexIgnoreCase("LABEL"),
        syncAudit: Strings.toRegexIgnoreCase("SYNC_AUDIT"),
    ]
}
