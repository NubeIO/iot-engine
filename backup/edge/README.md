# Beaglebone Black

Disable

```bash
systemctl list-sockets

systemctl stop bonescript.socket bonescript.service cloud9.socket cloud9.service node-red.socket node-red.service

systemctl disable bonescript.socket bonescript.service cloud9.socket cloud9.service node-red.socket node-red.service bonescript-autorun.service
```
