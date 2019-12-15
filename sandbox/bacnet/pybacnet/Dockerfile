FROM python:3.6-alpine

WORKDIR /app
RUN apk add git && \
    git clone https://github.com/JoelBender/bacpypes.git && \
    cd bacpypes && \
    pip install .

WORKDIR /app/bacpypes

COPY entrypoint.sh ./
RUN chmod +x entrypoint.sh

EXPOSE 47808:47808/udp

ENV DEVICE_ID=4000 DEVICE_NAME="BACNet01"

ENTRYPOINT [ "./entrypoint.sh" ]
