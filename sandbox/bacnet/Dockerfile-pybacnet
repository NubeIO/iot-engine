FROM python:3.6-alpine

WORKDIR /app
RUN apk add git && \
    git clone https://github.com/JoelBender/bacpypes.git && \
    cd bacpypes && \
    pip install .

EXPOSE 47808:47808/udp
