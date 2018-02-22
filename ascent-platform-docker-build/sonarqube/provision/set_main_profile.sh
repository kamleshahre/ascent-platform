#!/bin/bash
function curlAdmin {
    curl -v -u admin:admin $@
}

# Restore qualityprofile and exclude Spring Boot Main Application.java class
if [ "$LANGUAGE" ] && [ "$PROFILE_NAME" ]; then
    curlAdmin -F "backup=@/usr/share/configure/sonar/qualityprofile/java-ascent-32413.xml" -X POST "$SONAR_URL/api/qualityprofiles/restore"
    curlAdmin -X POST "$SONAR_URL/api/qualityprofiles/set_default?language=$LANGUAGE&profileName=$PROFILE_NAME"
    curlAdmin -X POST "$SONAR_URL/api/settings/set?key=sonar.coverage.exclusions&values=**/api/v*/transfer/**/*,**/*Application.java"
fi

