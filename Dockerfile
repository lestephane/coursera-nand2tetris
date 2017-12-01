FROM gradle:alpine

USER root
RUN apk --no-cache add bash git

USER gradle
