FROM debian:stretch-slim

WORKDIR /app
ADD https://astuteinternet.dl.sourceforge.net/project/bacnet/bacnet-stack/bacnet-stack-0.8.6/bacnet-stack-0.8.6.tgz .
ADD wrapper.sh simulator.pl ./

RUN apt-get update && apt-get -y install build-essential \
	&& tar zxf bacnet-stack-0.8.6.tgz \
	&& cd bacnet-stack-0.8.6 \
	&& make \
	&& rm -f bin/*.txt bin/*.bat \
	&& mv bin/* /usr/local/bin \
	&& cd - \
	&& chmod a+x wrapper.sh \
	&& rm -rf bacnet-stack* \
	&& apt-get -y remove build-essential \
	&& apt-get -y autoremove \
	&& apt-get -y autoclean

EXPOSE 47808:47808/udp

CMD ["./wrapper.sh"]
