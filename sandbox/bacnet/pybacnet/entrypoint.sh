#!/bin/sh

bin=./samples

hostIp=$(hostname -i | cut -d' ' -f1)
subnet=$(ip -o -f inet addr show | grep "$hostIp" | awk {'print $4'})

sed -e  "s#192.168.210.64/24#$subnet#g" \
    -e "s#599#$DEVICE_ID#g" \
    -e "s#Betelgeuse#$DEVICE_NAME#g" \
    BACpypes~.ini > BACpypes.ini

if [ -f $bin/"$1".py ]; then
    cmd=$1
    shift 1
    python $bin/"$cmd".py "$@"
else
    echo 'Usage: docker run -it --rm py-bacnet <command> <args>'
    echo '... where <command> is one of:'
    ls -C $bin
    exit 1
fi
