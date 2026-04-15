sed -i '' '/<plugin>/,/<\/plugin>/ {
  /<artifactId>spring-boot-maven-plugin<\/artifactId>/ {
    n
    a\
                <executions>\
                    <execution>\
                        <goals>\
                            <goal>repackage<\/goal>\
                        <\/goals>\
                    <\/execution>\
                <\/executions>\

  }
}' /Users/mac/noor/back/user-service/pom.xml
