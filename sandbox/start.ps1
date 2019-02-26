[CmdletBinding()]
param($H, $services = $(throw "services parameter is required."))

$files = ""
$dashboard = "dashboard mongo keycloak ditto postgres"
$edge = "edge nexus kafka ditto postgres"

if ($services -contains '*dashboard*') { $services = "$dashboard $services" }
if ($services -contains '*edge*') { $services = "$edge $services" }

$services = ($services -split ' ' | Select-Object -Unique) -join ' '

foreach ($service In $services) {
    $file = "$service-docker-compose.yml"
    if (Test-Path -Path $file) {
        $files = "$files -f $file"
    }
    else {
        Write-Output "File $file does not exist"
    }
}
if (-not ([string]::IsNullOrEmpty($files))) {
    if ([string]::IsNullOrEmpty($H)) {
        $CMD = "docker-compose $files up"
    }
    else {
        $CMD = "docker-compose -H $H $files up"
    }
	
    Write-Output $CMD
    Invoke-Expression $CMD
}
else {
    Write-Output "No available component to execute"
}

