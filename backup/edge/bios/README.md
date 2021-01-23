# BIOS

It is main application in `IoT device`. It is designed for:

- Ensure only Java process for each `IoT device`
- Responsible for `installing/uninstalling/updating` builtin `module`.

`Module` is default `NubeIO` builtin application such as `Edge Installer` and `Edge Monitor`

- [`Edge Installer`](../module/installer/README.md) helps `install/uninstall/upgrade/downgrade/update` service from `NubeIO` or `3rd service`
- [`Edge Monitor`](../module/monitor/README.md) helps watching resource usage, `edge device` information  

## Architecture design

Refers [Core Installer](../core/README.md)  
