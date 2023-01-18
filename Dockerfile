FROM gradle:jdk17 as builder

COPY --chown=gradle:gradle build.gradle build.gradle
COPY --chown=gradle:gradle src src
RUN gradle installDist


FROM openjdk:17-jdk-slim as runner

# Alpine
# RUN addgroup -S -g 1000 app 
# RUN adduser -D -S -G app -u 1000 -s /bin/sh app

# Debian
RUN groupadd -r -g 1000 app
RUN useradd -r -u 1000 -g app -d /home/app --create-home  --shell /bin/bash app

USER app
WORKDIR /home/app
COPY --from=builder --chown=app:app /home/gradle/build/install .
EXPOSE 8980

CMD gradle/bin/routeguide-server